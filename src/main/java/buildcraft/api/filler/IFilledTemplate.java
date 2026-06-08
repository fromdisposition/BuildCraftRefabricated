/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.filler;

import net.minecraft.core.BlockPos;

public interface IFilledTemplate {
   BlockPos getSize();

   default BlockPos getMax() {
      return this.getSize().subtract(new BlockPos(1, 1, 1));
   }

   boolean get(int var1, int var2, int var3);

   void set(int var1, int var2, int var3, boolean var4);

   default void setLineX(int fromX, int toX, int y, int z, boolean value) {
      for (int x = fromX; x <= toX; x++) {
         this.set(x, y, z, value);
      }
   }

   default void setLineY(int x, int fromY, int toY, int z, boolean value) {
      for (int y = fromY; y <= toY; y++) {
         this.set(x, y, z, value);
      }
   }

   default void setLineZ(int x, int y, int fromZ, int toZ, boolean value) {
      for (int z = fromZ; z <= toZ; z++) {
         this.set(x, y, z, value);
      }
   }

   default void setAreaYZ(int x, int fromY, int toY, int fromZ, int toZ, boolean value) {
      for (int y = fromY; y <= toY; y++) {
         for (int z = fromZ; z <= toZ; z++) {
            this.set(x, y, z, value);
         }
      }
   }

   default void setAreaXZ(int fromX, int toX, int y, int fromZ, int toZ, boolean value) {
      for (int x = fromX; x <= toX; x++) {
         for (int z = fromZ; z <= toZ; z++) {
            this.set(x, y, z, value);
         }
      }
   }

   default void setAreaXY(int fromX, int toX, int fromY, int toY, int z, boolean value) {
      for (int y = fromY; y <= toY; y++) {
         for (int x = fromX; x <= toX; x++) {
            this.set(x, y, z, value);
         }
      }
   }

   default void setPlaneYZ(int x, boolean value) {
      this.setAreaYZ(x, 0, this.getMax().getY(), 0, this.getMax().getZ(), value);
   }

   default void setPlaneXZ(int y, boolean value) {
      this.setAreaXZ(0, this.getMax().getX(), y, 0, this.getMax().getZ(), value);
   }

   default void setPlaneXY(int z, boolean value) {
      this.setAreaXY(0, this.getMax().getX(), 0, this.getMax().getY(), z, value);
   }

   default void setAll(boolean value) {
      for (int z = 0; z < this.getSize().getZ(); z++) {
         for (int y = 0; y < this.getSize().getY(); y++) {
            for (int x = 0; x < this.getSize().getX(); x++) {
               this.set(x, y, z, value);
            }
         }
      }
   }
}
