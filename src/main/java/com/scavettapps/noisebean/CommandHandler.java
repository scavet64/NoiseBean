///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.scavettapps.noisebean;
//
//import com.scavettapps.noisebean.commands.AbstractCommand;
////import com.scavettapps.noisebean.commands.PingCommand;
//import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
//import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
//import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
//import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
//import java.util.HashMap;
//import java.util.Map;
//import net.dv8tion.jda.api.entities.Guild;
//import net.dv8tion.jda.api.entities.GuildVoiceState;
//import net.dv8tion.jda.api.entities.Member;
//import net.dv8tion.jda.api.entities.Message;
//import net.dv8tion.jda.api.entities.VoiceChannel;
//import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
//import net.dv8tion.jda.api.hooks.ListenerAdapter;
//import net.dv8tion.jda.api.managers.AudioManager;
//
///**
// *
// * @author vstro
// */
//public class CommandHandler extends ListenerAdapter {
//
//   private final Map<String, AbstractCommand> commands;
//
//   public CommandHandler() {
//      commands = new HashMap<>();
//      //commands.put(PingCommand.NAME, new PingCommand());
//   }
//
//   @Override
//   public void onMessageReceived(MessageReceivedEvent event) {
//      if (event.getAuthor().isBot()) {
//         return;
//      }
//      // We don't want to respond to other bot accounts, including ourself
//      Message message = event.getMessage();
//      String content = message.getContentRaw();
//      // getContentRaw() is an atomic getter
//      // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
////      if (content.startsWith("!")) {
////         String[] words = content.split(" ");
////         String command = words[0].substring(1);
////         if (commands.containsKey(command)) {
////            commands.get(command).execute(event);
////         }
////      }
//
//      // This makes sure we only execute our code when someone sends a message with "!play"
//      if (!event.getMessage().getContentRaw().startsWith("!play")) {
//         return;
//      }
//      // Now we want to exclude messages from bots since we want to avoid command loops in chat!
//      // this will include own messages as well for bot accounts
//      // if this is not a bot make sure to check if this message is sent by yourself!
//      if (event.getAuthor().isBot()) {
//         return;
//      }
//      Guild guild = event.getGuild();
//      // This will get the first voice channel with the name "music"
//      // matching by voiceChannel.getName().equalsIgnoreCase("music")
//
//      Member mem = event.getMember();
//      GuildVoiceState vs = mem.getVoiceState();
//      VoiceChannel channel = vs.getChannel();
//      AudioManager manager = guild.getAudioManager();
//
//      AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
//      AudioSourceManagers.registerRemoteSources(playerManager);
//      
//      AudioPlayer player = playerManager.createPlayer();
//
//      // MySendHandler should be your AudioSendHandler implementation
//      //manager.setSendingHandler(new LavaPlayerAudioProvider());
//      // Here we finally connect to the target voice channel 
//      // and it will automatically start pulling the audio from the MySendHandler instance
//      manager.openAudioConnection(channel);
//   }
//
//}
