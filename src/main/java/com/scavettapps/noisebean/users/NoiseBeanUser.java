/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.users;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoiseBeanUser {
   
   @Id
   @Column(name = "noisebean_user_id")
   private String id;
   
   @NotBlank
   @NotNull
   private String username;
   
   @NotNull
   @Builder.Default
   private Instant dateAdded = Instant.now();
   
   @NotNull
   @Builder.Default
   private Instant dateModified = Instant.now();
   
   public Long getIdLong() {
      return Long.parseLong(id);
   }
   
}
