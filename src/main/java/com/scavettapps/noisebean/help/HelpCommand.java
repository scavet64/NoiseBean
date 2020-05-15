/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.help;

import com.scavettapps.noisebean.commands.AbstractCommand;
import com.scavettapps.noisebean.commands.Command;
import com.scavettapps.noisebean.core.MessageSender;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
@Command(name = "help", description = "Get a brief description about the available commands")
@Slf4j
public class HelpCommand extends AbstractCommand {
   
   @Autowired
   private List<? extends ListenerAdapter> list;

   @Override
   public void executeCommand(String[] args, MessageReceivedEvent event, MessageSender chat) {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < list.size(); i++) {
         ListenerAdapter cmd = list.get(i);
         Command[] annotations = cmd.getClass().getAnnotationsByType(Command.class);
         if (annotations.length > 0) {
            b.append(prefix)
                .append(annotations[0].name()[0])
                .append(" - ")
                .append(annotations[0].description())
                .append("\n");
            //log.info("Found command {}", annotations[0].name()[0]);
         }
      }
      
      chat.sendEmbed("Commands :^)", b.toString());
   }
   
}
