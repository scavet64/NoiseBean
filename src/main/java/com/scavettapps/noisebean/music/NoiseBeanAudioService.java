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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vincent Scavetta.
 */
@Service
public class NoiseBeanAudioService {
   
   private final NoiseBeanAudioManager myManager;
   private final Map<String, Map.Entry<AudioPlayer, TrackManager>> players = new HashMap<>();

   @Autowired
   public NoiseBeanAudioService(NoiseBeanAudioManager myManager) {
      this.myManager = myManager;
   }
 
   public TrackManager getTrackManager(Guild guild) {
      return players.get(guild.getId()).getValue();
   }
   
   public boolean hasPlayer(Guild guild) {
      return players.containsKey(guild.getId());
   }

   public AudioPlayer getPlayer(Guild guild) {
      AudioPlayer p;
      if (hasPlayer(guild)) {
         p = players.get(guild.getId()).getKey();
      } else {
         p = createPlayer(guild);
      }
      return p;
   }

   public AudioPlayer createPlayer(Guild guild) {
      AudioPlayer nPlayer = myManager.createPlayer();
      TrackManager manager = new TrackManager(nPlayer);
      nPlayer.addListener(manager);
      guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(nPlayer));
      players.put(guild.getId(), new AbstractMap.SimpleEntry<>(nPlayer, manager));
      return nPlayer;
   }

   public void reset(Guild guild) {
      players.remove(guild.getId());
      getPlayer(guild).destroy();
      getTrackManager(guild).purgeQueue();
      guild.getAudioManager().closeAudioConnection();
   }
   
   public void playSound(String soundPath, Guild guild, Member member) {
      AudioPlayer player = this.getPlayer(guild);
      TrackManager trackManager = this.getTrackManager(guild);

      TrackScheduler trackScheduler = new TrackScheduler(player);
      player.addListener(trackScheduler);

      myManager.loadItemOrdered(
          guild,
          soundPath,
          new LogBasedAudioLoadResultHandlerImpl(member, trackManager)
      );
   }
}
