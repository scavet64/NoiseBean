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

import com.scavettapps.noisebean.sounds.SoundFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vincent Scavetta.
 */
@Service
public class IntroductionService {

   private final IntroductionRepository introductionRepository;

   @Autowired
   public IntroductionService(IntroductionRepository introductionRepository) {
      this.introductionRepository = introductionRepository;
   }

   /**
    * Returns null if the user does not have an introduction set.
    *
    * @param userId The user's ID
    * @return The Users introduction. Null if the introduction was never set.
    */
   public UserIntroduction getUsersIntro(String userId) {
      return this.introductionRepository.findByUserId(userId).orElse(null);
   }

   public UserIntroduction setUserIntro(String userId, SoundFile soundToPlay) {

      UserIntroduction intro = this.introductionRepository.findByUserId(userId).orElse(
          UserIntroduction.builder()
          .userId(userId)
          .build()
      );
      
      intro.setSoundFile(soundToPlay);
      return this.introductionRepository.save(intro);
   }
}
