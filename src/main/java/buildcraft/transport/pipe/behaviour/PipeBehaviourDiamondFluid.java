/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.lib.fluid.stack.FluidStack;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class PipeBehaviourDiamondFluid extends PipeBehaviourDiamond {
   public PipeBehaviourDiamondFluid(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourDiamondFluid(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   public void sideCheck(PipeEventFluid.SideCheck sideCheck) {
      FluidStack toCompare = sideCheck.fluid;

      for (Direction face : Direction.values()) {
         if (sideCheck.isAllowed(face) && this.pipe.isConnected(face)) {
            int offset = 9 * face.ordinal();
            boolean sideAllowed = false;
            boolean foundItem = false;

            for (int i = 0; i < 9; i++) {
               ItemStack compareTo = this.filters.getStackInSlot(offset + i);
               if (!compareTo.isEmpty()) {
                  FluidStack target = FilterFluidStacks.fluidFromFilter(compareTo);
                  if (!target.isEmpty()) {
                     foundItem = true;
                     if (FluidStack.isSameFluidSameComponents(target, toCompare)) {
                        sideAllowed = true;
                        break;
                     }
                  }
               }
            }

            if (foundItem) {
               if (sideAllowed) {
                  sideCheck.increasePriority(face, 12);
               } else {
                  sideCheck.disallow(face);
               }
            }
         }
      }
   }

   @Override
   public void registerEventHandlers(IPipeEventBus bus) {
      bus.on(PipeEventFluid.SideCheck.class, this, this::sideCheck);
   }
}
