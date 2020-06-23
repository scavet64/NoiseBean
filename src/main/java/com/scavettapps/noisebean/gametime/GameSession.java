/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import com.scavettapps.noisebean.calltime.*;
import com.scavettapps.noisebean.users.NoiseBeanUser;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameSession {
   
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long gameSessionId;
   
   @NotNull
   @ManyToOne
   @JoinColumn(columnDefinition = "user_id", referencedColumnName = "noisebean_user_id", name = "user_id")
   private NoiseBeanUser userId;
   
   @NotNull
   private String gameName;
   
   @NotNull
   @Builder.Default
   private Instant sessionStarted = Instant.now();
   
   private Instant sessionEnded;
   
   @Builder.Default
   private long minPlayed = -1;
   
   public long calculateMinPlayed() {
      if (sessionEnded == null) {
         return -1;
      } else {
         return ChronoUnit.MINUTES.between(sessionStarted, sessionEnded);
      }
   }
   
}
