/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.robotics.BCRoboticsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;

public class TileRequester extends BcBlockEntity implements IRequestProvider {
   public static final int NB_ITEMS = 20;
   public final ItemHandlerSimple invRequests = this.itemManager.addInvHandler("requests", NB_ITEMS, ItemHandlerManager.EnumAccess.PHANTOM);
   public final ItemHandlerSimple invItems = this.itemManager
      .addInvHandler("items", NB_ITEMS, this::matchesRequest, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);

   public TileRequester(BlockPos pos, BlockState state) {
      super(BCRoboticsBlockEntities.REQUESTER, pos, state);
   }

   private boolean matchesRequest(int slot, ItemStack stack) {
      ItemStack request = this.invRequests.getStackInSlot(slot);
      return !request.isEmpty() && StackUtil.isMatchingItemOrList(request, stack);
   }

   public ItemStack getRequestTemplate(int index) {
      return this.invRequests.getStackInSlot(index);
   }

   public boolean isFulfilled(int i) {
      ItemStack request = this.invRequests.getStackInSlot(i);
      if (request.isEmpty()) {
         return true;
      }

      ItemStack existing = this.invItems.getStackInSlot(i);
      if (existing.isEmpty()) {
         return false;
      }

      return StackUtil.isMatchingItemOrList(request, existing) && existing.getCount() >= request.getCount();
   }

   @Override
   public int getRequestsCount() {
      return NB_ITEMS;
   }

   @Override
   public ItemStack getRequest(int i) {
      ItemStack request = this.invRequests.getStackInSlot(i);
      if (request.isEmpty() || this.isFulfilled(i)) {
         return ItemStack.EMPTY;
      }

      ItemStack remaining = request.copy();
      ItemStack existing = this.invItems.getStackInSlot(i);
      if (existing.isEmpty()) {
         return remaining;
      }

      if (!StackUtil.isMatchingItemOrList(request, existing)) {
         return ItemStack.EMPTY;
      }

      remaining.shrink(existing.getCount());
      return remaining.getCount() <= 0 ? ItemStack.EMPTY : remaining;
   }

   @Override
   public ItemStack offerItem(int i, ItemStack stack) {
      ItemStack request = this.invRequests.getStackInSlot(i);
      if (request.isEmpty() || stack.isEmpty() || !StackUtil.isMatchingItemOrList(request, stack)) {
         return stack;
      }

      int maxQty = request.getCount();
      ItemStack existing = this.invItems.getStackInSlot(i);
      int current = existing.isEmpty() ? 0 : existing.getCount();
      if (current >= maxQty) {
         return stack;
      }

      int accept = Math.min(stack.getCount(), maxQty - current);
      ItemStack stored = stack.copyWithCount(current + accept);
      this.invItems.setStackInSlot(i, stored);
      this.setChanged();

      ItemStack leftover = stack.copy();
      leftover.shrink(accept);
      return leftover.getCount() <= 0 ? ItemStack.EMPTY : leftover;
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      output.store("items", CompoundTag.CODEC, this.itemManager.serializeNBT());
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      input.read("items", CompoundTag.CODEC).ifPresent(tag -> this.itemManager.deserializeNBT(tag));
   }
}
