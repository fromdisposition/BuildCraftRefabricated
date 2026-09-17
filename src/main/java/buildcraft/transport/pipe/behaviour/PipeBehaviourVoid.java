/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventItem;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourVoid extends PipeBehaviour {
   public PipeBehaviourVoid(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourVoid(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   public static void reachCentre(PipeEventItem.ReachCenter reachCenter) {
      reachCenter.getStack().setCount(0);
   }

   public static void moveFluidToCentre(PipeEventFluid.OnMoveToCentre move) {
      Arrays.fill(move.fluidEnteringCentre, 0);
   }

   @Override
   public void registerEventHandlers(IPipeEventBus bus) {
      bus.on(PipeEventItem.ReachCenter.class, this, PipeBehaviourVoid::reachCentre);
      bus.on(PipeEventFluid.OnMoveToCentre.class, this, PipeBehaviourVoid::moveFluidToCentre);
   }
}
