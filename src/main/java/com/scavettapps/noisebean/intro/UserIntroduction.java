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
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Vincent Scavetta.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserIntroduction {
   
   @Id
   @Column(name = "introduction_id")
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
   
   @NotBlank
   @NotNull
   @Column(name = "user_id")
   private String userId;

   @NotNull
   @JoinColumn(name = "sound_file_id", referencedColumnName = "sound_file_id")
   @ManyToOne
   private SoundFile soundFile;
   
   @Builder.Default
   private Instant dateCreated = Instant.now();
}
