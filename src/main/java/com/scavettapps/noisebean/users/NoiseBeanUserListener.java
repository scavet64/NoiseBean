/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.users;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Slf4j
public class NoiseBeanUserListener extends ListenerAdapter {

   private final NoiseBeanUserService noiseBeanUserService;

   @Autowired
   public NoiseBeanUserListener(NoiseBeanUserService noiseBeanUserService) {
      this.noiseBeanUserService = noiseBeanUserService;
   }

   @Override
   public void onGuildAvailable(GuildAvailableEvent event) {
      // Make sure all the users are accounted for.
      
   }

   @Override
   public void onGuildReady(GuildReadyEvent event) {
      for (Member member : event.getGuild().getMembers()) {
         NoiseBeanUser nbUser = this.noiseBeanUserService.getNoiseBeanUser(member.getIdLong());
         if (nbUser == null) {
            // Create it
            nbUser = NoiseBeanUser.builder()
                .id(member.getId())
                .username(member.getUser().getName())
                .build();
            nbUser = this.noiseBeanUserService.saveNoiseBeanUser(nbUser);
            log.info("Added new NoiseBeanUser ID:[{}] Username:[{}]", nbUser.getId(), nbUser.getUsername());
         } else {
            // Check to see if they changed their username
            if (!nbUser.getUsername().equalsIgnoreCase(member.getUser().getName())) {
               // Update the name
               nbUser.setUsername(member.getUser().getName());
               nbUser = this.noiseBeanUserService.saveNoiseBeanUser(nbUser);
               log.info("Updated username of [{}] to [{}]", nbUser.getId(), nbUser.getUsername());
            }
         }
      }
   }
   
   

}
