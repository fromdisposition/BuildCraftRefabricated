/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.lib.misc.VecUtil;
import java.util.EnumMap;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public abstract class PipeEnergyFlowBase extends PipeFlow {
   public Vec3 clientDisplayFlowCentre = VecUtil.VEC_HALF;
   public Vec3 clientDisplayFlowCentreLast = VecUtil.VEC_HALF;
   protected final PipeEnergyDisplaySupport.DisplaySnapshot displaySnapshot = new PipeEnergyDisplaySupport.DisplaySnapshot();
   private SafeTimeTracker networkTracker;

   protected PipeEnergyFlowBase(IPipe pipe) {
      super(pipe);
   }

   protected PipeEnergyFlowBase(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   protected SafeTimeTracker getNetworkTracker() {
      if (this.networkTracker == null) {
         this.networkTracker = PipeEnergySimulation.createNetworkTracker();
      }

      return this.networkTracker;
   }

   protected boolean tickClientDisplay(EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections) {
      if (!this.pipe.getHolder().getPipeWorld().isClientSide()) {
         return false;
      }

      PipeEnergyDisplaySupport.ClientAnimationState state = PipeEnergyDisplaySupport.tickClientAnimation(
         this.clientDisplayFlowCentre, this.clientDisplayFlowCentreLast, sections
      );
      this.clientDisplayFlowCentreLast = state.centreLast();
      this.clientDisplayFlowCentre = state.centre();
      return true;
   }

   protected void captureDisplaySnapshot(EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections) {
      this.displaySnapshot.capture(sections);
   }

   protected void sendDisplayIfChanged(EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections, int payloadId) {
      if (this.displaySnapshot.changed(sections) && this.getNetworkTracker().markTimeIfDelay(this.pipe.getHolder().getPipeWorld())) {
         this.sendPayload(payloadId);
      }
   }
}
