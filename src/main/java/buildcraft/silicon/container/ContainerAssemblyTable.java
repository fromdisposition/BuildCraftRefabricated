/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.lib.integration.jei.JeiTransferUtil;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.EnumAssemblyRecipeState;
import buildcraft.silicon.tile.TileAssemblyTable;

@SuppressWarnings("this-escape")
public class ContainerAssemblyTable extends ContainerBCTile<TileAssemblyTable> {

    public ContainerAssemblyTable(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv.player, getTile(playerInv, pos));
    }

    public ContainerAssemblyTable(int containerId, Player player, TileAssemblyTable tile) {
        super(BCSiliconMenuTypes.ASSEMBLY_TABLE, containerId, player, tile);

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotBase(tile.inv, x + y * 3, 8 + x * 18, 36 + y * 18));
            }
        }

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotDisplay(this::getDisplay, x + y * 3, 116 + x * 18, 36 + y * 18));
            }
        }

        addFullPlayerInventory(8, 123);
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        if (tile == null) return false;
        var keys = new ArrayList<>(tile.recipesStates.keySet());
        if (index < 0 || index >= keys.size()) return false;

        TileAssemblyTable.AssemblyInstruction instruction = keys.get(index);
        EnumAssemblyRecipeState current = tile.recipesStates.get(instruction);

        if (current == EnumAssemblyRecipeState.POSSIBLE) {

            tile.recipesStates.put(instruction, EnumAssemblyRecipeState.SAVED);
        } else if (current == EnumAssemblyRecipeState.SAVED) {

            tile.recipesStates.put(instruction, EnumAssemblyRecipeState.POSSIBLE);
        } else if (current == EnumAssemblyRecipeState.PAUSED) {

            tile.recipesStates.put(instruction, EnumAssemblyRecipeState.SAVED);
        } else {

            tile.recipesStates.put(instruction, EnumAssemblyRecipeState.PAUSED);
        }
        return true;
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        if (id == NET_JEI_TRANSFER_ITEMS && !isClient && tile != null) {

            boolean maxTransfer = buffer.readBoolean();
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                CompoundTag tag = buffer.readNbt();
                ItemStack want = tag == null ? ItemStack.EMPTY : NBTUtilBC.itemStackFromNBT(tag);
                if (want.isEmpty()) continue;
                int limit = maxTransfer ? Integer.MAX_VALUE : want.getCount();
                JeiTransferUtil.moveMatchingToHandler(player.getInventory(), want, limit, tile.inv);
            }
            return;
        }
        super.readMessage(id, buffer, isClient, ctx);
    }

    private ItemStack getDisplay(int index) {
        return index < tile.recipesStates.size()
                ? new ArrayList<>(tile.recipesStates.keySet()).get(index).output
                : ItemStack.EMPTY;
    }

    private static TileAssemblyTable getTile(Inventory inv, BlockPos pos) {
        var be = inv.player.level().getBlockEntity(pos);
        return be instanceof TileAssemblyTable t ? t : null;
    }
}

