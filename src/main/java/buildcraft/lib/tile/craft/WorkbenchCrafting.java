/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.craft;

import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WorkbenchCrafting {
   private final BlockEntity tile;
   private final int width;
   private final int height;
   private final ItemHandlerSimple invBlueprint;
   private final ItemHandlerSimple invMaterials;
   private final ItemHandlerSimple invResult;
   private boolean isBlueprintDirty = true;
   private boolean areMaterialsDirty = true;
   private boolean cachedHasRequirements = false;
   @Nullable
   private RecipeHolder<CraftingRecipe> currentRecipe;
   private ItemStack assumedResult = ItemStack.EMPTY;

   public WorkbenchCrafting(
      int width, int height, BcBlockEntity tile, ItemHandlerSimple invBlueprint, ItemHandlerSimple invMaterials, ItemHandlerSimple invResult
   ) {
      this.width = width;
      this.height = height;
      this.tile = tile;
      this.invBlueprint = invBlueprint;
      if (invBlueprint.getSlots() < width * height) {
         throw new IllegalArgumentException(
            "Passed blueprint has a smaller size than width * height! ( expected " + width * height + ", got " + invBlueprint.getSlots() + ")"
         );
      }

      this.invMaterials = invMaterials;
      this.invResult = invResult;
   }

   public int getSize() {
      return this.width * this.height;
   }

   public ItemStack getAssumedResult() {
      return this.assumedResult;
   }

   public void onInventoryChange(ItemHandlerSimple inv) {
      if (inv == this.invBlueprint) {
         this.isBlueprintDirty = true;
      } else if (inv == this.invMaterials) {
         this.areMaterialsDirty = true;
      }
   }

   private CraftingInput createBlueprintInput() {
      List<ItemStack> items = new ArrayList<>(this.getSize());

      for (int s = 0; s < this.getSize(); s++) {
         items.add(this.invBlueprint.getStackInSlot(s));
      }

      return CraftingInput.of(this.width, this.height, items);
   }

   public boolean tick() {
      if (this.tile.getLevel().isClientSide()) {
         throw new IllegalStateException("Never call this on the client side!");
      }

      if (this.isBlueprintDirty) {
         CraftingInput input = this.createBlueprintInput();
         this.currentRecipe = CraftingUtil.findMatchingRecipe(input, this.tile.getLevel());
         if (this.currentRecipe == null) {
            this.assumedResult = ItemStack.EMPTY;
         } else {
            this.assumedResult = ((CraftingRecipe)this.currentRecipe.value()).assemble(input);
         }

         this.isBlueprintDirty = false;
         this.areMaterialsDirty = true;
         return true;
      } else {
         return false;
      }
   }

   public boolean canCraft() {
      if (this.currentRecipe == null || this.isBlueprintDirty) {
         return false;
      }

      if (!this.invResult.canFullyAccept(this.assumedResult)) {
         return false;
      }

      if (this.areMaterialsDirty) {
         this.areMaterialsDirty = false;
         this.cachedHasRequirements = this.hasExactStacks();
      }

      return this.cachedHasRequirements;
   }

   public boolean craft() {
      return this.isBlueprintDirty ? false : this.craftExact();
   }

   private boolean hasExactStacks() {
      Map<ItemStackKey, Integer> required = new HashMap<>();

      for (int s = 0; s < this.getSize(); s++) {
         ItemStack req = this.invBlueprint.getStackInSlot(s);
         if (!req.isEmpty()) {
            ItemStack singleReq = req.copyWithCount(1);
            ItemStackKey key = new ItemStackKey(singleReq);
            required.merge(key, req.getCount(), Integer::sum);
         }
      }

      for (Entry<ItemStackKey, Integer> entry : required.entrySet()) {
         ArrayStackFilter filter = new ArrayStackFilter(entry.getKey().baseStack);
         int count = entry.getValue();
         ItemStack inInventory = this.invMaterials.extract(filter, count, count, true);
         if (inInventory.isEmpty() || inInventory.getCount() < count) {
            return false;
         }
      }

      return true;
   }

   private boolean craftExact() {
      NonNullList<ItemStack> gridContents = NonNullList.withSize(this.getSize(), ItemStack.EMPTY);

      for (int s = 0; s < this.getSize(); s++) {
         ItemStack bpt = this.invBlueprint.getStackInSlot(s);
         if (!bpt.isEmpty()) {
            ItemStack stack = this.invMaterials.extract(new ArrayStackFilter(bpt), 1, 1, false);
            if (stack.isEmpty()) {
               this.returnItemsToMaterials(gridContents);
               return false;
            }

            gridContents.set(s, stack);
         }
      }

      CraftingInput craftInput = CraftingInput.of(this.width, this.height, gridContents);
      if (!((CraftingRecipe)this.currentRecipe.value()).matches(craftInput, this.tile.getLevel())) {
         this.returnItemsToMaterials(gridContents);
         return false;
      }

      ItemStack result = ((CraftingRecipe)this.currentRecipe.value()).assemble(craftInput);
      if (result.isEmpty()) {
         this.returnItemsToMaterials(gridContents);
         return false;
      }

      ItemStack leftover = this.invResult.insert(result, false, false);
      if (!leftover.isEmpty()) {
         InventoryUtil.addToBestAcceptor(this.tile.getLevel(), this.tile.getBlockPos(), null, leftover);
      }

      NonNullList<ItemStack> remainingStacks = ((CraftingRecipe)this.currentRecipe.value()).getRemainingItems(craftInput);

      for (int s = 0; s < gridContents.size(); s++) {
         gridContents.set(s, ItemStack.EMPTY);
      }

      for (int s = 0; s < remainingStacks.size(); s++) {
         ItemStack remaining = (ItemStack)remainingStacks.get(s);
         if (!remaining.isEmpty()) {
            leftover = this.invMaterials.insert(remaining, false, false);
            if (!leftover.isEmpty()) {
               InventoryUtil.addToBestAcceptor(this.tile.getLevel(), this.tile.getBlockPos(), null, leftover);
            }
         }
      }

      return true;
   }

   private void returnItemsToMaterials(NonNullList<ItemStack> gridContents) {
      for (int s = 0; s < gridContents.size(); s++) {
         ItemStack inSlot = (ItemStack)gridContents.get(s);
         if (!inSlot.isEmpty()) {
            ItemStack leftover = this.invMaterials.insert(inSlot, false, false);
            if (!leftover.isEmpty()) {
               InventoryUtil.addToBestAcceptor(this.tile.getLevel(), this.tile.getBlockPos(), null, leftover);
            }

            gridContents.set(s, ItemStack.EMPTY);
         }
      }
   }
}
