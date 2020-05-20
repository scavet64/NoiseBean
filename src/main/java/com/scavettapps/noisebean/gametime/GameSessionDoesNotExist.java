/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

/**
 *
 * @author Vincent Scavetta.
 */
public class GameSessionDoesNotExist extends Exception {

   public GameSessionDoesNotExist() {
   }

   public GameSessionDoesNotExist(String message) {
      super(message);
   }

   public GameSessionDoesNotExist(String message, Throwable cause) {
      super(message, cause);
   }

   public GameSessionDoesNotExist(Throwable cause) {
      super(cause);
   }

   public GameSessionDoesNotExist(String message, Throwable cause, 
       boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
   
}
