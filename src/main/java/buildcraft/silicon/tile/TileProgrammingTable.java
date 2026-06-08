/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileProgrammingTable extends TileLaserTableBase {
   public static final int WIDTH = 6;
   public static final int HEIGHT = 4;
   public final ItemHandlerSimple invInput = this.itemManager.addInvHandler("input", 1, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
   public final ItemHandlerSimple invOutput = this.itemManager.addInvHandler("output", 1, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
   public String currentRecipeId = "";
   public IProgrammingRecipe currentRecipe;
   public List<ItemStack> options = new ArrayList<>();
   public int optionId = -1;
   public long syncedTarget;
   private long lastSyncedTarget = -1L;
   private int lastSyncedOptionId = -2;
   private String lastSyncedRecipeId = null;
   private ItemStack lastInput = ItemStack.EMPTY;

   public TileProgrammingTable(BlockPos pos, BlockState state) {
      super(BCSiliconBlockEntities.PROGRAMMING_TABLE, pos, state);
   }

   @Override
   protected int getLaserBufferMultiplier() {
      return 5;
   }

   public boolean hasWork() {
      return this.currentRecipe != null && this.optionId >= 0 && this.invOutput.getStackInSlot(0).isEmpty();
   }

   @Override
   public long getTarget() {
      if (this.level != null && this.level.isClientSide()) {
         return this.syncedTarget;
      }

      if (!this.hasWork() || this.optionId >= this.options.size()) {
         return 0L;
      }

      return this.currentRecipe.getEnergyCostMj(this.options.get(this.optionId));
   }

   public void findRecipe() {
      String oldId = this.currentRecipeId;
      this.currentRecipeId = "";
      ItemStack input = this.invInput.getStackInSlot(0);
      IProgrammingRecipe recipe = BuildcraftRecipeRegistry.programmingTable.getRecipeFor(input);
      if (recipe != null) {
         this.currentRecipeId = recipe.getId();
      }

      if (oldId == null || !oldId.equals(this.currentRecipeId)) {
         this.optionId = -1;
         this.updateRecipe();
         this.syncClientState();
      }
   }

   public void updateRecipe() {
      this.currentRecipe = BuildcraftRecipeRegistry.programmingTable.getRecipe(this.currentRecipeId);
      this.options = new ArrayList<>();
      if (this.currentRecipe != null) {
         this.options.addAll(this.currentRecipe.getOptions(WIDTH, HEIGHT));
      }
   }

   public void selectOption(int index) {
      if (this.options == null || this.options.isEmpty()) {
         this.optionId = -1;
      } else if (index == this.optionId) {
         this.optionId = -1;
      } else if (index >= 0 && index < this.options.size()) {
         this.optionId = index;
      } else {
         this.optionId = -1;
      }

      this.syncClientState();
   }

   private void syncClientState() {
      if (this.level != null && !this.level.isClientSide()) {
         long target = this.getTarget();
         boolean changed = target != this.lastSyncedTarget
            || this.optionId != this.lastSyncedOptionId
            || !this.currentRecipeId.equals(this.lastSyncedRecipeId == null ? "" : this.lastSyncedRecipeId);
         if (changed) {
            this.syncedTarget = target;
            this.lastSyncedTarget = target;
            this.lastSyncedOptionId = this.optionId;
            this.lastSyncedRecipeId = this.currentRecipeId;
            this.setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
         }
      }
   }

   @Override
   public void serverTick() {
      super.serverTick();
      ItemStack input = this.invInput.getStackInSlot(0);
      if (!ItemStack.isSameItemSameComponents(input, this.lastInput)) {
         this.lastInput = input.copy();
         this.findRecipe();
      }

      if (this.currentRecipe == null) {
         return;
      }

      if (this.invInput.getStackInSlot(0).isEmpty()) {
         this.currentRecipe = null;
         this.currentRecipeId = "";
         this.options.clear();
         this.optionId = -1;
         this.syncClientState();
         return;
      }

      long target = this.getTarget();
      if (this.optionId >= 0 && target > 0L && this.power >= target && this.currentRecipe.canCraft(this.invInput.getStackInSlot(0))) {
         ItemStack result = this.currentRecipe.craft(this.invInput.getStackInSlot(0), this.options.get(this.optionId));
         if (!result.isEmpty()) {
            this.power = 0L;
            this.invInput.setStackInSlot(0, ItemStack.EMPTY);
            ItemStack output = this.invOutput.getStackInSlot(0);
            if (output.isEmpty()) {
               this.invOutput.setStackInSlot(0, result);
            } else if (ItemStack.isSameItemSameComponents(output, result)) {
               output = output.copy();
               output.grow(result.getCount());
               this.invOutput.setStackInSlot(0, output);
            }

            this.findRecipe();
            this.setChanged();
         }
      }

      this.syncClientState();
   }

   public void onInputChanged() {
      this.findRecipe();
   }

   public ItemStack getOptionStack(int index) {
      if (this.options == null || index < 0 || index >= this.options.size()) {
         return ItemStack.EMPTY;
      }

      return this.options.get(index);
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putString("recipe_id", this.currentRecipeId);
      output.putInt("option_id", this.optionId);
      output.putLong("synced_target", this.syncedTarget);
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.currentRecipeId = input.getStringOr("recipe_id", "");
      this.optionId = input.getIntOr("option_id", -1);
      this.syncedTarget = input.getLongOr("synced_target", 0L);
      this.lastSyncedTarget = this.syncedTarget;
      this.lastSyncedOptionId = this.optionId;
      this.lastSyncedRecipeId = this.currentRecipeId;
      this.updateRecipe();
   }

}
