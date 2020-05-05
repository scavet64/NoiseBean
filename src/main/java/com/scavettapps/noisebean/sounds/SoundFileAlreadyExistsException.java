/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.sounds;

/**
 *
 * @author Vincent Scavetta.
 */
public class SoundFileAlreadyExistsException extends RuntimeException {

   public SoundFileAlreadyExistsException() {
      super ("Sound file already exists");
   }

   public SoundFileAlreadyExistsException(String name) {
      super("Sound file already exists with name: " + name);
   }
   
}
