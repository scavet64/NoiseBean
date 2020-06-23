/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import com.scavettapps.noisebean.calltime.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Vincent Scavetta.
 */
public interface GameSessionRepository extends JpaRepository<GameSession, Long>{
   
   Optional<GameSession> findByUserId_IdAndGameName(String userId, String gameName);
   
   List<GameSession> findAllByUserId_Id(String userId);
   
   List<GameSession> findAllByGameName(String gameName);
   
   List<GameSession> findAllByUserId_IdAndGameNameIgnoreCase(String userId, String gameName);
   
   Optional<GameSession> findByUserId_IdAndSessionEndedIsNull(String userId);
   
   Optional<GameSession> findByUserId_IdAndGameNameAndSessionEndedIsNull(String userId, String gameName);
   
}
