/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.gametime;

import java.util.Comparator;

/**
 *
 * @author Vincent Scavetta.
 */
public class GamePlayTime {

   private String gameName;
   private Long playTime;

   public GamePlayTime(String gameName) {
      this.gameName = gameName;
      this.playTime = 0L;
   }

   public GamePlayTime(String gameName, Long playTime) {
      this.gameName = gameName;
      this.playTime = playTime;
   }

   public Long addPlayTime(long playtime) {
      this.playTime += playtime;
      return this.playTime;
   }

   public String getPlayTimeString() {
      String timeString;
      // Convert millis to min
      long min = this.playTime / 60000;
      
      if (min > 60) {
         long hours = min / 60;
         long minLeft = min % 60;
         timeString = String.format("%d hours and %d minutes", hours, minLeft);
      } else {
         timeString = String.format("%d minutes", min);
      }
      
      return timeString;
   }

   public String getGameName() {
      return this.gameName;
   }

   public void setGameName(String gameName) {
      this.gameName = gameName;
   }

   public Long getPlayTime() {
      return this.playTime;
   }

   public void setPlayTime(Long playTime) {
      this.playTime = playTime;
   }

   public static Comparator<GamePlayTime> PlayTimeAsc = (GamePlayTime gpt1, GamePlayTime gpt2) -> {
      return (int) (gpt1.playTime - gpt2.playTime);
   };

   public static Comparator<GamePlayTime> PlayTimeDesc = (GamePlayTime gpt1, GamePlayTime gpt2) -> {
      return (int) (gpt2.playTime - gpt1.playTime);
   };
}
