/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.listening;

import com.scavettapps.noisebean.core.MessageSender;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Vincent Scavetta.
 */
@Component
public class ExceptionListener  extends ListenerAdapter {

   @Value("${noisebean.admin-id}")
   protected String adminId;
   
   @Override
   public void onException(@Nonnull ExceptionEvent event) {
      super.onException(event);
      Throwable ex = event.getCause();
      ex.printStackTrace();
      String msg ="*\n\nError:```java\n" + ex.getLocalizedMessage() + "```";
      if (msg.length() <= 2000) {
         new MessageSender(null).sendPrivateMessageToUser(msg, event.getJDA().getUserById(adminId));
      }
   }
   
}
