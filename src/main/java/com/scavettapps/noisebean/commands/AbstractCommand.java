/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.commands;

import com.scavettapps.noisebean.MessageSender;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 *
 * @author vstro
 */
public abstract class AbstractCommand extends ListenerAdapter {

    public abstract void executeCommand(String[] args, MessageReceivedEvent e, MessageSender chat);

    public abstract List<String> getAlias();

    public boolean allowsPrivate() {
        return false;
    }

    public boolean authorExclusive() {
        return false;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        // Checks related to the Event's objects, to prevent concurrency issues.
        if (e.getAuthor() == null || e.getChannel() == null)
            return;

        if (e.getAuthor().isBot() || !isValidCommand(e.getMessage()))
            return; // Ignore message if it's not a command or sent by a bot
//        if (authorExclusive() && !e.getAuthor().getId().equals(Info.AUTHOR_ID))
//            return; // Ignore if the command is meant to be used by the owner only
//        if (e.isFromType(ChannelType.TEXT) && MessageUtil.canNotTalk(e.getTextChannel()))
//            return; // Ignore if we cannot talk in the channel anyway

        String[] args = commandArgs(e.getMessage());
        MessageSender chat = new MessageSender(e);

        if (e.isFromType(ChannelType.PRIVATE) && !allowsPrivate()) { // Check if the command is guild-only
            chat.sendMessage("**This command can only be used in a guild!**");
        } else {
            try {
                executeCommand(args, e, chat);
            } catch (Exception ex) {
                ex.printStackTrace();
//                String msg = "User: **" + MessageUtil.userDiscrimSet(e.getAuthor())
//                        + "**\nMessage:\n*" + MessageUtil.stripFormatting(e.getMessage().getContentDisplay())
//                        + "*\n\nError:```java\n" + ex.getMessage() + "```";
//                if (msg.length() <= 2000) {
//                    chat.sendPrivateMessageToUser(msg, e.getJDA().getUserById(Info.AUTHOR_ID));
//                }
            }
        }
    }

    private boolean isValidCommand(Message msg) {
        String prefix = "!";
        if (!msg.getContentRaw().startsWith(prefix))
            return false; // It's not a command if it doesn't start with our prefix
        String cmdName = msg.getContentRaw().substring(prefix.length());
        if (cmdName.contains(" ")) {
            cmdName = cmdName.substring(0, cmdName.indexOf(" ")); // If there are parameters, remove them
        }
        if (cmdName.contains("\n")) {
            cmdName = cmdName.substring(0, cmdName.indexOf("\n"));
        }
        return getAlias().contains(cmdName.toLowerCase());
    }

    private String[] commandArgs(Message msg) {
        String noPrefix = msg.getContentRaw().substring("!".length());
        if (!noPrefix.contains(" ")) { // No whitespaces -> No args
            return new String[]{};
        }
        return noPrefix.substring(noPrefix.indexOf(" ") + 1).split("\\s+");
    }

}
