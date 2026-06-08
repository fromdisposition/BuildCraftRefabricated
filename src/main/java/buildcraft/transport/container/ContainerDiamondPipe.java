/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.container;

import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import buildcraft.lib.gui.slot.SlotPhantom;

public class ContainerDiamondPipe extends AbstractPipeFilterContainer<PipeBehaviourDiamond> {
   public ContainerDiamondPipe(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, resolveBehaviour(playerInv, pos, PipeBehaviourDiamond.class, "diamond pipe"));
   }

   public ContainerDiamondPipe(int containerId, Inventory playerInv, PipeBehaviourDiamond behaviour) {
      super(BCTransportMenuTypes.DIAMOND_PIPE, containerId, playerInv, behaviour, behaviour == null ? null : behaviour.pipe.getHolder());
      if (behaviour == null) {
         this.addFullPlayerInventory(8, 140);
      } else {
         for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
               this.addSlot(new SlotPhantom(behaviour.filters, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
         }

         this.addFullPlayerInventory(8, 140);
      }
   }
}
