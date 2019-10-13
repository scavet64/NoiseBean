/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean;

import com.scavettapps.noisebean.commands.MusicCommand;
import com.scavettapps.noisebean.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 *
 * @author vstro
 */
public class Driver {

   private static final String PREFIX = "!";

   public static void main(String[] args) throws Exception {

      Properties prop = new Properties();
      try ( InputStream input = Driver.class.getClassLoader().getResourceAsStream("application.properties")) {

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
      //AudioProvider provider = new LavaPlayerAudioProvider(player);


      JDABuilder builder = new JDABuilder(prop.getProperty("apikey"));
      builder.addEventListeners(new MusicCommand());

      // Disable parts of the cache
      //builder.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE));
      // Enable the bulk delete event
      builder.setBulkDeleteSplittingEnabled(false);
      // Disable compression (not recommended)
      //builder.setCompression(Compression.NONE);
      // Set activity (like "playing Something")
      builder.setActivity(Activity.watching("TV"));

      builder.build();
   }
}
