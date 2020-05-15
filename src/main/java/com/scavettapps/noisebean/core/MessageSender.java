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

/**
 *
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

   public Message sendEmbed(String title, String description) {
      if (event.isFromType(ChannelType.TEXT)
          && event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
         return MessageUtil.sendMessage(
             new EmbedBuilder().setTitle(title, null).setDescription(description).build(),
             event.getChannel()
         );
      } else {
         return sendMessage("Please give the bot permissions to `EMBED LINKS`.");
      }
   }

   /**
    * TODO: Find a better way to do this
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
