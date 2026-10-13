/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.enums;

import net.minecraft.util.StringRepresentable;

public enum EnumLaserTableType implements StringRepresentable {
   ASSEMBLY_TABLE,
   ADVANCED_CRAFTING_TABLE,
   INTEGRATION_TABLE,
   CHARGING_TABLE,
   PROGRAMMING_TABLE;

   public String getSerializedName() {
      return this.name();
   }
}
