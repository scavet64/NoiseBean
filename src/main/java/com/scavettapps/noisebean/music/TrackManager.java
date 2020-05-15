/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author vstro
 */
public class TrackManager extends AudioEventAdapter {

   private Logger LOGGER = LoggerFactory.getLogger(TrackManager.class);

   private final AudioPlayer player;
   private final Queue<AudioInfo> queue;
   private boolean isLooping;

   public TrackManager(AudioPlayer player) {
      this.player = player;
      this.queue = new LinkedBlockingQueue<>();
   }

   public boolean toggleLoop() {
      this.isLooping = !this.isLooping;
      return this.isLooping;
   }
   
   public void queue(AudioTrack track, Member author, VoiceChannel channel) {
      AudioInfo info = new AudioInfo(track, author, channel);
      queue.add(info);

      if (player.getPlayingTrack() == null) {
         player.playTrack(track);
      }
   }

   public void queue(AudioTrack track, Member author) {
      AudioInfo info = new AudioInfo(track, author, author.getVoiceState().getChannel());
      queue.add(info);

      if (player.getPlayingTrack() == null) {
         player.playTrack(track);
      }
   }

   @Override
   public void onTrackStart(AudioPlayer player, AudioTrack track) {
      AudioInfo info = queue.element();
      VoiceChannel vChan = info.getChannel();
      if (vChan == null) { // User has left all voice channels
         player.stopTrack();
      } else {
         info.getAuthor().getGuild().getAudioManager().openAudioConnection(vChan);
      }
   }

   @Override
   public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
      if (isLooping) {
         LOGGER.info("Playing looped song");
         player.playTrack(track.makeClone());
      } else {
         Guild g = queue.poll().getAuthor().getGuild();
         if (queue.isEmpty()) {
            g.getAudioManager().closeAudioConnection();
         } else {
            player.playTrack(queue.element().getTrack());
         }
      }
   }

   public void shuffleQueue() {
      List<AudioInfo> tQueue = new ArrayList<>(getQueuedTracks());
      AudioInfo current = tQueue.get(0);
      tQueue.remove(0);
      Collections.shuffle(tQueue);
      tQueue.add(0, current);
      purgeQueue();
      queue.addAll(tQueue);
   }

   public Set<AudioInfo> getQueuedTracks() {
      return new LinkedHashSet<>(queue);
   }

   public void purgeQueue() {
      queue.clear();
   }

   public void remove(AudioInfo entry) {
      queue.remove(entry);
   }

   public AudioInfo getTrackInfo(AudioTrack track) {
      return queue.stream().filter(audioInfo -> audioInfo.getTrack().equals(track)).findFirst().orElse(null);
   }
}
