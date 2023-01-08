/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.listening;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.annotation.Nonnull;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Slf4j
public class AuditingListener extends ListenerAdapter {

   @Override
   public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
      super.onMessageReactionRemove(event);
      log.info(
            "Reaction [{}] was removed from messageId [{}] by [{}]",
            event.getReactionEmote().getName(),
            event.getMessageId(),
            event.getMember().getEffectiveName());
   }

   @Override
   public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
      super.onMessageReactionAdd(event);
      log.info(
            "Reaction [{}] was added to messageId [{}] by [{}]",
            event.getReactionEmote().getName(),
            event.getMessageId(),
            event.getMember().getEffectiveName());
   }

   @Override
   public void onUserUpdateOnlineStatus(@Nonnull UserUpdateOnlineStatusEvent event) {
      super.onUserUpdateOnlineStatus(event);
      log.info(
            "[{}] status is now [{}]",
            event.getMember().getEffectiveName(),
            event.getNewOnlineStatus().toString());
   }

   @Override
   public void onSlashCommand(SlashCommandEvent event) {
      if (!event.getName().equals("ping")) {
         return; // make sure we handle the right command
      }
      long time = System.currentTimeMillis();
      event.reply("Pong!").setEphemeral(true) // reply or acknowledge
            .flatMap(v -> event.getHook().editOriginalFormat("Pong: %d ms",
                  System.currentTimeMillis() - time) // then edit original
            ).queue(); // Queue both reply and edit
   }

}
