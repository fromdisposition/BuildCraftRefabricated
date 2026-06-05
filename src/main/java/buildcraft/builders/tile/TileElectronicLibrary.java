/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.tile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.nbt.NbtSquisher;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.container.ContainerElectronicLibrary;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;

public class TileElectronicLibrary extends TileBC_Neptune implements MenuProvider,
        buildcraft.lib.fabric.menu.BlockEntityExtendedMenu {

    public final ItemHandlerSimple invDownIn = itemManager.addInvHandler(
        "downIn", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSnapshot snap && snap.isUsed(),
        EnumAccess.INSERT, EnumPipePart.VALUES
    );

    public final ItemHandlerSimple invDownOut = itemManager.addInvHandler(
        "downOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES
    );

    public final ItemHandlerSimple invUpIn = itemManager.addInvHandler(
        "upIn", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSnapshot,
        EnumAccess.INSERT, EnumPipePart.VALUES
    );

    public final ItemHandlerSimple invUpOut = itemManager.addInvHandler(
        "upOut", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES
    );

    @Nullable
    public Snapshot.Key selected = null;

    public int progressDown = -1;
    public int progressUp = -1;

    private final Set<Player> watchingPlayers = new HashSet<>();

    public TileElectronicLibrary(BlockPos pos, BlockState state) {
        super(BCBuildersBlockEntities.LIBRARY, pos, state);
    }

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (level != null && !level.isClientSide()) {
            setChanged();

            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void onPlayerOpen(Player player) {
        super.onPlayerOpen(player);
        watchingPlayers.add(player);
    }

    @Override
    public void onPlayerClose(Player player) {
        super.onPlayerClose(player);
        watchingPlayers.remove(player);
    }

    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!invDownIn.getStackInSlot(0).isEmpty() && invDownOut.getStackInSlot(0).isEmpty()) {
            if (progressDown == -1) {
                progressDown = 0;
            }
            if (progressDown >= 50) {
                ItemStack inStack = invDownIn.getStackInSlot(0);

                invDownOut.setStackInSlot(0, inStack.copy());
                invDownIn.setStackInSlot(0, ItemStack.EMPTY);
                progressDown = -1;
                setChanged();

                broadcastDownload(inStack);
            } else {
                progressDown++;
            }
        } else if (progressDown != -1) {
            progressDown = -1;
        }

        if (selected != null && !invUpIn.getStackInSlot(0).isEmpty() && invUpOut.getStackInSlot(0).isEmpty()) {
            if (progressUp == -1) {
                progressUp = 0;
            }
            if (progressUp >= 50) {

                requestUpload();
                progressUp = -1;
            } else {
                progressUp++;
            }
        } else if (progressUp != -1) {
            progressUp = -1;
        }
    }

    private void broadcastDownload(ItemStack usedItem) {
        Snapshot.Header header = ItemSnapshot.getHeader(usedItem);
        if (header == null) return;

        Snapshot snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
        if (snapshot == null) return;

        Snapshot snapshotWithHeader = snapshot.copy();
        snapshotWithHeader.key = new Snapshot.Key(snapshot.key, header);

        byte[] data;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtSquisher.squishVanilla(Snapshot.writeToNBT(snapshotWithHeader), baos);
            data = baos.toByteArray();
        } catch (IOException e) {
            BCLog.logger.warn("[library] Failed to serialize snapshot for download broadcast", e);
            return;
        }

        for (Player p : watchingPlayers) {
            if (p.containerMenu instanceof ContainerElectronicLibrary container) {
                container.sendDownloadData(data);
            }
        }
    }

    private void requestUpload() {
        if (selected == null) return;
        final Snapshot.Key key = selected;
        for (Player p : watchingPlayers) {
            if (p.containerMenu instanceof ContainerElectronicLibrary container) {
                container.sendMessage(ContainerElectronicLibrary.NET_UPLOAD_REQUEST, buf -> key.writeToByteBuf(buf));
            }
        }
    }

    public void onUploadReceived(Snapshot snapshot) {
        GlobalSavedDataSnapshots.get(level).addSnapshot(snapshot);
        Snapshot.Header header = snapshot.key.header;
        if (header == null) {
            header = new Snapshot.Header(snapshot.key, new UUID(0, 0), new java.util.Date(), "Snapshot");
        }
        EnumSnapshotType type = snapshot.getType();
        ItemSnapshot usedItem = type == EnumSnapshotType.BLUEPRINT
            ? BCBuildersItems.BLUEPRINT_USED.get()
            : BCBuildersItems.TEMPLATE_USED.get();
        invUpOut.setStackInSlot(0, usedItem.createUsedStack(header));
        invUpIn.setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("progressDown", progressDown);
        output.putInt("progressUp", progressUp);
        output.store("items", CompoundTag.CODEC, itemManager.serializeNBT());
        if (selected != null) {
            output.store("selected", CompoundTag.CODEC, selected.serializeNBT());
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progressDown = input.getIntOr("progressDown", -1);
        progressUp = input.getIntOr("progressUp", -1);
        input.read("items", CompoundTag.CODEC).ifPresent(itemManager::deserializeNBT);
        selected = input.read("selected", CompoundTag.CODEC)
            .map(Snapshot.Key::new)
            .orElse(null);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("tile.buildcraftbuilders.library.name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new ContainerElectronicLibrary(containerId, playerInv, this);
    }
}
