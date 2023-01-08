/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.core;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * @author vstro
 */
@Service
@Slf4j
public class MessageUtil {

   public static boolean canNotTalk(TextChannel channel) {
      if (channel == null) {
         return true;
      }
      Member member = channel.getGuild().getSelfMember();
      return member == null
         || !member.hasPermission(channel, Permission.MESSAGE_READ)
         || !member.hasPermission(channel, Permission.MESSAGE_WRITE);
   }

   public static Message sendMessage(MessageEmbed embed, MessageChannel channel) {
      return sendMessage(new MessageBuilder().setEmbed(embed).build(), channel);
   }

   public static Message sendMessage(String message, MessageChannel channel) {
      if (message == null || message.isEmpty()) {
         log.warn("Tried to send empty message to channel: {}", channel.getName());
         return null;
      }
      return sendMessage(new MessageBuilder().append(filter(message)).build(), channel);
   }

   private static Message sendMessage(@Nonnull Message message, MessageChannel channel) {
      if (channel instanceof TextChannel && canNotTalk((TextChannel) channel)) {
         return null;
      }
      return channel.sendMessage(message).complete();
   }

   private static String filter(String msgContent) {
      return msgContent.length() > 2000
         ? "*The output message is over 2000 characters!*"
         : msgContent.replace("@everyone", "@\u180Eeveryone").replace("@here", "@\u180Ehere");
   }

   public static String userDiscriminatorSet(User u) {
      return stripFormatting(u.getName()) + "#" + u.getDiscriminator();
   }

   public static String stripFormatting(String s) {
      return s.replace("*", "\\*")
         .replace("`", "\\`")
         .replace("_", "\\_")
         .replace("~~", "\\~\\~")
         .replace(">", "\u180E>");
   }
}
