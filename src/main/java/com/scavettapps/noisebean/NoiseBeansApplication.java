/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class NoiseBeansApplication {

   public static void main(String[] args) {
      SpringApplication.run(NoiseBeansApplication.class, args);
   }

   @Bean
   @Scope("singleton")
   public EventWaiter eventWaiter() {
      return new EventWaiter();
   }

}
