package com.scavettapps.noisebean.core;

public class ConfigurationException extends Exception {

   public ConfigurationException() {
   }

   public ConfigurationException(String message) {
      super(message);
   }

   public ConfigurationException(Throwable cause) {
      super(cause);
   }

   public ConfigurationException(String message, Throwable cause) {
      super(message, cause);
   }

   public ConfigurationException(String message, Throwable cause, boolean enableSuppression,
       boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }

}
