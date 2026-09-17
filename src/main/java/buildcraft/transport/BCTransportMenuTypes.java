/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;
import net.minecraft.world.inventory.MenuType;

public final class BCTransportMenuTypes {
   public static MenuType<ContainerFilteredBuffer_BC8> FILTERED_BUFFER;
   public static MenuType<ContainerDiamondPipe> DIAMOND_PIPE;
   public static MenuType<ContainerDiamondWoodPipe> DIAMOND_WOOD_PIPE;
   public static MenuType<ContainerEmzuliPipe_BC8> EMZULI_PIPE;

   private BCTransportMenuTypes() {
   }

   public static void register() {
      FILTERED_BUFFER = BCRegistries.registerMenuType("buildcrafttransport", "filtered_buffer", ExtendedMenuTypes.create(ContainerFilteredBuffer_BC8::new));
      DIAMOND_PIPE = BCRegistries.registerMenuType("buildcrafttransport", "diamond_pipe", ExtendedMenuTypes.create(ContainerDiamondPipe::new));
      DIAMOND_WOOD_PIPE = BCRegistries.registerMenuType("buildcrafttransport", "diamond_wood_pipe", ExtendedMenuTypes.create(ContainerDiamondWoodPipe::new));
      EMZULI_PIPE = BCRegistries.registerMenuType("buildcrafttransport", "emzuli_pipe", ExtendedMenuTypes.create(ContainerEmzuliPipe_BC8::new));
   }
}
