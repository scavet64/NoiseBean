/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import com.scavettapps.noisebean.users.NoiseBeanUser;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
   
   public long calculateTimePlayed(TemporalUnit unit) {
      if (sessionEnded == null) {
         return -1;
      } else {
         return unit.between(sessionStarted, sessionEnded);
      }
   }
   
}

