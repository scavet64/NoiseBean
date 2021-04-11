/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import com.scavettapps.noisebean.users.NoiseBeanUserService;
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

   private final GameSessionService gameSessionService;

   @Autowired
   public GameSessionListener(
       GameSessionService gameSessionService
   ) {
      this.gameSessionService = gameSessionService;
   }

   @Override
   public void onUserActivityEnd(UserActivityEndEvent event) {
      if (event.getOldActivity().getType() == Activity.ActivityType.DEFAULT) {

         String userId = event.getUser().getId();
         String gameName = event.getOldActivity().getName();

         if (gameName.equals("SteamVR")) {
            log.info("Ignoring SteamVR end for [{}]", userId);
            return;
         }
         
         // Check to make sure that the activity didnt update its rich presence
         for (var activity : event.getMember().getActivities()) {
            if (activity.getName().equals(event.getOldActivity().getName())) {
               log.info("Same activity was detected for user: [{}] game: [{}]",
                   userId,
                   gameName
               );
               return;
            }
         }

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

         // TODO: Look into fixing this but for now just ignore steamVR since its blocking other VR games from being recorded
         if (gameName.equals("SteamVR")) {
            log.info("Ignoring SteamVR start for [{}]", userId);
            return;
         }

         // Check if a session for this game already exists. If not, start one
         if (this.gameSessionService.doesSessionExist(userId, gameName)) {
            log.info("GameSession already existed for user [{}] and game [{}]",
               userId,
               gameName
            );
         } else {
            this.gameSessionService.startNewSession(userId, gameName);
            log.info("Started GameSession for user [{}] and game [{}]",
                    userId,
                    gameName
            );
         }
      }
   }

}
