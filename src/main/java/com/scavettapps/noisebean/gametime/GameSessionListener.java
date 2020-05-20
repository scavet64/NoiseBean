/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Slf4j
public class GameSessionListener extends ListenerAdapter {

   private GameSessionService gameSessionService;

   @Autowired
   public GameSessionListener(GameSessionService gameSessionService) {
      this.gameSessionService = gameSessionService;
   }

   @Override
   public void onUserActivityEnd(UserActivityEndEvent event) {
      if (event.getOldActivity().getType() == Activity.ActivityType.DEFAULT) {
         String userId = event.getUser().getId();
         String gameName = event.getOldActivity().getName();
         try {
            this.gameSessionService.endSession(userId, gameName);
         } catch (GameSessionDoesNotExist ex) {
            log.error("Could not find GameSession for user [{}] and game [{}]",
                userId,
                gameName
            );
         }
      }
   }

   @Override
   public void onUserActivityStart(UserActivityStartEvent event) {
      if (event.getNewActivity().getType() == Activity.ActivityType.DEFAULT) {
         String userId = event.getUser().getId();
         String gameName = event.getNewActivity().getName();

         this.gameSessionService.startNewSession(userId, gameName);
         log.info("Started GameSession for user [{}] and game [{}]",
             userId,
             gameName
         );

      }
   }

}
