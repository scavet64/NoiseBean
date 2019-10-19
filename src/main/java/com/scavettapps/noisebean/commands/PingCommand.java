/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.commands;

import org.springframework.stereotype.Service;

import com.scavettapps.noisebean.core.MessageSender;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 *
 * @author vstro
 */
@Service
@Command(name = "ping")
public class PingCommand extends AbstractCommand {

	@Override
	public void executeCommand(String[] args, MessageReceivedEvent event, MessageSender chat) {
		MessageChannel channel = event.getChannel();
		channel.sendMessage("Pong!").queue();
	}

	@Override
	public boolean allowsPrivate() {
		return true;
	}

}
