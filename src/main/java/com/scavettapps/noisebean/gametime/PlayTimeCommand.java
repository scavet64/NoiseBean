/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import com.scavettapps.noisebean.commands.AbstractCommand;
import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.core.MessageUtil;
import java.util.Arrays;
import java.util.Map;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Command(name = "playtime", description = "Check your playtime for different games")
public class PlayTimeCommand extends AbstractCommand {
   
   private final static String PLAYTIME_FORMAT = "You have played %d minutes of %s";
   
   private final GameSessionService gameSessionService;

   @Autowired
   public PlayTimeCommand(GameSessionService gameSessionService) {
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
      
      String[] remainingText = Arrays.copyOfRange(args, 1, args.length);
      String gameName = String.join(" ", remainingText);
      
      long playtime = this.gameSessionService.getPlaytime(event.getAuthor().getId(), gameName);
      
      chat.sendMessage(String.format(PLAYTIME_FORMAT, playtime, gameName));
   }
   
   private void getPlayTimeList(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length != 1) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }
      
      Map<String, Long> playtimes = this.gameSessionService.getPlayTimeList(event.getAuthor().getId());
      
      StringBuilder sb = new StringBuilder();
      for(String key : playtimes.keySet()) {
         Long playtime = playtimes.get(key);
         sb.append(String.format("%s for %d minutes\n", key, playtime));
      }
      
      chat.sendEmbed("Game Times", sb.toString());
   }
   
}
