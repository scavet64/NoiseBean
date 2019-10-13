///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.scavettapps.noisebean.commands;
//
//import net.dv8tion.jda.api.entities.MessageChannel;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//
///**
// *
// * @author vstro
// */
//public class PingCommand extends AbstractCommand {
//
//   public static String NAME = "ping";
//   
//   @Override
//   public void execute(MessageReceivedEvent event) {
//      MessageChannel channel = event.getChannel();
//      channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
//   }
//
//}
