/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.tile;

import java.util.Date;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;

import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.container.ContainerReplacer;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;

public class TileReplacer extends TileBC_Neptune implements MenuProvider,
        buildcraft.lib.fabric.menu.BlockEntityExtendedMenu {

    public final ItemHandlerSimple invSnapshot = itemManager.addInvHandler(
        "snapshot", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSnapshot snap && snap.isUsed()
            && snap.getSnapshotType() == EnumSnapshotType.BLUEPRINT,
        EnumAccess.NONE
    );

    public final ItemHandlerSimple invSchematicFrom = itemManager.addInvHandler(
        "schematicFrom", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSchematicSingle s && s.isUsed(),
        EnumAccess.NONE
    );

    public final ItemHandlerSimple invSchematicTo = itemManager.addInvHandler(
        "schematicTo", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSchematicSingle s && s.isUsed(),
        EnumAccess.NONE
    );

    public TileReplacer(BlockPos pos, BlockState state) {
        super(BCBuildersBlockEntities.REPLACER, pos, state);
    }

    public void doReplace(@Nullable String newName) {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (invSnapshot.getStackInSlot(0).isEmpty()
                || invSchematicFrom.getStackInSlot(0).isEmpty()
                || invSchematicTo.getStackInSlot(0).isEmpty()) {
            return;
        }
        Snapshot.Header header = ItemSnapshot.getHeader(invSnapshot.getStackInSlot(0));
        if (header == null) {
            return;
        }
        Snapshot snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
        if (!(snapshot instanceof Blueprint blueprint)) {
            return;
        }
        try {
            ISchematicBlock from = ItemSchematicSingle.getSchematic(invSchematicFrom.getStackInSlot(0));
            ISchematicBlock to = ItemSchematicSingle.getSchematic(invSchematicTo.getStackInSlot(0));
            if (from == null || to == null) {
                return;
            }
            Blueprint newBlueprint = blueprint.copy();
            newBlueprint.replace(from, to);
            newBlueprint.computeKey();
            GlobalSavedDataSnapshots.get(level).addSnapshot(newBlueprint);

            String resolvedName = (newName != null && !newName.isBlank()) ? newName.trim() : header.name;
            ItemSnapshot usedItem = BCBuildersItems.BLUEPRINT_USED.get();
            Snapshot.Header newHeader = new Snapshot.Header(
                    newBlueprint.key,
                    header.owner,
                    new Date(),
                    resolvedName
            );
            invSnapshot.setStackInSlot(0, usedItem.createUsedStack(newHeader));

            setChanged();
        } catch (InvalidInputDataException e) {
            BCLog.logger.warn("[builders.replacer] Invalid replacer blueprint data", e);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("items", CompoundTag.CODEC, itemManager.serializeNBT());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("items", CompoundTag.CODEC).ifPresent(itemManager::deserializeNBT);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftbuilders.replacer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new ContainerReplacer(containerId, playerInv, this);
    }

    @Deprecated
    public void tick() {

    }
}
