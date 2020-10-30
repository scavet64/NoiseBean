/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import com.scavettapps.noisebean.commands.AbstractCommand;
import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.core.MessageUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Command(name = "playtime", description = "Check your playtime for different games")
public class PlayTimeCommand extends AbstractCommand {
   
   private final GameSessionService gameSessionService;

   @Autowired
   public PlayTimeCommand(
       GameSessionService gameSessionService
   ) {
      this.gameSessionService = gameSessionService;
   } 
   
   @Override
   public void executeCommand(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length == 0) {
         sendHelpMessage(chat);
         return;
      }

      switch (args[0]) {
         case "game":
            getPlayTime(args, event, chat);
            break;
         case "list":
            getPlayTimeList(args, event, chat);
            break;
         case "since":
            getPlayTimeListSince(args, event, chat);
            break;
         case "top":
            getPlayTimeListTop(args, event, chat);
            break;
         default:
            break;
      }
   }
   
   private void sendHelpMessage(MessageSender chat) {
      chat.sendEmbed("Playtime Help",
          MessageUtil.stripFormatting(this.prefix) + "playtime\n"
          + " -> game [name] - Get your playtime for this game\n"
          + " -> list - Get a list of playtimes\n");
   }

   private void getPlayTime(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length < 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }

      String gameName = this.joinRestOfArguments(args, 1);
      
      GamePlayTime gamePlayTime = this.gameSessionService.getPlaytime(event.getAuthor().getId(), gameName);
      
      
      String timeString = gamePlayTime.getPlayTimeString();
      chat.sendMessage(String.format("You have played **%s** for %s\n", gamePlayTime.getGameName(), timeString));
   }
   
   private void getPlayTimeList(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length != 1) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }
      
      var playtimes = this.gameSessionService.getPlayTimeList(event.getAuthor().getId());

      chat.sendEmbed("Game Times", buildPlaytimeString(playtimes));
   }

   private void getPlayTimeListSince(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length < 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }

      String date = this.joinRestOfArguments(args, 1);
      final DateTimeFormatter FMT = new DateTimeFormatterBuilder()
         .appendPattern("M/d/yyyy")
         .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
         .toFormatter()
         .withZone(ZoneOffset.UTC);
      Instant since = FMT.parse(date, Instant::from);

      var playtimes = this.gameSessionService.getPlayTimeList(event.getAuthor().getId(), since);

      chat.sendEmbed("Game Times", buildPlaytimeString(playtimes));
   }

   private void getPlayTimeListTop(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length != 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }

      try {
         String number = args[1];
         int top = Integer.parseInt(number);

         var playtimes = this.gameSessionService.getPlayTimeList(event.getAuthor().getId(), top);
         chat.sendEmbed("Game Times", buildPlaytimeString(playtimes));
      } catch (NumberFormatException nfe) {
         chat.sendMessage("Malformed number :^()");
      }
   }

   @NotNull
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
