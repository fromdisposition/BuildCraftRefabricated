/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

public final class ZonePlannerChunkKeys {
   private ZonePlannerChunkKeys() {
   }

   public static long chunkKey(int chunkX, int chunkZ) {
      return (chunkX & 0xFFFFFFFFL) | (long)chunkZ << 32;
   }
}
