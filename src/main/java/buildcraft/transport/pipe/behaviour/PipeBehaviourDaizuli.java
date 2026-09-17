/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.PipeEventItem;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

public class PipeBehaviourDaizuli extends PipeBehaviourDirectional {
   private final PipeBehaviourColourData colourData = new PipeBehaviourColourData();

   public PipeBehaviourDaizuli(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourDaizuli(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      this.colourData.readFromNbt(nbt);
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      this.colourData.writeToNbt(nbt);
      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      super.readFromNbt(nbt);
      this.colourData.readFromNbt(nbt);
   }

   @Override
   public void writePayload(FriendlyByteBuf buffer) {
      super.writePayload(buffer);
      this.colourData.writePayload(buffer);
   }

   @Override
   public void readPayload(FriendlyByteBuf buffer, boolean isClientSide) {
      super.readPayload(buffer, isClientSide);
      this.colourData.readPayload(buffer);
   }

   @Override
   public int getTextureIndex(@Nullable Direction face) {
      return face != this.currentDir.face && face != null ? 16 : this.colourData.getColour().getId();
   }

   @Override
   protected boolean canFaceDirection(Direction dir) {
      return true;
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      if (part != EnumPipePart.CENTER && part != this.currentDir) {
         return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
      } else {
         return this.colourData.onPipeActivate(player, trace, this.pipe.getHolder());
      }
   }

   public void sideCheck(PipeEventItem.SideCheck sideCheck) {
      if (this.colourData.getColour() == sideCheck.colour) {
         sideCheck.disallowAllExcept(this.currentDir.face);
      } else {
         sideCheck.disallow(this.currentDir.face);
      }
   }

   @Override
   public void registerEventHandlers(IPipeEventBus bus) {
      super.registerEventHandlers(bus);
      bus.on(PipeEventItem.SideCheck.class, this, this::sideCheck);
      this.colourData.registerPaintActions(bus, this);
   }
}
