/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import buildcraft.api.core.EnumPipePart;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;

public abstract class PipeBehaviour {
   public final IPipe pipe;

   public PipeBehaviour(IPipe pipe) {
      this.pipe = pipe;
   }

   public PipeBehaviour(IPipe pipe, CompoundTag nbt) {
      this.pipe = pipe;
   }

   public CompoundTag writeToNbt() {
      return new CompoundTag();
   }

   public void readFromNbt(CompoundTag nbt) {
   }

   public void onTick() {
   }

   public boolean hasSimulationWork() {
      return false;
   }

   public boolean canConnect(Direction face, PipeBehaviour other) {
      return true;
   }

   public boolean canConnect(Direction face, BlockEntity oTile) {
      return true;
   }

   public boolean shouldForceConnection(Direction face, BlockEntity oTile) {
      return false;
   }

   public int getTextureIndex(@Nullable Direction face) {
      return 0;
   }

   public PipeFaceTex getTextureData(@Nullable Direction face) {
      return PipeFaceTex.get(this.getTextureIndex(face));
   }

   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      return false;
   }

   public void onEntityCollide(Entity entity) {
   }

   public void writePayload(FriendlyByteBuf buffer) {
   }

   public void readPayload(FriendlyByteBuf buffer, Object ctx) throws IOException {
   }

   public <T> T getCapability(@Nonnull Object capability, Direction facing) {
      return null;
   }

   public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
   }
}
