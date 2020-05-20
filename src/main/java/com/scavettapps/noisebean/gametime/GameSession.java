/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import com.scavettapps.noisebean.calltime.*;
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
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameSession {
   
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long gameSessionId;
   
   @NotNull
   private String userId;
   
   @NotNull
   private String gameName;
   
   @NotNull
   @Builder.Default
   private Instant sessionStarted = Instant.now();
   
   private Instant sessionEnded;
   
   private long minPlayed;
   
}

