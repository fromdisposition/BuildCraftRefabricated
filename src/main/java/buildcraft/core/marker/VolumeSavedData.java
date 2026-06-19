/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedDataType;

public class VolumeSavedData extends MarkerSavedDataBase<VolumeConnection> {
   public static final String ID = "buildcraft_marker_volume";
   private static final Codec<VolumeSavedData> CODEC = MarkerSavedDataBase.codec(VolumeSavedData::new);
   public static final SavedDataType<VolumeSavedData> TYPE = new SavedDataType<>(
      //? if >= 26.1 {
      Identifier.fromNamespaceAndPath("buildcraftcore", "marker_volume"), VolumeSavedData::new, CODEC, DataFixTypes.LEVEL
      //?} else {
      /*"buildcraftcore_marker_volume", VolumeSavedData::new, CODEC, DataFixTypes.LEVEL
      *///?}
   );

   private VolumeSavedData() {
   }

   public static VolumeSavedData getOrCreate(Level level) {
      return MarkerSavedDataBase.getOrCreate(level, TYPE, VolumeSavedData::new);
   }
}
