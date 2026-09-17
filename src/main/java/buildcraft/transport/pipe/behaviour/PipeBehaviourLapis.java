/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeFaceTex;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

public class PipeBehaviourLapis extends PipeBehaviour {
   private final PipeBehaviourColourData colourData = new PipeBehaviourColourData();

   public PipeBehaviourLapis(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourLapis(IPipe pipe, CompoundTag nbt) {
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
   public PipeFaceTex getTextureData(@Nullable Direction face) {
      return PipeFaceTex.get(this.colourData.getColour().getId());
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      return this.colourData.onPipeActivate(player, trace, this.pipe.getHolder());
   }

   public void onReachCenter(PipeEventItem.ReachCenter reachCenter) {
      reachCenter.colour = this.colourData.getColour();
   }

   @Override
   public void registerEventHandlers(IPipeEventBus bus) {
      bus.on(PipeEventItem.ReachCenter.class, this, this::onReachCenter);
      this.colourData.registerPaintActions(bus, this);
   }
}
