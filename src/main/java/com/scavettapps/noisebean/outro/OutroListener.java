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
package com.scavettapps.noisebean.outro;

import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.intro.IntroductionListener;
import com.scavettapps.noisebean.music.NoiseBeanAudioService;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
public class OutroListener extends ListenerAdapter {

   private static final Logger LOGGER = LoggerFactory.getLogger(IntroductionListener.class);

   private final NoiseBeanAudioService noiseBeanAudioService;
   private final OutroService outroService;

   public OutroListener(
       NoiseBeanAudioService noiseBeanAudioService,
       OutroService outroService
   ) {
      this.noiseBeanAudioService = noiseBeanAudioService;
      this.outroService = outroService;
   }
   
   @Override
   public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
      
      UserOutro outro = this.outroService.getUsersOutro(event.getMember().getId());
      if (outro != null) {
         String soundPath = outro.getSoundFile().getFilePath();
         this.noiseBeanAudioService.playSound(soundPath, event.getGuild(), event.getMember());
      }
      
      LOGGER.info(
          "{} left voice chat channel ", 
          event.getMember().getEffectiveName(), 
          event.getChannelLeft().getName()
      );
      super.onGuildVoiceLeave(event);
   }
}
