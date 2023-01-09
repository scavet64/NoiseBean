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

import com.scavettapps.noisebean.sounds.SoundFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vincent Scavetta.
 */
@Service
public class OutroService {

   private final OutroRepository outroRepository;

   @Autowired
   public OutroService(OutroRepository introductionRepository) {
      this.outroRepository = introductionRepository;
   }

   /**
    * Returns null if the user does not have an introduction set.
    *
    * @param userId The user's ID
    * @return The Users introduction. Null if the introduction was never set.
    */
   public UserOutro getUsersOutro(String userId) {
      return this.outroRepository.findByUserId(userId).orElse(null);
   }

   public UserOutro setUserOutro(String userId, SoundFile soundToPlay) {

      UserOutro outro = this.outroRepository.findByUserId(userId).orElse(
          UserOutro.builder()
          .userId(userId)
          .build()
      );
      
      outro.setSoundFile(soundToPlay);
      return this.outroRepository.save(outro);
   }
}
