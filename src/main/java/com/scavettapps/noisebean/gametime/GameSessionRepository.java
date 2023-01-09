/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Vincent Scavetta.
 */
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

   Optional<GameSession> findByUserId_IdAndGameName(String userId, String gameName);

   List<GameSession> findAllByUserId_Id(String userId);

   List<GameSession> findAllByUserId_IdAndSessionStartedAfter(String userId, Instant instant);

   List<GameSession> findAllByUserId_IdAndSessionStartedAfterAndSessionStartedBefore(String userId, Instant from, Instant to);

   List<GameSession> findAllByGameName(String gameName);

   List<GameSession> findAllByUserId_IdAndGameNameIgnoreCase(String userId, String gameName);

   Optional<GameSession> findByUserId_IdAndSessionEndedIsNull(String userId);

   Optional<GameSession> findByUserId_IdAndGameNameAndSessionEndedIsNull(String userId, String gameName);
}
