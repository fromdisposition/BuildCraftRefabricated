/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.misc.CraftingUtil;

import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;

@SuppressWarnings("this-escape")
public class ContainerAdvancedCraftingTable extends ContainerBCTile<TileAdvancedCraftingTable> {

    private final List<Slot> blueprintSlots = new ArrayList<>();

    public ContainerAdvancedCraftingTable(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv.player, getTile(playerInv, pos));
    }

    public ContainerAdvancedCraftingTable(int containerId, Player player, TileAdvancedCraftingTable tile) {
        super(BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE, containerId, player, tile);

        addSlot(new buildcraft.lib.gui.slot.SlotDisplay(i -> tile.resultClient, 0, 127, 33));

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                addSlot(new SlotBase(tile.invMaterials, x + y * 5, 15 + x * 18, 85 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotOutput(tile.invResults, x + y * 3, 109 + x * 18, 85 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                Slot slot = new SlotPhantom(tile.invBlueprint, x + y * 3, 33 + x * 18, 16 + y * 18, false);
                addSlot(slot);
                blueprintSlots.add(slot);
            }
        }

        addFullPlayerInventory(8, 153);
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
        return this.slots.get(0);
    }

    @Override
    public PostPlaceAction handlePlacement(boolean useMaxItems, boolean isCreative, RecipeHolder<?> recipe,
        ServerLevel level, Inventory playerInv) {

        if (!(recipe.value() instanceof CraftingRecipe craftingRecipe)) {
            return PostPlaceAction.NOTHING;
        }

        CraftingUtil.placeRecipeInBlueprint(craftingRecipe, tile.invBlueprint, level);

        return PostPlaceAction.PLACE_GHOST_RECIPE;
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents contents) {

        for (int i = 0; i < tile.invMaterials.getSlots(); i++) {
            contents.accountStack(tile.invMaterials.getStackInSlot(i));
        }
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    private static TileAdvancedCraftingTable getTile(Inventory inv, BlockPos pos) {
        var be = inv.player.level().getBlockEntity(pos);
        return be instanceof TileAdvancedCraftingTable t ? t : null;
    }
}
