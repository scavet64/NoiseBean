/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.scavettapps.noisebean.core.ConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.core.annotation.Order;

/**
 *
 * @author vstro
 */
@Service
@Order(0)
public class Bot {

   private static final String COULD_NOT_FIND_APPLICATION_SECRETS_FILE = "Could not find application.secrets file";

   @Autowired
   private List<? extends ListenerAdapter> list;

   @Autowired
   private EventWaiter eventWaiter;

   private JDA bot;
   private Logger logger = LoggerFactory.getLogger(this.getClass());

   @PostConstruct
   public void runBot() throws Exception {

      JDABuilder builder = JDABuilder.create(
            loadApiKey(),
            GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));

      registerCommands(builder);

      // Enable the bulk delete event
      builder.setBulkDeleteSplittingEnabled(false);
      builder.setActivity(Activity.listening("Cozy Music"));
      builder.enableCache(CacheFlag.ACTIVITY);
      builder.addEventListeners(eventWaiter);

      bot = builder.build();
   }   

   @PreDestroy
   public void shutdownBot() {
      bot.shutdownNow();
   }

   private String loadApiKey() throws ConfigurationException {
      Properties prop = new Properties();
      try (InputStream input = Bot.class.getClassLoader().getResourceAsStream("application.secrets")) {
         if (input == null) {
            System.err.println(COULD_NOT_FIND_APPLICATION_SECRETS_FILE);
            throw new ConfigurationException(COULD_NOT_FIND_APPLICATION_SECRETS_FILE);
         }
         prop.load(input);
      } catch (IOException ex) {
         ex.printStackTrace();
      }

      return prop.getProperty("apikey");
   }

   public void registerCommands(JDABuilder builder) throws ConfigurationException {
      for (ListenerAdapter command : list) {
         logger.info("Registering Command: " + command.getClass().getSimpleName());
         builder.addEventListeners(command);
      }
   }
}
