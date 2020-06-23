/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.users;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Vincent Scavetta.
 */
public interface NoiseBeanUserRepository extends JpaRepository<NoiseBeanUser, String> {
   
}
