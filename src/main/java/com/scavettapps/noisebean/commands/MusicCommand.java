/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.commands;

import com.scavettapps.noisebean.music.AudioInfo;
import com.scavettapps.noisebean.music.AudioPlayerSendHandler;
import com.scavettapps.noisebean.music.TrackManager;
import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.core.MessageUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.File;
import java.io.IOException;

import java.util.*;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Component
@Command(name = "music")
public class MusicCommand extends AbstractCommand {

   private static final int PLAYLIST_LIMIT = 200;
   private static final AudioPlayerManager myManager = new DefaultAudioPlayerManager();
   private static final Map<String, Map.Entry<AudioPlayer, TrackManager>> players = new HashMap<>();

   private static final String CD = "\uD83D\uDCBF";
   private static final String DVD = "\uD83D\uDCC0";
   private static final String MIC = "\uD83C\uDFA4 **|>** ";
   private static final String POINTRIGHT = "\u23E9";
   private static final String WARNING_SIGN = "\u26A0";
   private static final String NO_ENTRY = "\u26D4";
   private static final String WHITE_HEAVY_CHECKMARK = "\u2705";
   private static final String RESET = "\uD83D\uDD04";
   private static final String STOPWATCH = "\u23F1";
   private static final String HEADPHONE = "\uD83C\uDFA7";

   private static final String QUEUE_INFO = "Info about the Queue: (Size - %d)";
   private static final String QUEUE_TITLE = "__%s has added %d new track%s to the Queue:__";
   private static final String QUEUE_DESCRIPTION = "%s **|>**  %s\n%s\n%s %s\n%s";
   private static final String ERROR = "Error while loading \"%s\"";

   public MusicCommand() {
      AudioSourceManagers.registerRemoteSources(myManager);
   }

