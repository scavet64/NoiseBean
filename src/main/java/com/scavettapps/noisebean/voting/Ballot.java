/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.voting;

import com.scavettapps.noisebean.voting.games.GameVoteOption;
import java.util.HashSet;
import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Vincent Scavetta.
 */
@Data
@Builder
public class Ballot {
   @Builder.Default
   private HashSet<Vote> votes = new HashSet<>();

   private GameVoteOption option;
}
