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
import org.jetbrains.annotations.NotNull;
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

   public GameSession startNewSession(String userId, String gameName, Instant startTime) {
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
         .sessionStarted(startTime)
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

   public boolean doesSessionExist(String userId, String gameName) {
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      return this.gameSessionRepository.findByUserId_IdAndSessionEndedIsNull(user.getId()).isPresent();
   }
   
   public GamePlayTime getPlaytime(String userId, String gameName) {
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      List<GameSession> sessions = this.gameSessionRepository.findAllByUserId_IdAndGameNameIgnoreCase(
          user.getId(), 
          gameName
      );
      
      var gamePlayTime = new GamePlayTime(gameName);
      for (GameSession session : sessions) {
         gamePlayTime.addPlayTime(session.calculateTimePlayed(ChronoUnit.MILLIS));
      }
      
      return gamePlayTime;
   }
   
   public List<GamePlayTime> getPlayTimeList(String userId) {
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      List<GameSession> sessions = this.gameSessionRepository.findAllByUserId_Id(user.getId());

      return buildGamePlayTimes(sessions);
   }

   public List<GamePlayTime> getPlayTimeList(String userId, Instant since) {
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      List<GameSession> sessions = this.gameSessionRepository.findAllByUserId_IdAndSessionStartedAfter(user.getId(), since);

      return buildGamePlayTimes(sessions);
   }

   public List<GamePlayTime> getPlayTimeList(String userId, int top) {
      NoiseBeanUser user = this.noiseBeanUserService.getNoiseBeanUser(userId);
      List<GameSession> sessions = this.gameSessionRepository.findAllByUserId_Id(user.getId());

      List<GamePlayTime> playtimes =  buildGamePlayTimes(sessions);
      playtimes.sort(GamePlayTime.PlayTimeDesc);

      List<GamePlayTime> limitedList = new ArrayList<>();
      for (int i = 0; i < top && i < playtimes.size(); i++) {
         limitedList.add(playtimes.get(i));
      }
      return limitedList;
   }

   @NotNull
   private List<GamePlayTime> buildGamePlayTimes(List<GameSession> sessions) {
      List<GamePlayTime> gamePlayTimeList = new ArrayList<>();
      for (GameSession session : sessions) {
         var playtime = gamePlayTimeList.stream()
            .filter(existingPlaytime -> existingPlaytime.getGameName().equals(session.getGameName()))
            .findFirst()
            .orElseGet(() -> createAndSaveNewPlayTime(session.getGameName(), gamePlayTimeList));

         playtime.addPlayTime(session.calculateTimePlayed(ChronoUnit.MILLIS));
      }
      return gamePlayTimeList;
   }
   
   private GamePlayTime createAndSaveNewPlayTime(String gameName, List<GamePlayTime> list) {
      var newGameTime = new GamePlayTime(gameName);
      list.add(newGameTime);
      return newGameTime;
   } 
}
