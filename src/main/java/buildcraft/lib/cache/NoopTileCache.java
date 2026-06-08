/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public enum NoopTileCache implements ITileCache {
   INSTANCE;

   @Override
   public void invalidate() {
   }

   @Override
   public TileCacheRet getTile(BlockPos pos) {
      return null;
   }

   @Override
   public TileCacheRet getTile(Direction offset) {
      return null;
   }
}
