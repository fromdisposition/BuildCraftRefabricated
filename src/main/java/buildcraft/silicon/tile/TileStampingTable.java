/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.item.ItemPackage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

public class TileStampingTable extends TileLaserTableBase {
   private static final long ENERGY_PER_CRAFT = 400L * MjAPI.MJ;
   public final ItemHandlerSimple invInput = this.itemManager.addInvHandler("input", 1, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
   public final ItemHandlerSimple invOutput = this.itemManager.addInvHandler("output", 4, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);

   public TileStampingTable(BlockPos pos, BlockState state) {
      super(BCSiliconBlockEntities.STAMPING_TABLE, pos, state);
   }

   @Override
   public long getTarget() {
      ItemStack input = this.invInput.getStackInSlot(0);
      if (input.isEmpty() || !this.hasOutputRoom()) {
         return 0L;
      }

      if (ItemPackage.isPackage(input)) {
         return ENERGY_PER_CRAFT * Math.max(1, ItemPackage.getContentCount(input));
      }

      return ENERGY_PER_CRAFT;
   }

   private boolean hasOutputRoom() {
      for (int i = 0; i < this.invOutput.getSlots(); i++) {
         ItemStack stack = this.invOutput.getStackInSlot(i);
         if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void serverTick() {
      super.serverTick();
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      long target = this.getTarget();
      if (target <= 0L || this.power < target) {
         return;
      }

      ItemStack input = this.invInput.getStackInSlot(0);
      CraftingInput craftInput = this.buildCraftInput(input);
      RecipeHolder<CraftingRecipe> recipe = CraftingUtil.findMatchingRecipe(craftInput, this.level);
      if (recipe != null) {
         //? if >= 26.1 {
         ItemStack result = recipe.value().assemble(craftInput).copy();
         //?} else {
         /*ItemStack result = recipe.value().assemble(craftInput, this.level.registryAccess()).copy();
         *///?}
         if (!result.isEmpty()) {
            this.power -= target;
            this.output(result);

            for (ItemStack remaining : recipe.value().getRemainingItems(craftInput)) {
               if (!remaining.isEmpty()) {
                  this.output(remaining.copy());
               }
            }

            ItemStack shrunk = input.copy();
            shrunk.shrink(1);
            this.invInput.setStackInSlot(0, shrunk.isEmpty() ? ItemStack.EMPTY : shrunk);
         }
      } else {
         this.rejectInput(input);
      }

      this.setChanged();
   }

   private CraftingInput buildCraftInput(ItemStack input) {
      List<ItemStack> grid = new ArrayList<>(9);

      if (ItemPackage.isPackage(input)) {
         for (int i = 0; i < 9; i++) {
            grid.add(ItemPackage.getStack(input, i));
         }
      } else {
         grid.add(input.copyWithCount(1));

         for (int i = 1; i < 9; i++) {
            grid.add(ItemStack.EMPTY);
         }
      }

      return CraftingInput.of(3, 3, grid);
   }

   private void output(ItemStack stack) {
      ItemStack leftover = this.invOutput.insert(stack, false, false);
      if (!leftover.isEmpty()) {
         InventoryUtil.addToBestAcceptor(this.level, this.getBlockPos(), null, leftover);
      }
   }

   private void rejectInput(ItemStack input) {
      for (int i = 0; i < this.invOutput.getSlots(); i++) {
         if (this.invOutput.getStackInSlot(i).isEmpty()) {
            this.invOutput.setStackInSlot(i, input.copy());
            this.invInput.setStackInSlot(0, ItemStack.EMPTY);
            return;
         }
      }
   }
}
