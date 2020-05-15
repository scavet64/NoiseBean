/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.listening;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Slf4j
public class AuditingListener extends ListenerAdapter {

   @Override
   public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
      super.onMessageReactionRemove(event);
      log.info(
          "Reaction [{}] was removed from messageId [{}] by [{}]", 
          event.getReactionEmote().getName(),
          event.getMessageId(),
          event.getMember().getEffectiveName()
      );
   }

   @Override
   public void onMessageReactionAdd(MessageReactionAddEvent event) {
      super.onMessageReactionAdd(event);
      log.info(
          "Reaction [{}] was added to messageId [{}] by [{}]", 
          event.getReactionEmote().getName(),
          event.getMessageId(),
          event.getMember().getEffectiveName()
      );
   }

   @Override
   public void onUserActivityEnd(UserActivityEndEvent event) {
      super.onUserActivityEnd(event);
      if (event.getOldActivity().getType() == Activity.ActivityType.DEFAULT) {
         log.info(
             "[{}] has ended [{}]",
             event.getMember().getEffectiveName(),
             event.getOldActivity().getName()
         );
      }
      
   }

   @Override
   public void onUserActivityStart(UserActivityStartEvent event) {
      super.onUserActivityStart(event);
      if (event.getNewActivity().getType() == Activity.ActivityType.DEFAULT) {
         log.info(
             "[{}] has started [{}]",
             event.getMember().getEffectiveName(),
             event.getNewActivity().getName()
         );
      }
   }

   @Override
   public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
      super.onUserUpdateOnlineStatus(event);
      log.info(
          "[{}] status is now [{}]", 
          event.getMember().getEffectiveName(),
          event.getNewOnlineStatus().toString()
      );
   }
   
}
