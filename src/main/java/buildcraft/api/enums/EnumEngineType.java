/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.enums;

import buildcraft.api.core.IEngineType;
import net.minecraft.util.StringRepresentable;

public enum EnumEngineType implements StringRepresentable, IEngineType {
   WOOD("core", "wood"),
   STONE("energy", "stone"),
   IRON("energy", "iron"),
   CREATIVE("energy", "creative"),
   RF("energy", "rf");

   public final String unlocalizedTag;
   public final String resourceLocation;
   public static final EnumEngineType[] VALUES = values();

   EnumEngineType(String mod, String loc) {
      this.unlocalizedTag = loc;
      this.resourceLocation = "buildcraft" + mod + ":block/engine/inv/" + loc;
   }

   @Override
   public String getItemModelLocation() {
      return this.resourceLocation;
   }

   public String getSerializedName() {
      return this.unlocalizedTag;
   }

   public static EnumEngineType fromMeta(int meta) {
      if (meta < 0 || meta >= VALUES.length) {
         meta = 0;
      }

      return VALUES[meta];
   }
}
