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

import com.scavettapps.noisebean.users.NoiseBeanUser;
import com.scavettapps.noisebean.users.NoiseBeanUserService;
import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vincent Scavetta.
 */
@Service
@Slf4j
public class VoiceSessionService {

   private final VoiceSessionRepository voiceSessionRepository;

   public VoiceSessionService(
         VoiceSessionRepository voiceSessionRepository,
         NoiseBeanUserService noiseBeanUserService) {
      this.voiceSessionRepository = voiceSessionRepository;
   }

   /**
    * Returns null if the user does not have an introduction set.
    *
    * @param userId The user's ID
    * @return The Users introduction. Null if the introduction was never set.
    */
   public VoiceSession GetCurrentSession(String userId) {
      return this.voiceSessionRepository
            .findByUserId_IdAndSessionEndedIsNull(userId)
            .orElse(null);
   }

   public VoiceSession startNewSession(NoiseBeanUser noiseBeanUser, String channelName) {
      VoiceSession newSession = VoiceSession.builder()
            .channelName(channelName)
            .userId(noiseBeanUser)
            .build();

      log.info("Starting VoiceSession for user [{} - {}] in channel [{}]",
            noiseBeanUser.getId(),
            noiseBeanUser.getUsername(),
            channelName);
      return this.voiceSessionRepository.save(newSession);
   }

   public VoiceSession endSession(NoiseBeanUser noiseBeanUser) throws VoiceSessionDoesNotExist {
      VoiceSession session = this.voiceSessionRepository.findByUserId_IdAndSessionEndedIsNull(
            noiseBeanUser.getId())
            .orElseThrow(() -> new VoiceSessionDoesNotExist());

      session.setSessionEnded(Instant.now());

      Instant started = session.getSessionStarted();
      Instant ended = session.getSessionEnded();
      session.setLength(ChronoUnit.MINUTES.between(started, ended));

      log.info("Ending VoiceSession for user [{} - {}] in channel [{}] for [{}] min",
            noiseBeanUser.getId(),
            noiseBeanUser.getUsername(),
            session.getChannelName(),
            session.getLength());

      return this.voiceSessionRepository.save(session);
   }

   public boolean doesSessionExist(NoiseBeanUser noiseBeanUser) {
      return this.voiceSessionRepository.findByUserId_IdAndSessionEndedIsNull(noiseBeanUser.getId()).isPresent();
   }
}
