package com.scavettapps.noisebean.gametime;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;

@Component
public class PlayTimeSlashListener extends ListenerAdapter {

   public static final String PLAYTIME_SINCE_FROM_OPTION = "from";

   private static final String ALL_PLAYTIME_TITLE_FORMAT = "All Playtimes for @%s";
   private static final String PLAYTIME_SINCE_TITLE_FORMAT = "Playtimes for %s since %s";

   private static final String NEXT_BUTTON_NAME = "next";
   private static final String PREVIOUS_BUTTON_NAME = "previous";
   private final String paginationFooterRegex = "Page (\\d)\\/\\d";
   private final Pattern paginationPattern = Pattern.compile(paginationFooterRegex);

   private final String allPlaytimesTitleRegex = "All Playtimes for @(.*)";
   private final Pattern allPlaytimesTitleRegexPattern = Pattern.compile(allPlaytimesTitleRegex);

   private final String playtimesSinceTitleRegex = "Playtimes for (.*) since (.*)";
   private final Pattern playtimesSinceTitleRegexPattern = Pattern.compile(playtimesSinceTitleRegex);

   private final DateTimeFormatter datetimeFormatter = new DateTimeFormatterBuilder()
         .appendPattern("M/d/yyyy")
         .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
         .toFormatter()
         .withZone(ZoneOffset.UTC);

   private final GameSessionService gameSessionService;

   public PlayTimeSlashListener(
         GameSessionService gameSessionService) {
      this.gameSessionService = gameSessionService;
   }

   @Override
   public void onSlashCommand(@Nonnull SlashCommandEvent event) {
      if (!event.getName().equals("playtime")) {
         return; // make sure we handle the right command
      }

      var subcommand = event.getSubcommandName();
      User userToLookup;
      if (event.getOption("user") != null) {
         userToLookup = event.getOption("user").getAsUser();
      } else {
         userToLookup = event.getUser();
      }
      switch (subcommand) {
         case "all":
            processGetAllPlaytimes(event, userToLookup);
            break;
         case "since":
            processGetPlaytimesSinceDate(event, userToLookup, event.getOption(PLAYTIME_SINCE_FROM_OPTION).getAsString());
            break;
         default:
            break;
      }
   }

   private void processGetAllPlaytimes(SlashCommandEvent event, User user) {
      var playtimes = this.gameSessionService.getPlayTimeList(user.getId());
      if (playtimes.isEmpty()) {
         event.reply("You didn't play anything :^O").queue();
         return;
      }

      var partitionedPlaytimes = Lists.partition(playtimes, 25);
      var pageToUse = partitionedPlaytimes.get(0);

      var titleString = String.format(ALL_PLAYTIME_TITLE_FORMAT, user.getName());

      var embed = buildPlaytimeEmbed(titleString, pageToUse, 1, partitionedPlaytimes.size());
      var buttons = buildPageableButtons();
      event.replyEmbeds(embed).setEphemeral(false).addActionRow(buttons).queue();
   }

   /**
    * Process the subcommand for getting playtimes since a date
    * 
    * @param event The event to be processed
    * @param user The user making the action
    * @param sinceString The date since
    */
   private void processGetPlaytimesSinceDate(SlashCommandEvent event, User user, String sinceString) {
      Instant since = datetimeFormatter.parse(sinceString, Instant::from);

      var playtimes = this.gameSessionService.getPlayTimeList(user.getId(), since);
      if (playtimes.isEmpty()) {
         event.reply("You didn't play anything :^O").queue();
         return;
      }

      var partitionedPlaytimes = Lists.partition(playtimes, 25);
      var pageToUse = partitionedPlaytimes.get(0);

      var title = String.format(PLAYTIME_SINCE_TITLE_FORMAT, user.getName(), sinceString);

      var embed = buildPlaytimeEmbed(title, pageToUse, 1, partitionedPlaytimes.size());
      var buttons = buildPageableButtons();
      event.replyEmbeds(embed).setEphemeral(false).addActionRow(buttons).queue();
   }

   private boolean isButtonPageAction(String buttonId) {
      return buttonId.equalsIgnoreCase(NEXT_BUTTON_NAME)
            || buttonId.equalsIgnoreCase(PREVIOUS_BUTTON_NAME);
   }

