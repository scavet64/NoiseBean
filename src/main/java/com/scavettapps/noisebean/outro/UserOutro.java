/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.outro;

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
public class UserOutro {
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
   
   @NotNull
   @Builder.Default
   private Instant dateCreated = Instant.now();
}
