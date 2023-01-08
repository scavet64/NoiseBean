/**
 * Copyright 2020 - Vincent Scavetta - All Rights Reserved
 */
package com.scavettapps.noisebean.voting;

import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author Vincent Scavetta.
 */
@Builder
public class Vote {
   private @Getter long userId;
   private @Getter int voteValue;

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 71 * hash + (int) (this.userId ^ (this.userId >>> 32));
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final Vote other = (Vote) obj;
      if (this.userId != other.userId) {
         return false;
      }
      return true;
   }
   
   
}
