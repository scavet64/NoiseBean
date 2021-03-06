/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scavettapps.noisebean.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.HashSet;
import java.util.Set;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class AudioInfo {

   private final AudioTrack track;
   private final Set<String> skips;
   private final Member author;
   private final VoiceChannel channel;

   AudioInfo(AudioTrack track, Member author, VoiceChannel channel) {
      this.track = track;
      this.skips = new HashSet<>();
      this.author = author;
      this.channel = channel;
   }

   public AudioTrack getTrack() {
      return track;
   }

   public int getSkips() {
      return skips.size();
   }

   public void addSkip(User u) {
      skips.add(u.getId());
   }

   public boolean hasVoted(User u) {
      return skips.contains(u.getId());
   }

   public Member getAuthor() {
      return author;
   }

   public VoiceChannel getChannel() {
      return channel;
   }

}
