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
package com.scavettapps.noisebean.sounds;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Vincent Scavetta.
 */
@Entity
@Data
public class SoundFile {
   @Id
   @Column(name = "sound_file_id")
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
   
   @NotBlank
   @NotNull
   @Column(name = "sound_name", unique = true)
   private String soundFileName;
   
   @NotBlank
   @NotNull
   @Column(name = "file_path")
   private String filePath;
   
   @NotNull
   @Builder.Default
   private Instant dateCreated = Instant.now();

   public SoundFile() {
   }

   public SoundFile(String soundFileName, String filePath) {
      this.soundFileName = soundFileName;
      this.filePath = filePath;
   }
}