   @Override
   public void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat) {
      Guild guild = e.getGuild();
      switch (args.length) {
         case 0: // Show help message
            sendHelpMessage(chat);
            break;
         case 1:
            oneArgumentSubcommands(args, e, chat, guild);
            break;
         case 2:
            twoArgumentSubcommands(args, e, chat, guild);
            break;
         default:
            break;
      }
   }

   private void oneArgumentSubcommands(String[] args, MessageReceivedEvent e, MessageSender chat, Guild guild) {
      String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
      switch (args[0].toLowerCase()) {
         case "help":
         case "commands":
            sendHelpMessage(chat);
            break;

         case "now":
         case "current":
         case "nowplaying":
         case "info": // Display song info
            infoSubcommand(chat, guild);
            break;

         case "queue":
            queueSubcommand(e, chat, guild);
            break;

         case "skip":
            if (isIdle(chat, guild)) {
               return;
            }
            skipSubcommand(e, chat, guild);
            break;

         case "forceskip":
            if (isIdle(chat, guild)) {
               return;
            }
            forceSkipSubcommand(e, chat, guild);
            break;

         case "reset":
            resetSubcommand(e, chat, guild);
            break;

         case "shuffle":
            if (isIdle(chat, guild)) {
               return;
            }
            shuffleSubcommand(e, chat, guild);
            break;
         case "loop":
            loopSubcommand(e, chat, guild);
            break;

         case "ytplay": // Query YouTube for a music video
            input = "ytsearch: " + input;

         case "play": // Play a track
            if (args.length <= 1) {
               chat.sendMessage("Please include a valid source.");
            } else {
               loadTrack(input, e.getMember(), e.getMessage(), chat);
            }
            break;
         default:
            break;
      }
   }

   private void twoArgumentSubcommands(String[] args, MessageReceivedEvent e, MessageSender chat, Guild guild) {
      String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
      switch (args[0].toLowerCase()) {
         case "play": // Play a track
            loadTrack(input, e.getMember(), e.getMessage(), chat);
            break;
         default:
            break;
      }
   }

   private void loopSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      TrackManager manager = getTrackManager(guild);
      if (manager == null) {
         chat.sendMessage(WARNING_SIGN + " Nothing to loop!");
         return;
      }

      boolean toggleState = manager.toggleLoop();
      if (toggleState) {
         chat.sendMessage(WHITE_HEAVY_CHECKMARK + " Loop has been enabled!");
      } else {
         chat.sendMessage(WHITE_HEAVY_CHECKMARK + " Loop has been disabled!");
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void shuffleSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (isDj(e.getMember())) {
         getTrackManager(guild).shuffleQueue();
         chat.sendMessage(WHITE_HEAVY_CHECKMARK + " Shuffled the queue!");
      } else {
         chat.sendMessage(NO_ENTRY + " You don't have the permission to do that!");
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void resetSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (!isDj(e.getMember())) {
         chat.sendMessage("You don't have the required permissions to do that! [DJ role]");
      } else {
         reset(guild);
         chat.sendMessage(RESET + " Resetting the music player..");
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void forceSkipSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (isCurrentDj(e.getMember()) || isDj(e.getMember())) {
         forceSkipTrack(guild, chat);
      } else {
         chat.sendMessage("You don't have permission to do that!\n" + "Use **"
             + MessageUtil.stripFormatting(this.prefix) + "music skip** to cast a vote!");
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void skipSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (isCurrentDj(e.getMember())) {
         forceSkipTrack(guild, chat);
      } else {
         AudioInfo info = getTrackManager(guild).getTrackInfo(getPlayer(guild).getPlayingTrack());
         if (info.hasVoted(e.getAuthor())) {
            chat.sendMessage(WARNING_SIGN + " You've already voted to skip this song!");
         } else {
            int votes = info.getSkips();
            if (votes >= 3) { // Skip on 4th vote
               getPlayer(guild).stopTrack();
               chat.sendMessage(POINTRIGHT + " Skipping current track.");
            } else {
               info.addSkip(e.getAuthor());
               tryToDelete(e.getMessage());
               chat.sendMessage("**" + MessageUtil.userDiscrimSet(e.getAuthor())
                   + "** has voted to skip this track! [" + (votes + 1) + "/4]");
            }
         }
      }
   }

   /**
    * @param e
    * @param chat
    * @param guild
    */
   private void queueSubcommand(MessageReceivedEvent e, MessageSender chat, Guild guild) {
      if (!hasPlayer(guild) || getTrackManager(guild).getQueuedTracks().isEmpty()) {
         chat.sendMessage("The queue is empty! Load a song with **" + MessageUtil.stripFormatting(this.prefix)
             + "music play**!");
      } else {
         StringBuilder sb = new StringBuilder();
         Set<AudioInfo> queue = getTrackManager(guild).getQueuedTracks();
         queue.forEach(audioInfo -> sb.append(buildQueueMessage(audioInfo)));
         String embedTitle = String.format(QUEUE_INFO, queue.size());
         chat.sendEmbed(embedTitle, "**>** " + sb.toString());
      }
   }

   /**
    * @param chat
    * @param guild
    */
   private void infoSubcommand(MessageSender chat, Guild guild) {
      if (!hasPlayer(guild) || getPlayer(guild).getPlayingTrack() == null) { // No song is playing
         chat.sendMessage("No song is being played at the moment! *It's your time to shine..*");
      } else {
         AudioTrack track = getPlayer(guild).getPlayingTrack();
         StringBuilder trackInfoBuilder = new StringBuilder();

         String trackInfo = trackInfoBuilder
             .append("\n")
             .append(STOPWATCH)
             .append(" **|>** `[ ")
             .append(getTimestamp(track.getPosition()))
             .append(" / ")
             .append(getTimestamp(track.getInfo().length)).append(" ]`").toString();

         StringBuilder authorInfoBuilder = new StringBuilder();
         String authorInfo = authorInfoBuilder
             .append("\n")
             .append(MIC)
             .append(getOrNull(track.getInfo().author))
             .toString();

         StringBuilder requestingUserBuilder = new StringBuilder();
         String reqUser = requestingUserBuilder
             .append("\n")
             .append(HEADPHONE)
             .append(" **|>**  ")
             .append(MessageUtil.userDiscrimSet(
                 getTrackManager(guild).getTrackInfo(track).getAuthor().getUser()))
             .toString();

         chat.sendEmbed("Track Info", String.format(
             QUEUE_DESCRIPTION,
             CD,
             getOrNull(track.getInfo().title),
             trackInfo,
             authorInfo,
             reqUser));
      }
   }

   @Override
   public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
      if (!players.containsKey(event.getGuild().getId())) {
         return; // Guild doesn't have a music player
      }
      TrackManager manager = getTrackManager(event.getGuild());
      manager.getQueuedTracks().stream()
          .filter(info -> !info.getTrack().equals(getPlayer(event.getGuild()).getPlayingTrack())
          && info.getAuthor().getUser().equals(event.getMember().getUser()))
          .forEach(manager::remove);
   }

   @Override
   public void onGuildLeave(GuildLeaveEvent event) {
      reset(event.getGuild());
   }

   private void tryToDelete(Message m) {
      if (m.getGuild().getSelfMember().hasPermission(m.getTextChannel(), Permission.MESSAGE_MANAGE)) {
         m.delete().queue();
      }
   }

   private boolean hasPlayer(Guild guild) {
      return players.containsKey(guild.getId());
   }

   private AudioPlayer getPlayer(Guild guild) {
      AudioPlayer p;
      if (hasPlayer(guild)) {
         p = players.get(guild.getId()).getKey();
      } else {
         p = createPlayer(guild);
      }
      return p;
   }

   private TrackManager getTrackManager(Guild guild) {
      return players.get(guild.getId()).getValue();
   }

   private AudioPlayer createPlayer(Guild guild) {
      AudioPlayer nPlayer = myManager.createPlayer();
      TrackManager manager = new TrackManager(nPlayer);
      nPlayer.addListener(manager);
      guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(nPlayer));
      players.put(guild.getId(), new AbstractMap.SimpleEntry<>(nPlayer, manager));
      return nPlayer;
   }

   private void reset(Guild guild) {
      players.remove(guild.getId());
      getPlayer(guild).destroy();
      getTrackManager(guild).purgeQueue();
      guild.getAudioManager().closeAudioConnection();
   }

   private void loadTrack(String identifier, Member author, Message msg, MessageSender chat) {
      if (author.getVoiceState().getChannel() == null) {
         chat.sendMessage("You are not in a Voice Channel!");
         return;
      }

      Guild guild = author.getGuild();
      getPlayer(guild); // Make sure this guild has a player.

      msg.getTextChannel().sendTyping().queue();
      myManager.loadItemOrdered(guild, identifier, new AudioLoadResultHandlerImpl(chat, identifier, guild, author));
      tryToDelete(msg);
   }

   private boolean isDj(Member member) {
      return member.getRoles().stream().anyMatch(r -> r.getName().equals("DJ"));
   }

   private boolean isCurrentDj(Member member) {
      return getTrackManager(member.getGuild()).getTrackInfo(getPlayer(member.getGuild()).getPlayingTrack())
          .getAuthor().equals(member);
   }

   private boolean isIdle(MessageSender chat, Guild guild) {
      if (!hasPlayer(guild) || getPlayer(guild).getPlayingTrack() == null) {
         chat.sendMessage("No music is being played at the moment!");
         return true;
      }
      return false;
   }

   private void forceSkipTrack(Guild guild, MessageSender chat) {
      getPlayer(guild).stopTrack();
      chat.sendMessage(POINTRIGHT + " Skipping track!");
   }

   private void sendHelpMessage(MessageSender chat) {
      chat.sendEmbed("DJNoiseBeans",
          MessageUtil.stripFormatting(this.prefix) + "music\n"
          + "         -> play [url]           - Load a song or a playlist\n"
          + "         -> ytplay [query]  - Search YouTube for a video and load it\n"
          + "         -> queue                 - View the current queue\n"
          + "         -> skip                     - Cast a vote to skip the current track\n"
          + "         -> current               - Display info related to the current track\n"
          + "         -> forceskip**\\***          - Force a skip\n"
          + "         -> shuffle**\\***              - Shuffle the queue\n"
          + "         -> reset**\\***                 - Reset the music player\n\n"
          + "Commands with an asterisk**\\*** require the __DJ Role__");
   }

   private String buildQueueMessage(AudioInfo info) {
      AudioTrackInfo trackInfo = info.getTrack().getInfo();
      String title = trackInfo.title;
      long length = trackInfo.length;
      return "`[ " + getTimestamp(length) + " ]` " + title + "\n";
   }

   private String getTimestamp(long milis) {
      long seconds = milis / 1000;
      long hours = Math.floorDiv(seconds, 3600);
      seconds = seconds - (hours * 3600);
      long mins = Math.floorDiv(seconds, 60);
      seconds = seconds - (mins * 60);
      return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
   }

   private String getOrNull(String s) {
      return s.isEmpty() ? "N/A" : s;
   }

   private final class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {

      private final MessageSender chat;
      private final String identifier;
      private final Guild guild;
      private final Member author;

      private AudioLoadResultHandlerImpl(MessageSender chat, String identifier, Guild guild, Member author) {
         this.chat = chat;
         this.identifier = identifier;
         this.guild = guild;
         this.author = author;
      }

      @Override
      public void trackLoaded(AudioTrack track) {
         chat.sendEmbed(String.format(QUEUE_TITLE, MessageUtil.userDiscrimSet(author.getUser()), 1, ""),
             String.format(
                 QUEUE_DESCRIPTION,
                 CD,
                 getOrNull(track.getInfo().title),
                 "",
                 MIC,
                 getOrNull(track.getInfo().author),
                 ""));
         getTrackManager(guild).queue(track, author);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
         if (playlist.getSelectedTrack() != null) {
            trackLoaded(playlist.getSelectedTrack());
         } else if (playlist.isSearchResult()) {
            trackLoaded(playlist.getTracks().get(0));
         } else {
            chat.sendEmbed(
                String.format(
                    QUEUE_TITLE,
                    MessageUtil.userDiscrimSet(author.getUser()),
                    Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT),
                    "s"),
                String.format(QUEUE_DESCRIPTION, DVD, getOrNull(playlist.getName()), "", "", "", ""));
            for (int i = 0; i < Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT); i++) {
               getTrackManager(guild).queue(playlist.getTracks().get(i), author);
            }
         }
      }

      @Override
      public void noMatches() {
         chat.sendEmbed(String.format(ERROR, identifier), WARNING_SIGN + " No playable tracks were found.");
      }

      @Override
      public void loadFailed(FriendlyException exception) {
         chat.sendEmbed(String.format(ERROR, identifier), NO_ENTRY + " " + exception.getLocalizedMessage());
      }
   }
}
