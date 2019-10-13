/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean;

import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.music.LavaPlayerAudioProvider;
import com.scavettapps.noisebean.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.voice.AudioProvider;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author vstro
 */
public class Driver {

   private static final String PREFIX = "!";

   private static final Map<String, Command> commands = new HashMap<>();

   static {
      commands.put("ping", event -> event.getMessage().getChannel()
          .flatMap(channel -> channel.createMessage("Pong!"))
          .then());
   }

   public static void main(String[] args) throws Exception {

      Properties prop = new Properties();
      try (InputStream input = Driver.class.getClassLoader().getResourceAsStream("application.properties")) {

         if (input == null) {
            System.out.println("Sorry, unable to find config.properties");
            return;
         }

         //load a properties file from class path, inside static method
         prop.load(input);

         System.out.println(prop.getProperty("apikey"));
         
      } catch (IOException ex) {
         ex.printStackTrace();
      }

      // Creates AudioPlayer instances and translates URLs to AudioTrack instances
      final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
      // This is an optimization strategy that Discord4J can utilize. It is not important to understand
      playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
      // Allow playerManager to parse remote sources like YouTube links
      AudioSourceManagers.registerRemoteSources(playerManager);
      // Create an AudioPlayer so Discord4J can receive audio data
      final AudioPlayer player = playerManager.createPlayer();
      // We will be creating LavaPlayerAudioProvider in the next step
      AudioProvider provider = new LavaPlayerAudioProvider(player);

      final DiscordClient client = new DiscordClientBuilder(prop.getProperty("apikey")).build();

      client.getEventDispatcher().on(ReadyEvent.class)
          .subscribe(ready -> System.out.println("Logged in as " + ready.getSelf().getUsername()));   

      commands.put("join", event -> Mono.justOrEmpty(event.getMember())
          .flatMap(Member::getVoiceState)
          .flatMap(VoiceState::getChannel)
          // join returns a VoiceConnection which would be required if we were
          // adding disconnection features, but for now we are just ignoring it.
          .flatMap(channel -> channel.join(spec -> spec.setProvider(provider)))
          .then());

      final TrackScheduler scheduler = new TrackScheduler(player);
      commands.put("play", event -> Mono.justOrEmpty(event.getMessage().getContent())
          .map(content -> Arrays.asList(content.split(" ")))
          .doOnNext(command -> playerManager.loadItem(command.get(1), scheduler))
          .then());

      client.getEventDispatcher().on(MessageCreateEvent.class)
          .flatMap(event -> Mono.justOrEmpty(event.getMessage().getContent())
          .flatMap(content -> Flux.fromIterable(commands.entrySet())
          // We will be using ! as our "prefix" to any command in the system.
          .filter(entry -> content.startsWith(PREFIX + entry.getKey()))
          .flatMap(entry -> entry.getValue().execute(event))
          .next()))
          .subscribe();

      client.login().block();
   }
}
