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

public class VolumeSavedData extends MarkerSavedDataBase<VolumeConnection> {
   public static final String ID = "buildcraft_marker_volume";
   private static final Codec<VolumeSavedData> CODEC = MarkerSavedDataBase.codec(VolumeSavedData::new);
   public static final BcSavedDataType<VolumeSavedData> TYPE = new BcSavedDataType<>(
      "buildcraftcore", "marker_volume", VolumeSavedData::new, CODEC, DataFixTypes.LEVEL
   );

   private VolumeSavedData() {
   }

   public static VolumeSavedData getOrCreate(Level level) {
      return TYPE.getOrCreate(level, VolumeSavedData::new);
   }

   //? if < 1.21.10 {
   /*@Override
   public net.minecraft.nbt.CompoundTag save(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
      return BcSavedDataType.encode(CODEC, this, tag, provider);
   }
   *///?}
}
