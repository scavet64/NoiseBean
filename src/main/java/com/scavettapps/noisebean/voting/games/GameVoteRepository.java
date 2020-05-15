/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.voting.games;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Vincent Scavetta.
 */
@Repository
public interface GameVoteRepository extends JpaRepository<GameVoteOption, Long>{
   Optional<GameVoteOption> findByGameVoteName(String gameName);
   
   List<GameVoteOption> findAllByOrderByGameVoteNameAsc();
}
