/**
 * Copyright 2020 Vincent Scavetta
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.scavettapps.noisebean.voicesession;

import com.scavettapps.noisebean.users.NoiseBeanUserService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Slf4j
public class VoiceSessionListener extends ListenerAdapter {

   // private static final Logger LOGGER = LoggerFactory.getLogger(VoiceSessionListener.class);

   private final VoiceSessionService voiceSessionService;
   private final NoiseBeanUserService noiseBeanUserService;

   public VoiceSessionListener(
         VoiceSessionService introductionService,
         NoiseBeanUserService noiseBeanUserService) {
      this.voiceSessionService = introductionService;
      this.noiseBeanUserService = noiseBeanUserService;
   }

   @Override
   public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
      log.info("{} Joined Voice", event.getMember().getEffectiveName());

      var noiseBeanUser = noiseBeanUserService.getNoiseBeanUser(event.getMember().getId());
      if (noiseBeanUser == null) {
         log.error("Could not find noiseBeanUser with id {}", event.getMember().getId());
      } else {
         // Check if a voice session for this user already exists. If not, start one
         if (this.voiceSessionService.doesSessionExist(noiseBeanUser)) {
            log.info("Active VoiceSession already existed for user [{} - {}]",
                  noiseBeanUser.getId(),
                  noiseBeanUser.getUsername());
         } else {
            this.voiceSessionService.startNewSession(noiseBeanUser, event.getChannelJoined().getName());
         }
      }

      super.onGuildVoiceJoin(event);
   }

   @Override
   public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {

      var noiseBeanUser = noiseBeanUserService.getNoiseBeanUser(event.getMember().getId());

      try {
         // Does a session exist for this game?
         if (this.voiceSessionService.doesSessionExist(noiseBeanUser)) {
            this.voiceSessionService.endSession(noiseBeanUser);
         } else {
            log.info("User left a voice channel but no active voice session was found for user [{} - {}]",
                  noiseBeanUser.getId(),
                  noiseBeanUser.getUsername());
         }

      } catch (Exception ex) {
         log.error("Failed to record ending VoiceSession for user [{} - {}]",
               noiseBeanUser.getId(),
               noiseBeanUser.getUsername());
      }

      log.info(
            "{} left voice chat channel ",
            event.getMember().getEffectiveName(),
            event.getChannelLeft().getName());
      super.onGuildVoiceLeave(event);
   }
}
