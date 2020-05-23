/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vincent Scavetta.
 */
@Service
@Slf4j
public class GameSessionService {
   
   private final GameSessionRepository gameSessionRepository;

   @Autowired
   public GameSessionService(GameSessionRepository activeChatterRepository) {
      this.gameSessionRepository = activeChatterRepository;
   }
   
   public GameSession startNewSession(String userId, String gameName) {
      // If there is a existing session, end it and start a new one
      this.gameSessionRepository.findByUserIdAndSessionEndedIsNull(userId)
          .ifPresent(session -> {
             session.setSessionEnded(Instant.now());
             this.gameSessionRepository.save(session);
          });
      
      GameSession newSession = GameSession.builder()
          .gameName(gameName)
          .userId(userId)
          .build();
      
      return this.gameSessionRepository.save(newSession);
   }
   
   public GameSession endSession(String userId, String gameName) throws GameSessionDoesNotExist {
      
      GameSession session = this.gameSessionRepository.findByUserIdAndGameNameAndSessionEndedIsNull(
          userId, 
          gameName
      ).orElseThrow(() -> new GameSessionDoesNotExist());
      
      session.setSessionEnded(Instant.now());
      
      Instant started = session.getSessionStarted();
      Instant ended = session.getSessionEnded();
      session.setMinPlayed(ChronoUnit.MINUTES.between(started, ended));
      
      log.info("Ending session of [{}] for [{}] min by [{}]", 
          gameName, 
          session.getMinPlayed(), 
          userId
      );
      
      return this.gameSessionRepository.save(session);
   }
   
   public long getPlaytime(String userId, String gameName) {
      List<GameSession> sessions = this.gameSessionRepository.findAllByUserIdAndGameNameIgnoreCase(
          userId, 
          gameName
      );
      
      long playTime = 0;
      for (GameSession session : sessions) {
         playTime += session.calculateMinPlayed();
      }
      
      return playTime;
   }
   
   public Map<String, Long> getPlayTimeList(String userId) {
      Map<String, Long> gameToPlayTime = new HashMap<>();
      List<GameSession> sessions = this.gameSessionRepository.findAllByUserId(userId);
      
      for (GameSession session : sessions) {
         String gameName = session.getGameName();
         if (!gameToPlayTime.containsKey(gameName)) {
            gameToPlayTime.put(gameName, 0L);
         }
         
         Long previous = gameToPlayTime.get(gameName);
         gameToPlayTime.put(gameName, previous + session.calculateMinPlayed());
      }
      
      return gameToPlayTime;
   }
   
}
