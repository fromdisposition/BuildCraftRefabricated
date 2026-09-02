/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

public class ContainerDynamoMJ extends ContainerElectricEngine<TileDynamoMJ> {
   public ContainerDynamoMJ(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileDynamoMJ.class));
   }

   public ContainerDynamoMJ(int containerId, Inventory playerInv, TileDynamoMJ dynamo) {
      super(BCEnergyMenuTypes.DYNAMO_MJ, containerId, playerInv, dynamo, d -> d.getMjBattery().getStored(), 44);
   }
}
