/**
 * Copyright 2020 Vincent Scavetta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.scavettapps.noisebean.music;

import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.core.MessageUtil;
import static com.scavettapps.noisebean.core.Unicode.CD;
import static com.scavettapps.noisebean.core.Unicode.DVD;
import static com.scavettapps.noisebean.core.Unicode.MIC;
import static com.scavettapps.noisebean.core.Unicode.NO_ENTRY;
import static com.scavettapps.noisebean.core.Unicode.WARNING_SIGN;
import static com.scavettapps.noisebean.music.AudioConstants.QUEUE_DESCRIPTION;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 *
 * @author Vincent Scavetta.
 */
public class ChatBasedAudioLoadResultHandlerImpl implements AudioLoadResultHandler {

   private static final int PLAYLIST_LIMIT = 200;
   private static final String QUEUE_TITLE = "__%s has added %d new track%s to the Queue:__";
   private static final String ERROR = "Error while loading \"%s\"";

   private final MessageSender chat;
   private final String identifier;
   private final Guild guild;
   private final Member author;
   private final TrackManager trackManager;

   public ChatBasedAudioLoadResultHandlerImpl(MessageSender chat, String identifier, Guild guild, Member author, TrackManager trackManager) {
      this.chat = chat;
      this.identifier = identifier;
      this.guild = guild;
      this.author = author;
      this.trackManager = trackManager;
   }

   @Override
   public void trackLoaded(AudioTrack track) {
      chat.sendEmbed(
          String.format(QUEUE_TITLE, MessageUtil.userDiscrimSet(author.getUser()), 1, ""),
          String.format(
              QUEUE_DESCRIPTION,
              CD,
              getOrNull(track.getInfo().title),
              "",
              MIC,
              getOrNull(track.getInfo().author)
          )
      );
      trackManager.queue(track, author);
   }

   @Override
   public void playlistLoaded(AudioPlaylist playlist) {
      if (playlist.getSelectedTrack() != null) {
         trackLoaded(playlist.getSelectedTrack());
      } else if (playlist.isSearchResult()) {
         trackLoaded(playlist.getTracks().get(0));
      } else {
         chat.sendEmbed(
             String.format(
                 QUEUE_TITLE,
                 MessageUtil.userDiscrimSet(author.getUser()),
                 Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT),
                 "s"),
             String.format(QUEUE_DESCRIPTION, DVD, getOrNull(playlist.getName()), "", "", "", ""));
         for (int i = 0; i < Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT); i++) {
            trackManager.queue(playlist.getTracks().get(i), author);
         }
      }
   }

   @Override
   public void noMatches() {
      chat.sendEmbed(String.format(ERROR, identifier), WARNING_SIGN + " No playable tracks were found.");
   }

   @Override
   public void loadFailed(FriendlyException exception) {
      chat.sendEmbed(String.format(ERROR, identifier), NO_ENTRY + " " + exception.getLocalizedMessage());
   }
   
   private String getOrNull(String s) {
      return s.isEmpty() ? "N/A" : s;
   }
}
