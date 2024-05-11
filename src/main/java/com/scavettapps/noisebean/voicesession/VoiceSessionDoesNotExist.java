package com.scavettapps.noisebean.voicesession;

public class VoiceSessionDoesNotExist extends Exception {

   public VoiceSessionDoesNotExist() {}

   public VoiceSessionDoesNotExist(String message) {
      super(message);
   }

   public VoiceSessionDoesNotExist(String message, Throwable cause) {
      super(message, cause);
   }

   public VoiceSessionDoesNotExist(Throwable cause) {
      super(cause);
   }

   public VoiceSessionDoesNotExist(String message, Throwable cause,
         boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
   }
}
