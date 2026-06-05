/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.misc.CraftingUtil;

@SuppressWarnings("this-escape")
public class ContainerAutoCraftItems extends ContainerBCTile<TileAutoWorkbenchItems> {
    private static final buildcraft.lib.tile.item.ItemHandlerSimple FALLBACK_RESULT = createFallbackHandler(1, false);
    private static final buildcraft.lib.tile.item.ItemHandlerSimple FALLBACK_BLUEPRINT = createFallbackHandler(9, true);
    private static final buildcraft.lib.tile.item.ItemHandlerSimple FALLBACK_MATERIAL_FILTER = createFallbackHandler(9, true);
    private static final buildcraft.lib.tile.item.ItemHandlerSimple FALLBACK_MATERIALS = createFallbackHandler(9, true);

    private final List<Slot> blueprintSlots = new ArrayList<>();
    private final Slot resultSlot;
    public final SlotBase[] materialSlots;

    public ContainerAutoCraftItems(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileAutoWorkbenchItems.class));
    }

    public ContainerAutoCraftItems(int containerId, Inventory playerInv, TileAutoWorkbenchItems tile) {
        super(BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS, containerId, playerInv.player, tile);

        buildcraft.lib.tile.item.ItemHandlerSimple invResult = tile != null ? tile.invResult : FALLBACK_RESULT;
        buildcraft.lib.tile.item.ItemHandlerSimple invBlueprint = tile != null ? tile.invBlueprint : FALLBACK_BLUEPRINT;
        buildcraft.lib.tile.item.ItemHandlerSimple invMaterialFilter =
                tile != null ? tile.invMaterialFilter : FALLBACK_MATERIAL_FILTER;
        buildcraft.lib.tile.item.ItemHandlerSimple invMaterials = tile != null ? tile.invMaterials : FALLBACK_MATERIALS;

        resultSlot = addSlot(new SlotOutput(invResult, 0, 124, 35));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                Slot slot = new SlotPhantom(invBlueprint, x + y * 3,
                        30 + x * 18, 17 + y * 18, false);
                addSlot(slot);
                blueprintSlots.add(slot);
            }
        }

        for (int x = 0; x < 9; x++) {
            addSlot(new SlotPhantom(invMaterialFilter, x, -1000000, -1000000));
        }

        materialSlots = new SlotBase[9];
        for (int x = 0; x < 9; x++) {
            materialSlots[x] = new SlotBase(invMaterials, x, 8 + x * 18, 84);
            addSlot(materialSlots[x]);
        }

        addSlot(new SlotDisplay(i -> tile != null ? tile.resultClient : ItemStack.EMPTY, 0, 93, 27));

        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return tile != null ? (int) (tile.getPowerStored() & 0xFFFFFFFFL) : 0; }
            @Override public void set(int value) {
                if (tile != null) {
                    long current = tile.getPowerStored();
                    tile.setPowerStored((current & 0xFFFFFFFF00000000L) | (value & 0xFFFFFFFFL));
                }
            }
        });
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return tile != null ? (int) (tile.getPowerStored() >>> 32) : 0; }
            @Override public void set(int value) {
                if (tile != null) {
                    long current = tile.getPowerStored();
                    tile.setPowerStored((current & 0x00000000FFFFFFFFL) | ((long) value << 32));
                }
            }
        });

        addFullPlayerInventory(8, 115);
    }

    public List<Slot> getInputGridSlots() {
        return blueprintSlots;
    }

    public int getGridWidth() {
        return 3;
    }

    public int getGridHeight() {
        return 3;
    }

    public Slot getResultSlot() {
        return resultSlot;
    }

    @Override
    public PostPlaceAction handlePlacement(boolean useMaxItems, boolean isCreative, RecipeHolder<?> recipe,
        ServerLevel level, Inventory playerInv) {
        if (tile == null) {
            return PostPlaceAction.NOTHING;
        }
        if (!(recipe.value() instanceof CraftingRecipe craftingRecipe)) {
            return PostPlaceAction.NOTHING;
        }
        CraftingUtil.placeRecipeInBlueprint(craftingRecipe, tile.invBlueprint, level);
        return PostPlaceAction.PLACE_GHOST_RECIPE;
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents contents) {
        if (tile == null) {
            return;
        }
        for (int i = 0; i < tile.invMaterials.getSlots(); i++) {
            contents.accountStack(tile.invMaterials.getStackInSlot(i));
        }
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    private static buildcraft.lib.tile.item.ItemHandlerSimple createFallbackHandler(int slots, boolean allowInput) {
        buildcraft.lib.tile.item.ItemHandlerSimple handler = new buildcraft.lib.tile.item.ItemHandlerSimple(slots, 1);
        handler.setChecker((slot, stack) -> allowInput);
        return handler;
    }

}
