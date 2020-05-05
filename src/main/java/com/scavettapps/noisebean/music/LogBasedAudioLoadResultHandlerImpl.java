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

import static com.scavettapps.noisebean.core.Unicode.CD;
import static com.scavettapps.noisebean.core.Unicode.DVD;
import static com.scavettapps.noisebean.core.Unicode.MIC;
import static com.scavettapps.noisebean.music.AudioConstants.QUEUE_DESCRIPTION;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vincent Scavetta.
 */
public class LogBasedAudioLoadResultHandlerImpl implements AudioLoadResultHandler {
   
   private static final Logger LOGGER = LoggerFactory.getLogger(LogBasedAudioLoadResultHandlerImpl.class);

   private static final int PLAYLIST_LIMIT = 200;

   private final Member author;
   private final TrackManager trackManager;

   public LogBasedAudioLoadResultHandlerImpl(Member author, TrackManager trackManager) {
      this.author = author;
      this.trackManager = trackManager;
   }

   @Override
   public void trackLoaded(AudioTrack track) {
      LOGGER.info(
          String.format(
              QUEUE_DESCRIPTION,
              CD,
              getOrNull(track.getInfo().title),
              "",
              MIC,
              getOrNull(track.getInfo().author),
              ""
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
         LOGGER.info(        
             String.format(QUEUE_DESCRIPTION, DVD, getOrNull(playlist.getName()), "", "", "", "")
         );
         for (int i = 0; i < Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT); i++) {
            trackManager.queue(playlist.getTracks().get(i), author);
         }
      }
   }

   @Override
   public void noMatches() {
      LOGGER.warn("No playable tracks were found.");
   }

   @Override
   public void loadFailed(FriendlyException exception) {
      LOGGER.error(exception.getLocalizedMessage());
   }
   
   private String getOrNull(String s) {
      return s.isEmpty() ? "N/A" : s;
   }
   
}
