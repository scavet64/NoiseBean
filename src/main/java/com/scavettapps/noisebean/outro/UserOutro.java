/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.outro;

import com.scavettapps.noisebean.sounds.SoundFile;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
public class UserOutro {
   @Id
   @Column(name = "introduction_id")
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
   
   @NotBlank
   @NotNull
   @Column(name = "user_id")
   private String userId;
   
   @NotBlank
   @NotNull
   @JoinColumn(name = "sound_file_id", referencedColumnName = "sound_file_id")
   @ManyToOne
   private SoundFile soundFile;
   
   @NotNull
   @Builder.Default
   private Instant dateCreated = Instant.now();
}
