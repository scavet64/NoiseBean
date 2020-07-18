/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import com.scavettapps.noisebean.users.NoiseBeanUser;
import com.scavettapps.noisebean.users.NoiseBeanUserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
   private final NoiseBeanUserService noiseBeanUserService;

   @Autowired
   public GameSessionService(
       GameSessionRepository activeChatterRepository,
       NoiseBeanUserService noiseBeanUserService
   ) {
      this.gameSessionRepository = activeChatterRepository;
      this.noiseBeanUserService = noiseBeanUserService;
   }
   
   public GameSession startNewSession(String userId, String gameName) {
      // If there is a existing session, end it and start a new one
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      this.gameSessionRepository.findByUserId_IdAndSessionEndedIsNull(user.getId())
          .ifPresent(session -> {
             session.setSessionEnded(Instant.now());
             this.gameSessionRepository.save(session);
          });
      
      GameSession newSession = GameSession.builder()
          .gameName(gameName)
          .userId(user)
          .build();
      
      return this.gameSessionRepository.save(newSession);
   }
   
   public GameSession endSession(String userId, String gameName) throws GameSessionDoesNotExist {
      // Get the user object
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      GameSession session = this.gameSessionRepository.findByUserId_IdAndGameNameAndSessionEndedIsNull(
          user.getId(), 
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
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      List<GameSession> sessions = this.gameSessionRepository.findAllByUserId_IdAndGameNameIgnoreCase(
          user.getId(), 
          gameName
      );
      
      long playTime = 0;
      for (GameSession session : sessions) {
         playTime += session.calculateMinPlayed();
      }
      
      return playTime;
   }
   
   public List<GamePlayTime> getPlayTimeList(String userId) {
      List<GamePlayTime> gamePlayTimeList = new ArrayList<>();
      
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      List<GameSession> sessions = this.gameSessionRepository.findAllByUserId_Id(user.getId());
      
      for (GameSession session : sessions) {
         var playtime = gamePlayTimeList.stream()
             .filter(existingPlaytime -> existingPlaytime.getGameName().equals(session.getGameName()))
             .findFirst()
             .orElseGet(() -> createAndSaveNewPlayTime(session.getGameName(), gamePlayTimeList));
         
         playtime.addPlayTime(session.calculateMinPlayed());
      }
      
      return gamePlayTimeList;
   }
   
   private GamePlayTime createAndSaveNewPlayTime(String gameName, List<GamePlayTime> list) {
      var newGameTime = new GamePlayTime(gameName);
      list.add(newGameTime);
      return newGameTime;
   } 
}
