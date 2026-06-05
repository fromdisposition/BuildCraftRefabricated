/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.craft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerSimple;

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

    public WorkbenchCrafting(int width, int height, TileBC_Neptune tile, ItemHandlerSimple invBlueprint,
        ItemHandlerSimple invMaterials, ItemHandlerSimple invResult) {
        this.width = width;
        this.height = height;
        this.tile = tile;
        this.invBlueprint = invBlueprint;
        if (invBlueprint.getSlots() < width * height) {
            throw new IllegalArgumentException("Passed blueprint has a smaller size than width * height! ( expected "
                + (width * height) + ", got " + invBlueprint.getSlots() + ")");
        }
        this.invMaterials = invMaterials;
        this.invResult = invResult;
    }

    public int getSize() {
        return width * height;
    }

    public ItemStack getAssumedResult() {
        return assumedResult;
    }

    public void onInventoryChange(ItemHandlerSimple inv) {
        if (inv == invBlueprint) {
            isBlueprintDirty = true;
        } else if (inv == invMaterials) {
            areMaterialsDirty = true;
        }
    }

    private CraftingInput createBlueprintInput() {
        List<ItemStack> items = new ArrayList<>(getSize());
        for (int s = 0; s < getSize(); s++) {
            items.add(invBlueprint.getStackInSlot(s));
        }
        return CraftingInput.of(width, height, items);
    }

    public boolean tick() {
        if (tile.getLevel().isClientSide()) {
            throw new IllegalStateException("Never call this on the client side!");
        }
        if (isBlueprintDirty) {
            CraftingInput input = createBlueprintInput();
            currentRecipe = CraftingUtil.findMatchingRecipe(input, tile.getLevel());
            if (currentRecipe == null) {
                assumedResult = ItemStack.EMPTY;
            } else {

                assumedResult = currentRecipe.value().assemble(input);

            }
            isBlueprintDirty = false;
            areMaterialsDirty = true;
            return true;
        }
        return false;
    }

    public boolean canCraft() {
        if (currentRecipe == null || isBlueprintDirty) {
            return false;
        }
        if (!invResult.canFullyAccept(assumedResult)) {
            return false;
        }
        if (areMaterialsDirty) {
            areMaterialsDirty = false;
            cachedHasRequirements = hasExactStacks();
        }
        return cachedHasRequirements;
    }

    public boolean craft() {
        if (isBlueprintDirty) {
            return false;
        }
        return craftExact();
    }

    private boolean hasExactStacks() {
        Map<ItemStackKey, Integer> required = new HashMap<>();
        for (int s = 0; s < getSize(); s++) {
            ItemStack req = invBlueprint.getStackInSlot(s);
            if (!req.isEmpty()) {
                ItemStack singleReq = req.copyWithCount(1);
                ItemStackKey key = new ItemStackKey(singleReq);
                required.merge(key, req.getCount(), Integer::sum);
            }
        }
        for (Map.Entry<ItemStackKey, Integer> entry : required.entrySet()) {
            ArrayStackFilter filter = new ArrayStackFilter(entry.getKey().baseStack);
            int count = entry.getValue();
            ItemStack inInventory = invMaterials.extract(filter, count, count, true);
            if (inInventory.isEmpty() || inInventory.getCount() < count) {
                return false;
            }
        }
        return true;
    }

    private boolean craftExact() {

        NonNullList<ItemStack> gridContents = NonNullList.withSize(getSize(), ItemStack.EMPTY);

        for (int s = 0; s < getSize(); s++) {
            ItemStack bpt = invBlueprint.getStackInSlot(s);
            if (!bpt.isEmpty()) {
                ItemStack stack = invMaterials.extract(new ArrayStackFilter(bpt), 1, 1, false);
                if (stack.isEmpty()) {

                    returnItemsToMaterials(gridContents);
                    return false;
                }
                gridContents.set(s, stack);
            }
        }

        CraftingInput craftInput = CraftingInput.of(width, height, gridContents);
        if (!currentRecipe.value().matches(craftInput, tile.getLevel())) {
            returnItemsToMaterials(gridContents);
            return false;
        }

        ItemStack result = currentRecipe.value().assemble(craftInput);

        if (result.isEmpty()) {
            returnItemsToMaterials(gridContents);
            return false;
        }

        ItemStack leftover = invResult.insert(result, false, false);
        if (!leftover.isEmpty()) {
            InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
        }

        NonNullList<ItemStack> remainingStacks = currentRecipe.value().getRemainingItems(craftInput);

        for (int s = 0; s < gridContents.size(); s++) {
            gridContents.set(s, ItemStack.EMPTY);
        }

        for (int s = 0; s < remainingStacks.size(); s++) {
            ItemStack remaining = remainingStacks.get(s);
            if (!remaining.isEmpty()) {
                leftover = invMaterials.insert(remaining, false, false);
                if (!leftover.isEmpty()) {
                    InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
                }
            }
        }

        return true;
    }

    private void returnItemsToMaterials(NonNullList<ItemStack> gridContents) {
        for (int s = 0; s < gridContents.size(); s++) {
            ItemStack inSlot = gridContents.get(s);
            if (!inSlot.isEmpty()) {
                ItemStack leftover = invMaterials.insert(inSlot, false, false);
                if (!leftover.isEmpty()) {
                    InventoryUtil.addToBestAcceptor(tile.getLevel(), tile.getBlockPos(), null, leftover);
                }
                gridContents.set(s, ItemStack.EMPTY);
            }
        }
    }
}
