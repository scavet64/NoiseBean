/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.voting.games;

import com.scavettapps.noisebean.voting.VoteOptionDoesNotExistException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vincent Scavetta.
 */
@Service
public class GameVoteService {

   private final GameVoteRepository gameVoteRepository;

   @Autowired
   public GameVoteService(GameVoteRepository gameVoteRepository) {
      this.gameVoteRepository = gameVoteRepository;
   }

   public List<GameVoteOption> getGameVoteOptions() {
      return this.gameVoteRepository.findAll();
   }
   
   public List<GameVoteOption> getGameVoteOptionsOrdered() {
      return this.gameVoteRepository.findAllByOrderByGameVoteNameAsc();
   }

   public GameVoteOption getGameOption(String optionName) throws VoteOptionDoesNotExistException {
      return this.gameVoteRepository.findByGameVoteName(optionName).orElseThrow(
          () -> new VoteOptionDoesNotExistException()
      );
   }

   public GameVoteOption saveGameVoteOption(GameVoteOption gameVoteOption) {
      return this.gameVoteRepository.save(gameVoteOption);
   }

   public GameVoteOption saveGameVoteOption(String optionName) {

      GameVoteOption option = this.gameVoteRepository.findByGameVoteName(optionName)
          .orElse(
              GameVoteOption.builder().gameVoteName(optionName).build()
          );

      return this.gameVoteRepository.save(option);
   }
   
   public void removeGameVoteOption(String optionName) throws VoteOptionDoesNotExistException {
      GameVoteOption option = this.getGameOption(optionName);
      this.gameVoteRepository.delete(option);
   }

}
