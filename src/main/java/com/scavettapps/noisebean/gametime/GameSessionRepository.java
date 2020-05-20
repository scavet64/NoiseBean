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
   
   Optional<GameSession> findByUserIdAndGameName(String userId, String gameName);
   
   List<GameSession> findAllByUserId(String userId);
   
   List<GameSession> findAllByGameName(String gameName);
   
   List<GameSession> findAllByUserIdAndGameNameIgnoreCase(String userId, String gameName);
   
   Optional<GameSession> findByUserIdAndSessionEndedIsNull(String userId);
   
   Optional<GameSession> findByUserIdAndGameNameAndSessionEndedIsNull(String userId, String gameName);
   
}
