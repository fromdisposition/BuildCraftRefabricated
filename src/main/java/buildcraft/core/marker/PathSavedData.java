/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker;

import buildcraft.lib.compat.BcSavedDataType;
import com.mojang.serialization.Codec;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;

public class PathSavedData extends MarkerSavedDataBase<PathConnection> {
   public static final String ID = "buildcraft_marker_path";
   private static final Codec<PathSavedData> CODEC = MarkerSavedDataBase.codec(PathSavedData::new);
   public static final BcSavedDataType<PathSavedData> TYPE = new BcSavedDataType<>(
      "buildcraftcore", "marker_path", PathSavedData::new, CODEC, DataFixTypes.LEVEL
   );

   private PathSavedData() {
   }

   public static PathSavedData getOrCreate(Level level) {
      return TYPE.getOrCreate(level, PathSavedData::new);
   }

   //? if < 1.21.10 {
   /*@Override
   public net.minecraft.nbt.CompoundTag save(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
      return BcSavedDataType.encode(CODEC, this, tag, provider);
   }
   *///?}
}
