/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.transport.BCTransportConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourWood extends PipeBehaviourDirectional implements IMjRedstoneReceiver {
   public PipeBehaviourWood(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourWood(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @Override
   public int getTextureIndex(@Nullable Direction face) {
      return face != null && face == this.currentDir.face ? 1 : 0;
   }

   @Override
   protected boolean canFaceDirection(Direction dir) {
      return dir != null && this.pipe.isConnected(dir) && this.pipe.getConnectedType(dir) == IPipe.ConnectedType.TILE;
   }

   public void fluidSideCheck(PipeEventFluid.SideCheck sideCheck) {
      if (this.currentDir.face != null) {
         sideCheck.disallow(this.currentDir.face);
      }
   }

   protected long extract(long power, boolean simulate) {
      if (power > 0L && this.getCurrentDir() != null) {
         PipeFlow flow = this.pipe.getFlow();
         if (flow instanceof IFlowItems itemFlow) {
            int maxItems = (int)(power / BCTransportConfig.mjPerItem);
            if (maxItems > 0) {
               int extracted = this.extractItems(itemFlow, this.getCurrentDir(), maxItems, simulate);
               if (extracted > 0) {
                  return power - extracted * BCTransportConfig.mjPerItem;
               }
            }
         } else if (flow instanceof IFlowFluid fluidFlow) {
            int maxMillibuckets = (int)(power / BCTransportConfig.mjPerMillibucket);
            if (maxMillibuckets > 0) {
               FluidStack extracted = this.extractFluid(fluidFlow, this.getCurrentDir(), maxMillibuckets, simulate);
               if (extracted != null && !extracted.isEmpty()) {
                  return power - extracted.getAmount() * BCTransportConfig.mjPerMillibucket;
               }
            }
         }
      }

      return power;
   }

   protected int extractItems(IFlowItems flow, Direction dir, int count, boolean simulate) {
      return flow.tryExtractItems(count, dir, null, stack -> true, simulate);
   }

   @Nullable
   protected FluidStack extractFluid(IFlowFluid flow, Direction dir, int millibuckets, boolean simulate) {
      return flow.tryExtractFluid(millibuckets, dir, null, simulate);
   }

   @Override
   public boolean canConnect(@Nonnull IMjConnector other) {
      return true;
   }

   private long lastRequestedPower;
   // -4, not Long.MIN_VALUE: game time minus MIN_VALUE overflows negative, so the "< 4" memo check was always
   // true and this permanently answered the initial 0 -- engines saw no demand and the pipe never extracted.
   private long lastRequestedTick = -4L;

   @Override
   public long getPowerRequested() {
      // Engines poll this every tick, and answering honestly means a full simulated extraction (an inventory
      // scan plus two transactions) even over an empty chest. Memoize for 4 ticks — the first pump after a
      // refill lands a few ticks late at worst, within the pipe's own extraction cadence.
      long now = this.pipe.getHolder().getPipeWorld().getGameTime();
      if (now - this.lastRequestedTick < 4L) {
         return this.lastRequestedPower;
      }

      this.lastRequestedTick = now;
      long power = 512L * MjAPI.MJ;
      this.lastRequestedPower = power - this.extract(power, true);
      return this.lastRequestedPower;
   }

   @Override
   public long receivePower(long microJoules, boolean simulate) {
      return this.extract(microJoules, simulate);
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> T getCapability(@Nonnull Object capability, Direction facing) {
      return (T)(capability != MjAPI.CAP_RECEIVER && capability != MjAPI.CAP_CONNECTOR ? super.getCapability(capability, facing) : this);
   }

   @Override
   public boolean canConnect(Direction face, PipeBehaviour other) {
      return !(other instanceof PipeBehaviourWood);
   }

   public void sideCheck(PipeEventItem.SideCheck sideCheck) {
      if (this.currentDir.face != null) {
         sideCheck.disallow(this.currentDir.face);
      }
   }

   @Override
   public void registerEventHandlers(IPipeEventBus bus) {
      super.registerEventHandlers(bus);
      bus.on(PipeEventFluid.SideCheck.class, this, this::fluidSideCheck);
      bus.on(PipeEventItem.SideCheck.class, this, this::sideCheck);
   }
}
