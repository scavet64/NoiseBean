/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Slf4j
public class GameSessionListener extends ListenerAdapter {
   private final ArrayList<String> ignoredGames = new ArrayList<String>() {
      {
         add("SteamVR");
         add("Rockstar Games Launcher");
      }
   };

   private final GameSessionService gameSessionService;

   public GameSessionListener(GameSessionService gameSessionService) {
      this.gameSessionService = gameSessionService;
   }

   private boolean isGameIgnored(String gameName) {
      return ignoredGames.stream().anyMatch(x -> x.equals(gameName));
   }

   private boolean isActivityIgnored(String gameName, User user) {
      var userId = user.getId();
      var username = user.getName();

      if (user.isBot()) {
         log.debug("Ignoring bot's activity [{} - {}]", userId, username);
         return true;
      }

      if (isGameIgnored(gameName)) {
         log.info("Ignoring [{}] activity change for [{} - {}]", gameName, userId, username);
         return true;
      }

      return false;
   }

   @Override
   public void onUserActivityEnd(@Nonnull UserActivityEndEvent event) {
      if (event.getOldActivity().getType() == Activity.ActivityType.DEFAULT) {

         var userId = event.getUser().getId();
         var username = event.getUser().getName();

         var oldActivity = event.getOldActivity();
         var gameName = oldActivity.getName();

         if (isActivityIgnored(gameName, event.getUser()))
            return;

         // Check to make sure that the activity didn't update its rich presence
         for (var activity : event.getMember().getActivities()) {
            if (activity.getName().equals(gameName)) {
               log.info(
                     "Same activity was detected for user: [{} - {}] game: [{}]",
                     userId,
                     username,
                     gameName);
               return;
            }
         }

         try {
            // Does a session exist for this game?
            if (this.gameSessionService.doesSessionExist(userId, gameName)) {
               this.gameSessionService.endSession(userId, gameName);
            } else {
               // Attempt to create the session manually using information from discord
               log.info("Attempting to retroactively record GameSession for user [{} - {}] and game [{}]", userId, username, gameName);
               var timestamps = oldActivity.getTimestamps();
               if (timestamps != null) {
                  this.gameSessionService.startNewSession(userId, gameName, timestamps.getStartTime());
                  this.gameSessionService.endSession(userId, gameName);
               }
            }

         } catch (Exception ex) {
            log.error("Failed to record ending GameSession for user [{} - {}] and game [{}]", userId, username, gameName);
         }
      }
   }

   @Override
   public void onUserActivityStart(@Nonnull UserActivityStartEvent event) {
      if (event.getNewActivity().getType() == Activity.ActivityType.DEFAULT) {

         var userId = event.getUser().getId();
         var username = event.getUser().getName();

         var activity = event.getNewActivity();
         var gameName = activity.getName();

         if (isActivityIgnored(gameName, event.getUser()))
            return;

         // Check if a session for this game already exists. If not, start one
         if (this.gameSessionService.doesSessionExist(userId, gameName)) {
            log.info("Active GameSession already existed for user [{} - {}] and game [{}]", userId, username, gameName);
         } else {
            this.gameSessionService.startNewSession(userId, gameName);
            log.info("Started GameSession for user [{} - {}] and game [{}]", userId, username, gameName);
         }
      }
   }

}
