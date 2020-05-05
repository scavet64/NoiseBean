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
import net.dv8tion.jda.api.entities.Guild;
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
@Command(name = "sound")
public class SoundFileCommand extends AbstractCommand {

   private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SoundFileCommand.class);
   
   private SoundFileService soundFileService;

   @Autowired
   public SoundFileCommand(SoundFileService soundFileService) {
      this.soundFileService = soundFileService;
   }

   @Override
   public void executeCommand(String[] args, MessageReceivedEvent event, MessageSender chat) {
      Guild guild = event.getGuild();

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
      
      try {
         this.soundFileService.saveSoundFile(args[1], attachments.get(0));
         chat.sendMessage("Successfully added: " + args[1]);
         LOGGER.info("New sound file [{}] was added by [{}]", args[1], event.getAuthor().getName());
      } catch (SoundFileDownloadException ex) {
         LOGGER.error("Download file error");
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
            LOGGER.info("User [] deleted sound file [{}]", event.getAuthor().getName(), soundName);
         } else {
            chat.sendMessage("Could not delete sound file [" + soundName + "] from file system. Contact the DJNoiseBeans admin :^(");
            LOGGER.info("User [] could not delete sound file [{}] from file system.", event.getAuthor().getName(), soundName);
         }
      } catch (SoundFileNotFoundException ex) {
         LOGGER.warn("Sound file [{}] did not exist", soundName);
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
         LOGGER.info("User [] deleted sound file [{}]", event.getAuthor().getName(), soundName);
      } catch (SoundFileAlreadyExistsException  ex) {
         LOGGER.warn(ex.getLocalizedMessage());
         chat.sendMessage(ex.getLocalizedMessage());
      } catch (SoundFileNotFoundException  ex) {
         LOGGER.warn("Sound file [{}] did not exist file error", soundName);
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

         
         
         chat.sendMessage("Successfully removed: " + soundName);
         LOGGER.info("User [] deleted sound file [{}]", event.getAuthor().getName(), soundName);

      } catch (SoundFileNotFoundException ex) {
         LOGGER.warn("Sound file [{}] did not exist", soundName);
         chat.sendMessage("Could not find sound with that name :^(");
      }
   }

}
