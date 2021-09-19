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
package com.scavettapps.noisebean.sounds;

import com.google.common.io.Files;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message.Attachment;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Vincent Scavetta.
 */
@Service
@Slf4j
public class SoundFileService {

   private final static String SOUND_FILE_PATH_STRING = "./data/AudioFiles/";
   private final static File SOUND_FILE_PATH = new File(SOUND_FILE_PATH_STRING);

   private final SoundFileRepository soundFileRepository;

   /**
    * Autowired Constructor for the SoundFileService
    * @param soundFileRepository The sound file repository for CRUD functionality.
    */
   @Autowired
   public SoundFileService(SoundFileRepository soundFileRepository) {
      this.soundFileRepository = soundFileRepository;
      if (!SOUND_FILE_PATH.exists()) {
         SOUND_FILE_PATH.mkdir();
      }
   }

   /**
    * Get the sound file object that has this ID.
    *
    * @param id The ID of the sound that should be retrieved.
    * @return The sound file that has this ID.
    * @throws SoundFileNotFoundException If the sound file does not exist
    */
   public SoundFile getSoundFile(Long id) throws SoundFileNotFoundException {
      return this.soundFileRepository.findById(id).orElseThrow(
         () -> new SoundFileNotFoundException()
      );
   }

   /**
    * Get the sound file object that has this name.
    *
    * @param soundFileName The name of the sound that should be received
    * @return The sound file that has this name
    * @throws SoundFileNotFoundException If the sound file did not exist
    */
   public SoundFile getSoundFile(String soundFileName) throws SoundFileNotFoundException {
      return this.soundFileRepository.findBySoundFileName(soundFileName).orElseThrow(
         () -> new SoundFileNotFoundException()
      );
   }

   /**
    * Does a sound file with this name exist in the database?
    *
    * @param soundFileName The name to check for.
    * @return true if the name exists, false otherwise
    */
   public boolean doesSoundExist(String soundFileName) {
      return this.soundFileRepository.findBySoundFileName(soundFileName).isPresent();
   }

   /**
    * Save a sound file.
    *
    * @param soundFileName The name of the sound file that should be saved.
    * @param attachment The file attached to the discord message.
    * @return The persisted sound file object
    * @throws SoundFileDownloadException If there was a problem downloading the file to the filesystem.
    */
   public SoundFile saveSoundFile(String soundFileName, Attachment attachment) throws SoundFileDownloadException {

      try {
         File downloadedFile = new File(SOUND_FILE_PATH_STRING + attachment.getFileName());

         // Does this filename already exist, throw exception if so
         if (downloadedFile.exists()) {
            throw new SoundFileDownloadException("Filename Already Exists");
         }

         CompletableFuture<File> future = attachment.downloadToFile(downloadedFile);
         File completedFile = future.get(1, TimeUnit.MINUTES);

         SoundFile newFile = new SoundFile(soundFileName, completedFile.getAbsolutePath());

         return this.soundFileRepository.save(newFile);
      } catch (TimeoutException | ExecutionException | InterruptedException ex) {
         Logger.getLogger(SoundFileService.class.getName()).log(Level.SEVERE, null, ex);
         throw new SoundFileDownloadException();
      }
   }

   public Collection<SoundFile> getAllSoundFiles() {
      return this.soundFileRepository.findAll();
   }

   /**
    * Delete a sound file.
    *
    * @param soundFileName The sound file name that should be deleted
    * @return True if the deletion was successful and that the file was removed from the filesystem
    * @throws SoundFileNotFoundException If the sound file was not found
    */
   public boolean deleteSoundFile(String soundFileName) throws SoundFileNotFoundException {
      SoundFile sound = this.soundFileRepository.findBySoundFileName(soundFileName).orElseThrow(
         SoundFileNotFoundException::new
      );

      boolean deleted = true;
      File soundFilePath = new File(sound.getFilePath());
      if (soundFilePath.exists()) {
         deleted = soundFilePath.delete();
      }
      this.soundFileRepository.delete(sound);
      return deleted;
   }

   /**
    * Rename a sound file to a new name
    *
    * @param soundFileName The sound file to update.
    * @param newSoundName  The new sound file name.
    * @return The updated sound file object.
    * @throws SoundFileNotFoundException If the sound file did not exist
    */
   public SoundFile renameSoundFile(String soundFileName, String newSoundName) throws SoundFileNotFoundException {
      SoundFile sound = this.soundFileRepository.findBySoundFileName(soundFileName).orElseThrow(
         SoundFileNotFoundException::new
      );

      this.soundFileRepository.findBySoundFileName(soundFileName).ifPresent(
         s -> {
            throw new SoundFileAlreadyExistsException(s.getSoundFileName());
         }
      );

      sound.setSoundFileName(newSoundName);
      return this.soundFileRepository.save(sound);
   }
}
