/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.core;

import com.scavettapps.noisebean.intro.IntroductionCommand;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vstro
 */
public class MessageSender {

   private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

   private final GenericMessageEvent event;

   public MessageSender(GenericMessageEvent event) {
      this.event = event;
   }

   public Message sendMessage(String msgContent, MessageChannel tChannel) {
      if (tChannel == null || msgContent == null || msgContent.isEmpty()) {
         LOGGER.warn("unable to send message due to illegal argument");
         return null;
      }
      return MessageUtil.sendMessage(msgContent, tChannel);
   }

   public Message sendMessage(String msgContent) {
      return sendMessage(msgContent, event.getChannel());
   }

   /**
    * TODO Add checks to see if embeds are allowed
    *
    * @param title
    * @param description
    * @param channel
    * @return
    */
   public Message sendEmbed(String title, String description, MessageChannel channel) {

      return MessageUtil.sendMessage(
         new EmbedBuilder().setTitle(title, null).setDescription(description).build(),
         channel
      );

   }

   public List<Message> sendEmbed(String title, String description) {
      List<Message> sentMessages = new ArrayList<>();
      if (event.isFromType(ChannelType.TEXT)
         && event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
         // Check to make sure that the embedded description is not longer than 2048 characters
         List<String> messages = stringSplitter(description, 2048);
         messages.forEach((message) -> {
            sentMessages.add(MessageUtil.sendMessage(
               new EmbedBuilder().setTitle(title, null).setDescription(message).build(),
               event.getChannel()
            ));
         });
      } else {
         sentMessages.add(sendMessage("Please give the bot permissions to `EMBED LINKS`."));
      }
      return sentMessages;
   }

   private List<String> stringSplitter(String string, int maxLength) {
      List<String> splitStrings = new ArrayList<>();
      do {
         int start = Math.min(maxLength, string.length() - 1);
         boolean split = false;
         // Start at the max length and work backwards until we find the first new line break.
         for (int i = start; i >= 0; i--) {
            if (string.charAt(i) == '\n') {
               // Found our breakpoint
               int breakPoint = i + 1;
               splitStrings.add(string.substring(0, breakPoint));
               string = string.substring(breakPoint);
               split = true;
               break;
            }
         }
         // If we didnt find one, look for a space I guess
         if (!split) {
            for (int i = start; i > 0; i--) {
               if (string.charAt(i) == ' ') {
                  // Found our breakpoint
                  splitStrings.add(string.substring(0, i));
                  string = string.substring(start);
                  break;
               }
            }
         }
      } while (string.length() > 0);
      return splitStrings;
   }

   /**
    * TODO: Find a better way to do this
    *
    * @param content
    * @param user
    */
   public void sendPrivateMessageToUser(String content, User user) {
      PrivateChannel channel = user.openPrivateChannel().complete();
      sendMessage(content, channel);
   }

//   public void sendPrivateMessageToUser(String content, User user) {
//      user.openPrivateChannel().queue(c -> sendMessage(content, c));
//   }
}