   @Override
   public void onButtonClick(@Nonnull ButtonClickEvent e) {

      var buttonId = e.getButton().getId();
      if (!isButtonPageAction(buttonId)) {
         return;
      }

      // Check to see what command is being used here to determine the page that should be retrieved.
      // TODO: This feels extremely hacky, there has to be a better way to determine where the event was
      // triggered
      var embeddedTitle = e.getMessage().getEmbeds().get(0).getTitle();
      List<GamePlayTime> playtimes;
      if (embeddedTitle.matches(playtimesSinceTitleRegex)) {
         // all playtimes
         final Matcher matcher = playtimesSinceTitleRegexPattern.matcher(embeddedTitle);
         matcher.matches();
         var extractedUsername = matcher.group(1);
         var extractedDateString = matcher.group(2);

         var user = lookupUser(e.getGuild(), extractedUsername);

         Instant since = datetimeFormatter.parse(extractedDateString, Instant::from);
         playtimes = this.gameSessionService.getPlayTimeList(user.getId(), since);
      } else if (embeddedTitle.matches(allPlaytimesTitleRegex)) {
         // playtime since
         final Matcher matcher = allPlaytimesTitleRegexPattern.matcher(embeddedTitle);
         matcher.matches();
         var extractedUsername = matcher.group(1);

         var user = lookupUser(e.getGuild(), extractedUsername);

         playtimes = this.gameSessionService.getPlayTimeList(user.getId());
      } else {
         throw new RuntimeException("Could not determine the lookup function");
      }

      var partitionedPlaytimes = Lists.partition(playtimes, 25);

      final Matcher matcher = paginationPattern.matcher(e.getMessage().getEmbeds().get(0).getFooter().getText());
      matcher.matches();
      int currentPageNumber = Integer.parseInt(matcher.group(1));

      int newPageNumber;
      List<GamePlayTime> playtimesToDisplay;
      switch (e.getButton().getId()) {
         case NEXT_BUTTON_NAME:
            newPageNumber = currentPageNumber + 1;
            playtimesToDisplay = partitionedPlaytimes.get(newPageNumber - 1);
            break;
         case PREVIOUS_BUTTON_NAME:
            newPageNumber = currentPageNumber - 1;
            playtimesToDisplay = partitionedPlaytimes.get(newPageNumber - 1);
            break;
         default:
            throw new RuntimeException("Unknown button name!");
      }

      var embed = buildPlaytimeEmbed(embeddedTitle, playtimesToDisplay, newPageNumber, partitionedPlaytimes.size());
      var buttons = buildPageableButtons();
      e.getInteraction().getMessage().editMessageEmbeds(embed).setActionRow(buttons).queue();
      e.deferEdit().queue();;
   }

   private User lookupUser(Guild guild, String userName) {
      var members = guild.getMembersByName(userName, true);
      return members.get(0).getUser();
   }

   private MessageEmbed buildPlaytimeEmbed(String title, List<GamePlayTime> playtimesToDisplay, int newPageNumber, int totalPages) {
      EmbedBuilder msg = new EmbedBuilder();
      msg.setTitle(title);
      msg.setDescription(buildPlaytimeString(playtimesToDisplay));
      msg.setFooter("Page " + newPageNumber + "/" + totalPages);
      msg.setColor(0x33cc33);
      return msg.build();
   }

   private List<Button> buildPageableButtons() {
      List<Button> buttons = new ArrayList<Button>();
      buttons.add(Button.primary(PREVIOUS_BUTTON_NAME, Emoji.fromUnicode("⏪")));
      buttons.add(Button.primary(NEXT_BUTTON_NAME, Emoji.fromUnicode("⏩")));
      return buttons;
   }

   private String buildPlaytimeString(List<GamePlayTime> playtimes) {
      playtimes.sort(GamePlayTime.PlayTimeDesc);

      StringBuilder sb = new StringBuilder();
      for (GamePlayTime gamePlayTime : playtimes) {
         String timeString = gamePlayTime.getPlayTimeString();
         sb.append(String.format("**%s** for %s\n", gamePlayTime.getGameName(), timeString));
      }
      return sb.toString();
   }
}
