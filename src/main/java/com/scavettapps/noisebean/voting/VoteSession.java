/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.voting;

import com.scavettapps.noisebean.voting.games.GameVoteOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

/**
 *
 * @author Vincent Scavetta.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Slf4j
public class VoteSession {
   
   @Builder.Default
   private HashSet<Member> voters = new HashSet<>();
   
   @Builder.Default
   private List<Ballot> ballots = new ArrayList<>();
   
   private Message initialMessage;
   
   private int numberOfVotersForSession;
   
   private boolean isRunning;
   
   private List<GameVoteOption> options;
   
   private int current;
   
   public boolean hasVoter(long voterId) {
      for(Member member: voters) {
         if (member.getIdLong() == voterId) {
            return true;
         }
      }
      return false;
   }
   
   public GameVoteOption getCurrentOption() {
      if (options.size() > current) {
         return options.get(current);
      } else {
         return null;
      }
   }
   
   public boolean isElectionOver() {
      return this.options.size() == this.ballots.size();
   }
   
   public void incrementCurrent() {
      this.current++;
   }
   
   public boolean addVote(Vote vote) {
      // Is there a current ballot
      Ballot currentBallot;
      if (this.ballots.size() <= this.current) {
         currentBallot = Ballot.builder()
             .option(this.getCurrentOption())
             .build();
         this.ballots.add(currentBallot);
      } else {
         currentBallot = this.ballots.get(current);
      }
      currentBallot.getVotes().add(vote);
      
      if (currentBallot.getVotes().size() == numberOfVotersForSession){
         this.incrementCurrent();
         return true;
      } else {
         return false;
      }
   }
   
   public List<GameVoteOption> getResults() {
      Random rng = new Random();
      List<GameVoteOption> potentialWinners = new ArrayList<>();
      
      for(Ballot ballot: this.ballots) {
         try { 
            int gameWeight = 0;
            for (Vote vote : ballot.getVotes()) {
               if (vote.getVoteValue() == 0) {
                  // Skip votes with a zero
                  throw new ZeroVoteException();
               } else {
                  gameWeight += vote.getVoteValue();
               }
            }
            for (int i = 0; i < gameWeight; i++) {
               potentialWinners.add(ballot.getOption());
            }
         } catch (ZeroVoteException ex) {
            log.info("Ignoring votes for game [{}], vote of 0 detected", ballot.getOption().getGameVoteName());
         }
      }
      
      // Get the top 5 games now
      List<GameVoteOption> winners = new ArrayList<>();
      for (int i = 0; i < 5 && potentialWinners.size() > 0; i++) {
         GameVoteOption winner = potentialWinners.get(rng.nextInt(potentialWinners.size()));
         winners.add(winner);
         
         // remove all instances of this option now that it won.
         potentialWinners.removeIf(option -> option.getGameVoteName().equals(winner.getGameVoteName()));
      }
      
      return winners;
   }
   
}
