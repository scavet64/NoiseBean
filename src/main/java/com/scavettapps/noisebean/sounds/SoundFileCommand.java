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
package com.scavettapps.noisebean.sounds;

import com.scavettapps.noisebean.commands.AbstractCommand;
import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.core.MessageUtil;
import java.util.Collection;
import java.util.List;

import com.scavettapps.noisebean.music.NoiseBeanAudioService;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Command(name = "sound", description = "Manage the sounds that the bot can play")
@Log4j2
public class SoundFileCommand extends AbstractCommand {

   private final NoiseBeanAudioService noiseBeanAudioService;
   private final SoundFileService soundFileService;

   @Autowired
   public SoundFileCommand(
           SoundFileService soundFileService,
           NoiseBeanAudioService noiseBeanAudioService
   ) {
      this.noiseBeanAudioService = noiseBeanAudioService;
      this.soundFileService = soundFileService;
   }

   @Override
   public void executeCommand(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length == 0) {
         sendHelpMessage(chat);
         return;
      }

      switch (args[0]) {
         case "add":
            addSound(args, event, chat);
            break;
         case "remove":
            removeSound(args, event, chat);
            break;
         case "rename":
            renameSound(args, event, chat);
            break;
         case "list":
            listSounds(args, event, chat);
            break;
         case "play":
            playSound(args, event, chat);
            break;
         default:
            break;
      }
   }

   private void sendHelpMessage(MessageSender chat) {
      chat.sendEmbed("DJNoiseBeans",
          MessageUtil.stripFormatting(this.prefix) + "sound\n"
          + "         -> add [name]           - Add a new sound. Attach the sound you want to add to your message.\n"
          + "         -> remove [name]  - Remove a sound\n"
          + "         -> rename [name] [newname]  - Rename a sound to another unique name\n"
          + "         -> list                     - Show a list of all sounds\n");
   }

   private void addSound(String[] args, MessageReceivedEvent event, MessageSender chat) {

      if (args.length != 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }

      List<Attachment> attachments = event.getMessage().getAttachments();
      if (attachments.isEmpty()) {
         chat.sendMessage("No file was attached :^(");
         return;
      }

      String soundName = args[1];

      // Check if already exists
      if (this.soundFileService.doesSoundExist(soundName)) {
         chat.sendMessage("Sound already exists with that name :^(");
         return;
      }
      
      try {
         this.soundFileService.saveSoundFile(soundName, attachments.get(0));
         chat.sendMessage("Successfully added: " + args[1]);
         log.info("New sound file [{}] was added by [{}]", args[1], event.getAuthor().getName());
      } catch (SoundFileDownloadException ex) {
         log.error("Download file error");
         chat.sendMessage("Failed to download the sound file. Please try again later :^(");
      }
   }
   
   private void removeSound(String[] args, MessageReceivedEvent event, MessageSender chat) {

      if (args.length != 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }
      
      String soundName = args[1];
      
      try {
         boolean deleted = this.soundFileService.deleteSoundFile(soundName);
         if (deleted) {
            chat.sendMessage("Successfully removed: " + soundName);
            log.info("User [{}] deleted sound file [{}]", event.getAuthor().getName(), soundName);
         } else {
            chat.sendMessage("Could not delete sound file [" + soundName + "] from file system. Contact the DJNoiseBeans admin :^(");
            log.info("User [{}] could not delete sound file [{}] from file system.", event.getAuthor().getName(), soundName);
         }
      } catch (SoundFileNotFoundException ex) {
         log.warn("Sound file [{}] did not exist", soundName);
         chat.sendMessage("Could not find sound with that name :^(");
      }
   }
   
   private void renameSound(String[] args, MessageReceivedEvent event, MessageSender chat) {

      if (args.length != 3) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }
      
      String soundName = args[1];
      String newSoundName = args[2];
      
      try {
         this.soundFileService.renameSoundFile(soundName, newSoundName);
         chat.sendMessage("Successfully renamed: " + soundName + " to: " + newSoundName);
         log.info("User [{}] renamed sound file [{}]", event.getAuthor().getName(), soundName);
      } catch (SoundFileAlreadyExistsException  ex) {
         log.warn(ex.getLocalizedMessage());
         chat.sendMessage(ex.getLocalizedMessage());
      } catch (SoundFileNotFoundException  ex) {
         log.warn("Sound file [{}] did not exist file error", soundName);
         chat.sendMessage("Could not find sound with that name :^(");
      }
   }

   private void listSounds(String[] args, MessageReceivedEvent event, MessageSender chat) {
      Collection<SoundFile> sounds = this.soundFileService.getAllSoundFiles();

      if (sounds.isEmpty()) {
         chat.sendMessage("No sound files :^(");
         return;
      }

      StringBuilder strB = new StringBuilder();

      for (SoundFile sf : sounds) {
         strB.append(sf.getSoundFileName());
         strB.append("\n");
      }

      chat.sendMessage(strB.toString());
   }
   
   private void playSound(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length != 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }
      
      String soundName = args[1];
      
      try {
         SoundFile sound = this.soundFileService.getSoundFile(soundName);

         this.noiseBeanAudioService.playSound(sound.getFilePath(), event.getGuild(), event.getMember());

         chat.sendMessage("Added to Queue: " + soundName);
         log.info("User [{}] added sound file [{}] to the queue", event.getAuthor().getName(), soundName);

      } catch (SoundFileNotFoundException ex) {
         log.warn("Sound file [{}] did not exist", soundName);
         chat.sendMessage("Could not find sound with that name :^(");
      }
   }

}
