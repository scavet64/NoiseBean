/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 *
 * @author vstro
 */

public class MessageUtil {

    public static boolean canNotTalk(TextChannel channel) {
        if (channel == null) return true;
        Member member = channel.getGuild().getSelfMember();
        return member == null
                || !member.hasPermission(channel, Permission.MESSAGE_READ)
                || !member.hasPermission(channel, Permission.MESSAGE_WRITE);
    }

    private static void sendMessage(Message message, MessageChannel channel) {
        if (channel instanceof TextChannel && canNotTalk((TextChannel) channel)) return;
        channel.sendMessage(message).queue(null, null);
    }

    public static void sendMessage(MessageEmbed embed, MessageChannel channel) {
        sendMessage(new MessageBuilder().setEmbed(embed).build(), channel);
    }

    public static void sendMessage(String message, MessageChannel channel) {
        sendMessage(new MessageBuilder().append(filter(message)).build(), channel);
    }

    private static String filter(String msgContent) {
        return msgContent.length() > 2000
                ? "*The output message is over 2000 characters!*"
                : msgContent.replace("@everyone", "@\u180Eeveryone").replace("@here", "@\u180Ehere");
    }

    public static String userDiscrimSet(User u) {
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
