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
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Command(name = "activity")
@Slf4j
@Component
public class ActivityCommand extends AbstractCommand {
   
   @Override
   public void executeCommand(String[] args, MessageReceivedEvent event, MessageSender chat) {
      StringBuilder builder = new StringBuilder();
      for(int i = 0; i < args.length; i++) {
         builder.append(args[i]);
         builder.append(" ");
      }
      
      event.getJDA().getPresence().setActivity(Activity.listening(builder.toString()));
      log.info("updated activity to: {}", builder.toString());
   }
   
}
