/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.core;

import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 *
 * @author vstro
 */
public class MessageSender {

   private final MessageReceivedEvent event;

   public MessageSender(MessageReceivedEvent event) {
      this.event = event;
   }

   public void sendMessage(String msgContent, MessageChannel tChannel) {
      if (tChannel == null) {
         return;
      }
      MessageUtil.sendMessage(msgContent, tChannel);
   }

   public void sendMessage(String msgContent) {
      sendMessage(msgContent, event.getChannel());
   }

   public void sendEmbed(String title, String description) {
      if (event.isFromType(ChannelType.TEXT) && event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
         MessageUtil.sendMessage(new EmbedBuilder().setTitle(title, null).setDescription(description).build(), event.getChannel());
      } else {
         sendMessage("Please give the bot permissions to `EMBED LINKS`.");
      }
   }

   public void sendPrivateMessageToUser(String content, User user) {
      user.openPrivateChannel().queue(c -> sendMessage(content, c));
   }
}
