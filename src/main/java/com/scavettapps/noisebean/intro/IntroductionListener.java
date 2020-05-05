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
package com.scavettapps.noisebean.intro;

import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.music.ChatBasedAudioLoadResultHandlerImpl;
import com.scavettapps.noisebean.music.LogBasedAudioLoadResultHandlerImpl;
import com.scavettapps.noisebean.music.NoiseBeanAudioManager;
import com.scavettapps.noisebean.music.NoiseBeanAudioService;
import com.scavettapps.noisebean.music.TrackManager;
import com.scavettapps.noisebean.music.TrackScheduler;
import com.scavettapps.noisebean.sounds.SoundFile;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
public class IntroductionListener extends ListenerAdapter {

   private static final Logger LOGGER = LoggerFactory.getLogger(IntroductionListener.class);

   private final IntroductionService introductionService;
   private final NoiseBeanAudioService noiseBeanAudioService;

   @Autowired
   public IntroductionListener(
       IntroductionService introductionService,
       NoiseBeanAudioService noiseBeanAudioService
   ) {
      this.introductionService = introductionService;
      this.noiseBeanAudioService = noiseBeanAudioService;
   }

   @Override
   public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
      LOGGER.info("Voice Joined");

      UserIntroduction introduction = this.introductionService.getUsersIntro(event.getMember().getId());

      if (introduction == null) {
         LOGGER.info("No intro set for user: {} - {}", event.getMember().getId(), event.getMember().getUser().getName());
         return;
      }
      
      String context = introduction.getSoundFile().getFilePath();
      Guild guild = event.getGuild();
      Member author = event.getMember();
      
      noiseBeanAudioService.playSound(context, guild, author);

      super.onGuildVoiceJoin(event);
   }

   @Override
   public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {
      LOGGER.info("Voice muted");
      super.onGuildVoiceSelfMute(event);
   }

}
