/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.item.ItemPackage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TilePackager extends BcBlockEntity {
   public static final int PATTERN_SLOTS = 9;
   private static final int CRAFT_INTERVAL = 5;
   public final ItemHandlerSimple invStorage = this.itemManager.addInvHandler("storage", 9, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
   public final ItemHandlerSimple invInput = this.itemManager.addInvHandler("input", 1, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
   public final ItemHandlerSimple invOutput = this.itemManager.addInvHandler("output", 1, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
   public final ItemHandlerSimple invPattern = this.itemManager.addInvHandler("pattern", 9, ItemHandlerManager.EnumAccess.PHANTOM);
   private int patternsSet;
   private int lastSyncedPatterns = -1;
   private int tickCount;

   public TilePackager(BlockPos pos, BlockState state) {
      super(BCSiliconBlockEntities.PACKAGER, pos, state);
   }

   public boolean isPatternSlotSet(int slot) {
      return slot >= 0 && slot < PATTERN_SLOTS && (this.patternsSet & 1 << slot) != 0;
   }

   public void setPatternSlot(int slot, boolean set) {
      if (slot < 0 || slot >= PATTERN_SLOTS) {
         return;
      }

      if (set) {
         this.patternsSet |= 1 << slot;
      } else {
         this.patternsSet &= ~(1 << slot);
         this.invPattern.setStackInSlot(slot, ItemStack.EMPTY);
      }

      this.setChanged();
   }

   public void serverTick() {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      if (++this.tickCount >= CRAFT_INTERVAL) {
         this.tickCount = 0;
         if (this.patternsSet != 0) {
            this.attemptCraft();
         }
      }

      if (this.patternsSet != this.lastSyncedPatterns) {
         this.lastSyncedPatterns = this.patternsSet;
         this.setChanged();
         MessageUtil.sendUpdateToTrackingPlayers(this);
      }
   }

   private void attemptCraft() {
      if (!this.invOutput.getStackInSlot(0).isEmpty()) {
         return;
      }

      ItemStack input = this.invInput.getStackInSlot(0);
      if (input.isEmpty()) {
         return;
      }

      boolean fromPackage = ItemPackage.isPackage(input);
      if (!fromPackage && !input.is(Items.PAPER)) {
         return;
      }

      ItemStack result = fromPackage ? input.copy() : new ItemStack(BCSiliconItems.PACKAGE);
      result.setCount(1);

      int[] working = new int[PATTERN_SLOTS];

      for (int s = 0; s < PATTERN_SLOTS; s++) {
         working[s] = this.invStorage.getStackInSlot(s).getCount();
      }

      List<int[]> allocations = new ArrayList<>();

      for (int i = 0; i < PATTERN_SLOTS; i++) {
         if (!this.isPatternSlotSet(i)) {
            continue;
         }

         if (fromPackage && !ItemPackage.getStack(result, i).isEmpty()) {
            return;
         }

         ItemStack ghost = this.invPattern.getStackInSlot(i);
         int storageSlot = this.findStorage(working, ghost);
         if (storageSlot < 0) {
            return;
         }

         working[storageSlot]--;
         allocations.add(new int[]{i, storageSlot});
      }

      if (allocations.isEmpty()) {
         return;
      }

      for (int[] allocation : allocations) {
         int patternSlot = allocation[0];
         int storageSlot = allocation[1];
         ItemStack stored = this.invStorage.getStackInSlot(storageSlot).copy();
         ItemStack taken = stored.split(1);
         this.invStorage.setStackInSlot(storageSlot, stored.isEmpty() ? ItemStack.EMPTY : stored);
         ItemPackage.setStack(result, patternSlot, taken);
      }

      if (fromPackage) {
         this.invInput.setStackInSlot(0, ItemStack.EMPTY);
      } else {
         ItemStack paper = input.copy();
         paper.shrink(1);
         this.invInput.setStackInSlot(0, paper.isEmpty() ? ItemStack.EMPTY : paper);
      }

      this.invOutput.setStackInSlot(0, result);
      this.setChanged();
   }

   private int findStorage(int[] working, ItemStack ghost) {
      for (int s = 0; s < PATTERN_SLOTS; s++) {
         if (working[s] <= 0) {
            continue;
         }

         ItemStack stored = this.invStorage.getStackInSlot(s);
         if (stored.isEmpty()) {
            continue;
         }

         if (ghost.isEmpty() || ItemStack.isSameItemSameComponents(stored, ghost)) {
            return s;
         }
      }

      return -1;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("patternsSet", this.patternsSet);
      output.store("items", net.minecraft.nbt.CompoundTag.CODEC, this.itemManager.serializeNBT());
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.patternsSet = input.getIntOr("patternsSet", 0);
      this.lastSyncedPatterns = this.patternsSet;
      input.read("items", net.minecraft.nbt.CompoundTag.CODEC).ifPresent(tag -> this.itemManager.deserializeNBT(tag));
   }
}
