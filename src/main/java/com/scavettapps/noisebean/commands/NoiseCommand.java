/**
 * Copyright 2020 Vincent Scavetta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.scavettapps.noisebean.commands;

import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.music.ChatBasedAudioLoadResultHandlerImpl;
import com.scavettapps.noisebean.music.NoiseBeanAudioManager;
import com.scavettapps.noisebean.music.NoiseBeanAudioService;
import com.scavettapps.noisebean.music.TrackManager;
import com.scavettapps.noisebean.music.TrackScheduler;
import com.scavettapps.noisebean.sounds.SoundFile;
import com.scavettapps.noisebean.sounds.SoundFileNotFoundException;
import com.scavettapps.noisebean.sounds.SoundFileService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Command(name = "noise")
public class NoiseCommand extends AbstractCommand {

   private final NoiseBeanAudioManager myManager;
   private final NoiseBeanAudioService noiseBeanAudioService;
   private final SoundFileService soundFileService;

   @Autowired
   public NoiseCommand(
       NoiseBeanAudioManager myManager,
       NoiseBeanAudioService noiseBeanAudioService,
       SoundFileService soundFileService
   ) {
      this.myManager = myManager;
      this.noiseBeanAudioService = noiseBeanAudioService;
      this.soundFileService = soundFileService;
      AudioSourceManagers.registerLocalSource(myManager);
   }

   @Override
   public void executeCommand(String[] args, MessageReceivedEvent event, MessageSender chat) {

      if (args.length != 1) {
         chat.sendMessage("Incorrect number of arguments :^(");
         return;
      }

      if (!event.getMember().getVoiceState().inVoiceChannel()) {
         chat.sendMessage("You are not in a voice channel :^(");
         return;
      }

      try {
         SoundFile soundFile = this.soundFileService.getSoundFile(args[0]);

         Guild guild = event.getGuild();
         Member author = event.getMember();
         AudioPlayer player = this.noiseBeanAudioService.getPlayer(guild);
         TrackManager trackManager = this.noiseBeanAudioService.getTrackManager(guild);

         TrackScheduler trackScheduler = new TrackScheduler(player);
         player.addListener(trackScheduler);

         myManager.loadItemOrdered(event.getGuild(),
             soundFile.getFilePath(),
             new ChatBasedAudioLoadResultHandlerImpl(chat, prefix, guild, author, trackManager)
         );

      } catch (SoundFileNotFoundException ex) {
         chat.sendMessage("Could not find sound file :^(");
      }
   }

}
