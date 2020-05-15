/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.voting;

import com.scavettapps.noisebean.voting.games.GameVoteOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
