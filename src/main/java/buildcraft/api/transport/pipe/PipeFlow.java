/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import buildcraft.api.core.EnumPipePart;
import java.io.IOException;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;

public abstract class PipeFlow {
   public static final int NET_ID_FULL_STATE = 0;
   public static final int NET_ID_UPDATE = 1;
   public final IPipe pipe;

   public PipeFlow(IPipe pipe) {
      this.pipe = pipe;
   }

   public PipeFlow(IPipe pipe, CompoundTag nbt) {
      this.pipe = pipe;
   }

   public CompoundTag writeToNbt() {
      return new CompoundTag();
   }

   public void readFromNbt(CompoundTag nbt) {
   }

   public void writePayload(int id, FriendlyByteBuf buffer, Object side) {
   }

   public void readPayload(int id, FriendlyByteBuf buffer, Object side) throws IOException {
   }

   public void sendPayload(int id) {
      Object side = this.pipe.getHolder().getPipeWorld().isClientSide() ? null : null;
      this.sendCustomPayload(id, buf -> this.writePayload(id, buf, side));
   }

   public final void sendCustomPayload(int id, IPipeHolder.IWriter writer) {
      this.pipe.getHolder().sendMessage(IPipeHolder.PipeMessageReceiver.FLOW, buffer -> {
         buffer.writeBoolean(true);
         buffer.writeShort(id);
         writer.write(buffer);
      });
   }

   public abstract boolean canConnect(Direction var1, PipeFlow var2);

   public abstract boolean canConnect(Direction var1, BlockEntity var2);

   public boolean shouldForceConnection(Direction face, BlockEntity oTile) {
      return false;
   }

   public void onTick() {
   }

   public boolean hasSimulationWork() {
      return false;
   }

   public boolean hasClientSimulationWork() {
      return this.hasSimulationWork();
   }

   public void postPluggableTick() {
   }

   public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
   }

   public boolean onFlowActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      return false;
   }

   public final boolean hasCapability(@Nonnull Object capability, Direction facing) {
      return this.getCapability(capability, facing) != null;
   }

   public <T> T getCapability(@Nonnull Object capability, Direction facing) {
      return null;
   }

   public void registerEventHandlers(IPipeEventBus bus) {
   }
}
