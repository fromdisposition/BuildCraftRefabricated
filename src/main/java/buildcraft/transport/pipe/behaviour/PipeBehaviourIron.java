/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventItem;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourIron extends PipeBehaviourDirectional {
   public PipeBehaviourIron(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourIron(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @Override
   public int getTextureIndex(@Nullable Direction face) {
      return face == this.currentDir.face ? 0 : 1;
   }

   @Override
   protected boolean canFaceDirection(Direction dir) {
      return this.pipe.isConnected(dir);
   }

   public void sideCheck(PipeEventItem.SideCheck sideCheck) {
      if (this.currentDir == EnumPipePart.CENTER) {
         sideCheck.disallowAll();
      } else {
         sideCheck.disallowAllExcept(this.currentDir.face);
      }
   }

   public void fluidSideCheck(PipeEventFluid.SideCheck sideCheck) {
      if (this.currentDir == EnumPipePart.CENTER) {
         sideCheck.disallowAll();
      } else {
         sideCheck.disallowAllExcept(this.currentDir.face);
      }
   }

   public static void tryBounce(PipeEventItem.TryBounce tryBounce) {
      tryBounce.canBounce = true;
   }

   public void fluidInsert(PipeEventFluid.TryInsert insert) {
      if (this.currentDir.face == insert.from) {
         insert.cancel();
      }
   }

   @Override
   public void registerEventHandlers(IPipeEventBus bus) {
      super.registerEventHandlers(bus);
      bus.on(PipeEventItem.SideCheck.class, this, this::sideCheck);
      bus.on(PipeEventFluid.SideCheck.class, this, this::fluidSideCheck);
      bus.on(PipeEventItem.TryBounce.class, this, PipeBehaviourIron::tryBounce);
      bus.on(PipeEventFluid.TryInsert.class, this, this::fluidInsert);
   }
}
