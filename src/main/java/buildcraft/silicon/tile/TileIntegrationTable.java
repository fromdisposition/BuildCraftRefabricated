/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlockEntities;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileIntegrationTable extends TileLaserTableBase {
   public final ItemHandlerSimple invTarget = this.itemManager.addInvHandler("target", 1, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
   public final ItemHandlerSimple invToIntegrate = this.itemManager.addInvHandler("toIntegrate", 8, ItemHandlerManager.EnumAccess.BOTH, EnumPipePart.VALUES);
   public final ItemHandlerSimple invResult = this.itemManager.addInvHandler("result", 1, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
   public IntegrationRecipe recipe;
   public long syncedTarget;
   public ItemStack syncedOutput = ItemStack.EMPTY;
   private long lastSyncedTarget = -1L;
   private ItemStack lastSyncedOutput = ItemStack.EMPTY;

   public TileIntegrationTable(BlockPos pos, BlockState state) {
      super(BCSiliconBlockEntities.INTEGRATION_TABLE, pos, state);
   }

   private boolean extract(IngredientStack item, ImmutableList<IngredientStack> items, boolean simulate) {
      ItemStack targetStack = this.invTarget.getStackInSlot(0);
      if (targetStack.isEmpty()) {
         return false;
      }

      if (!StackUtil.contains(new ItemStack(targetStack.getItem(), item.count), targetStack)) {
         return false;
      }

      if (!item.ingredient.test(targetStack)) {
         return false;
      }

      if (!this.extract(this.invToIntegrate, items, simulate, true)) {
         return false;
      }

      if (!simulate) {
         targetStack = targetStack.copy();
         targetStack.setCount(targetStack.getCount() - item.count);
         this.invTarget.setStackInSlot(0, targetStack);
      }

      return true;
   }

   private boolean isSpaceEnough(ItemStack stack) {
      ItemStack output = this.invResult.getStackInSlot(0);
      return output.isEmpty() || StackUtil.canMerge(stack, output) && stack.getCount() + output.getCount() <= stack.getMaxStackSize();
   }

   private void updateRecipe() {
      if (this.recipe != null) {
         ItemStack output = this.getOutput();
         if (!output.isEmpty() && this.extract(this.recipe.getCenterStack(), this.recipe.getRequirements(output), true)) {
            return;
         }
      }

      this.recipe = IntegrationRecipeRegistry.INSTANCE.getRecipeFor(this.invTarget.getStackInSlot(0), this.invToIntegrate.stacks);
   }

   public ItemStack getOutput() {
      if (this.level != null && this.level.isClientSide() && this.recipe == null) {
         return this.syncedOutput;
      } else {
         return this.recipe != null ? this.recipe.getOutput(this.invTarget.getStackInSlot(0), this.invToIntegrate.stacks) : ItemStack.EMPTY;
      }
   }

   @Override
   public long getTarget() {
      if (this.level != null && this.level.isClientSide()) {
         return this.syncedTarget;
      }

      ItemStack output = this.getOutput();
      return this.recipe != null && this.isSpaceEnough(output) ? this.recipe.getRequiredMicroJoules(output) : 0L;
   }

   private void syncClientState() {
      if (this.level != null && !this.level.isClientSide()) {
         long target = this.recipe != null && this.isSpaceEnough(this.getOutput()) ? this.recipe.getRequiredMicroJoules(this.getOutput()) : 0L;
         ItemStack output = this.recipe != null ? this.recipe.getOutput(this.invTarget.getStackInSlot(0), this.invToIntegrate.stacks) : ItemStack.EMPTY;
         boolean changed = target != this.lastSyncedTarget || !ItemStack.matches(this.lastSyncedOutput, output);
         if (changed) {
            this.syncedTarget = target;
            this.syncedOutput = output.copy();
            this.lastSyncedTarget = target;
            this.lastSyncedOutput = this.syncedOutput.copy();
            this.setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
         }
      }
   }

   @Override
   public void serverTick() {
      super.serverTick();
      this.updateRecipe();
      this.syncClientState();
      long target = this.getTarget();
      if (target > 0L && this.power >= target) {
         ItemStack output = this.getOutput();
         if (!output.isEmpty() && this.recipe != null) {
            this.extract(this.recipe.getCenterStack(), this.recipe.getRequirements(output), false);
            ItemStack result = this.invResult.getStackInSlot(0);
            if (!result.isEmpty()) {
               result = result.copy();
               result.setCount(result.getCount() + output.getCount());
            } else {
               result = output.copy();
            }

            this.invResult.setStackInSlot(0, result);
            this.power -= target;
            this.recipe = null;
            this.setChanged();
            this.updateRecipe();
            this.syncClientState();
         }
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putLong("synced_target", this.syncedTarget);
      output.store("synced_output", ItemStack.CODEC, this.syncedOutput);
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.recipe = null;
      this.syncedTarget = input.getLongOr("synced_target", 0L);
      this.syncedOutput = input.read("synced_output", ItemStack.CODEC).orElse(ItemStack.EMPTY);
      this.lastSyncedTarget = this.syncedTarget;
      this.lastSyncedOutput = this.syncedOutput.copy();
   }
}
