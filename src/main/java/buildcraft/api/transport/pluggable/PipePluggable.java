/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pluggable;

import buildcraft.api.transport.pipe.IPipeHolder;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import team.reborn.energy.api.EnergyStorage;

public abstract class PipePluggable {
   public final PluggableDefinition definition;
   public final IPipeHolder holder;
   public final Direction side;

   public PipePluggable(PluggableDefinition definition, IPipeHolder holder, Direction side) {
      this.definition = definition;
      this.holder = holder;
      this.side = side;
   }

   public CompoundTag writeToNbt() {
      return new CompoundTag();
   }

   public boolean readFromNbt(CompoundTag nbt) {
      return false;
   }

   public CompoundTag writeClientUpdateData() {
      return new CompoundTag();
   }

   public void readClientUpdateData(CompoundTag nbt) {
   }

   public void writeCreationPayload(FriendlyByteBuf buffer) {
   }

   public void writePayload(FriendlyByteBuf buffer, Object side) {
   }

   public void readPayload(FriendlyByteBuf buffer, Object side, Object ctx) throws IOException {
   }

   public final void scheduleNetworkUpdate() {
      this.holder.scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.PLUGGABLES[this.side.ordinal()]);
   }

   public void onTick() {
   }

   public boolean needsTick() {
      return false;
   }

   public abstract AABB getBoundingBox();

   public boolean isBlocking() {
      return false;
   }

   public <T> T getCapability(@Nonnull Object cap) {
      return null;
   }

   @Nullable
   public Storage<FluidVariant> fluidStorage() {
      return null;
   }

   @Nullable
   public Storage<ItemVariant> itemStorage() {
      return null;
   }

   @Nullable
   public EnergyStorage energyStorage() {
      return null;
   }

   public void onRemove() {
   }

   public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
      ItemStack stack = this.getPickStack();
      if (!stack.isEmpty()) {
         toDrop.add(stack);
      }
   }

   public ItemStack getPickStack() {
      return ItemStack.EMPTY;
   }

   public boolean onPluggableActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ) {
      return false;
   }

   @Nullable
   public PluggableModelKey getModelRenderKey(Object layer) {
      return null;
   }

   public boolean canBeConnected() {
      return false;
   }

   public boolean isSideSolid() {
      return false;
   }

   public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
      return 0.0F;
   }

   public boolean canConnectToRedstone(@Nullable Direction to) {
      return false;
   }

   public Object getBlockFaceShape() {
      return null;
   }

   public void onPlacedBy(Player player) {
   }

   public void registerEventHandlers(buildcraft.api.transport.pipe.IPipeEventBus bus) {
   }
}
