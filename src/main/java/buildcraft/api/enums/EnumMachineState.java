/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.enums;

import buildcraft.api.properties.BuildCraftProperties;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;

public enum EnumMachineState implements StringRepresentable {
   OFF,
   ON,
   DONE;

   public static EnumMachineState getType(BlockState state) {
      return (EnumMachineState)state.getValue(BuildCraftProperties.MACHINE_STATE);
   }

   public String getSerializedName() {
      return this.name();
   }
}
