/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vincent Scavetta.
 */
@Service
public class NoiseBeanUserService {
   
   private final NoiseBeanUserRepository noiseBeanUserRepository;

   @Autowired
   public NoiseBeanUserService(NoiseBeanUserRepository noiseBeanUserRepository) {
      this.noiseBeanUserRepository = noiseBeanUserRepository;
   }
   
   public NoiseBeanUser getNoiseBeanUser(String userId) {
      return this.noiseBeanUserRepository.findById(userId).orElse(null);
   }
   
   public NoiseBeanUser getNoiseBeanUser(long userId) {
      return this.noiseBeanUserRepository.findById(Long.toString(userId)).orElse(null);
   }
   
   public NoiseBeanUser saveNoiseBeanUser(NoiseBeanUser user) {
      return this.noiseBeanUserRepository.save(user);
   }
}
