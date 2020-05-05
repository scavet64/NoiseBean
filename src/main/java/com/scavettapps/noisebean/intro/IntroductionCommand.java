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
package com.scavettapps.noisebean.intro;

import com.scavettapps.noisebean.commands.AbstractCommand;
import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.core.MessageSender;
import com.scavettapps.noisebean.core.MessageUtil;
import com.scavettapps.noisebean.sounds.SoundFile;
import com.scavettapps.noisebean.sounds.SoundFileNotFoundException;
import com.scavettapps.noisebean.sounds.SoundFileService;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Command(name = "intro")
public class IntroductionCommand extends AbstractCommand {
   
   private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IntroductionCommand.class);
   
   private final IntroductionService introductionService;
   private final SoundFileService soundFileService;

   @Autowired
   public IntroductionCommand(
       IntroductionService introductionService,
       SoundFileService soundFileService
   ) {
      this.introductionService = introductionService;
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
         case "set":
            setIntroduction(args, event, chat);
            break;
         case "remove":
            break;
         case "current":
            getCurrentIntro(args, event, chat);
            break;
         default:
            break;
      }
   }

   private void sendHelpMessage(MessageSender chat) {
      chat.sendEmbed("DJNoiseBeans",
          MessageUtil.stripFormatting(this.prefix) + "intro\n"
          + "         -> set [name]           - Set the name of the sound that should be played as you enter a voice channel\n"
          + "         -> remove  - Remove your custom intro if you have one set\n");
   }

   private void setIntroduction(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length != 2) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }
      
      try {
         SoundFile soundfile = this.soundFileService.getSoundFile(args[1]);
         this.introductionService.setUserIntro(event.getAuthor().getId(), soundfile);
         chat.sendMessage("I will now play this sound for you when you join");
      } catch (SoundFileNotFoundException ex) {
         chat.sendMessage("Sound does not exist :^(");
      }
   }
   
   private void getCurrentIntro(String[] args, MessageReceivedEvent event, MessageSender chat) {
      if (args.length != 1) {
         chat.sendMessage("Incorrect number of parameters.");
         return;
      }

      UserIntroduction intro = this.introductionService.getUsersIntro(event.getAuthor().getId());
      if (intro != null) {
         chat.sendMessage("Currently I will play this sound for you: " + intro.getSoundFile().getSoundFileName());
      } else {
         chat.sendMessage("Currently you do not have an intro set");
      }
   }

}
