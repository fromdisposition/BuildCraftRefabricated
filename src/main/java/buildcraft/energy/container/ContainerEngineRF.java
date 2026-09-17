/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

public class ContainerEngineRF extends ContainerElectricEngine<TileEngineRF> {
   public ContainerEngineRF(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileEngineRF.class));
   }

   public ContainerEngineRF(int containerId, Inventory playerInv, TileEngineRF engine) {
      super(BCEnergyMenuTypes.ENGINE_FE, containerId, playerInv, engine, TileEngineRF::getPower, 62);
   }
}
