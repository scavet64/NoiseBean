/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.voting.games;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class GameVoteOption {
   
   @Id
   @Column(name = "game_vote_option_id")
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
   
   @Column(name = "game_vote_name")
   private String gameVoteName;
}
