/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders.tile;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.SchematicBlockManager;
import buildcraft.builders.snapshot.SchematicEntityManager;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Snapshot.Header;
import buildcraft.builders.snapshot.Template;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;

public class TileArchitectTable extends TileBC_Neptune implements IDebuggable, MenuProvider,
        buildcraft.lib.fabric.menu.BlockEntityExtendedMenu {

    private static final net.minecraft.resources.Identifier ADVANCEMENT
        = net.minecraft.resources.Identifier.parse("buildcraftbuilders:architect");

    private EnumSnapshotType snapshotType = EnumSnapshotType.BLUEPRINT;
    public final Box box = new Box();
    public boolean markerBox = false;
    private BitSet templateScannedBlocks;
    private final List<ISchematicBlock> blueprintScannedPalette = new ArrayList<>();
    private int[] blueprintScannedData;
    private final List<ISchematicEntity> blueprintScannedEntities = new ArrayList<>();
    private boolean isValid = false;
    private boolean scanning = false;
    public String name = "<unnamed>";

    private int scanX, scanY, scanZ;
    private boolean scanInitialized = false;

    public final ItemHandlerSimple invSnapshotIn = itemManager.addInvHandler(
        "in", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSnapshot,
        EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invSnapshotOut = itemManager.addInvHandler(
        "out", 1,
        EnumAccess.EXTRACT, EnumPipePart.VALUES);

    private int scanProgress = 0;
    private int scanTotal = 0;

    private static final int DROP_TICKS = 10;
    private int dropCountdown = 0;

    private final List<BlockPos> scannedThisTick = new ArrayList<>();

    @Nullable private Blueprint cachedLivePreview;
    private long livePreviewGeneratedTick = Long.MIN_VALUE;
    private static final int LIVE_PREVIEW_TTL_TICKS = 40;
    private static final int LIVE_PREVIEW_MAX_VOLUME = 32 * 32 * 32;

    public TileArchitectTable(BlockPos pos, BlockState state) {
        super(BCBuildersBlockEntities.ARCHITECT, pos, state);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        cachedLivePreview = null;
        buildcraft.builders.BCBuildersEventDist.INSTANCE.invalidateArchitectTable(this);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        buildcraft.builders.BCBuildersEventDist.INSTANCE.validateArchitectTable(this);
    }

    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        if (level == null || level.isClientSide()) return;

        cachedLivePreview = null;

        BlockState blockState = level.getBlockState(worldPosition);
        Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
        BlockPos offsetPos = worldPosition.relative(facing.getOpposite());

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
        VolumeBox volumeBox = volumeBoxes.getVolumeBoxAt(offsetPos);
        BlockEntity tile = level.getBlockEntity(offsetPos);

        if (volumeBox != null) {
            box.reset();
            box.setMin(volumeBox.box.min());
            box.setMax(volumeBox.box.max());
            isValid = true;
            volumeBox.locks.add(
                new Lock(
                    new Lock.Cause.CauseBlock(worldPosition, blockState.getBlock()),
                    new Lock.Target.TargetRemove(),
                    new Lock.Target.TargetResize(),
                    new Lock.Target.TargetUsedByMachine(
                        Lock.Target.TargetUsedByMachine.EnumType.STRIPES_READ
                    )
                )
            );
            volumeBoxes.markDirtyAndBroadcast();
        } else if (tile instanceof IAreaProvider provider) {
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
            markerBox = true;
            isValid = true;
            provider.removeFromWorld();
        } else {
            isValid = false;
        }

        super.onPlacedBy(placer, stack);

        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick() {
        if (level == null) return;
        if (level.isClientSide()) return;

        ItemStack stackIn = invSnapshotIn.getStackInSlot(0);
        if (!stackIn.isEmpty() && invSnapshotOut.getStackInSlot(0).isEmpty() && isValid) {
            if (!scanning) {
                if (stackIn.getItem() instanceof ItemSnapshot snapshotItem) {
                    snapshotType = snapshotItem.getSnapshotType();
                } else {
                    snapshotType = EnumSnapshotType.BLUEPRINT;
                }
                scanTotal = box.size().getX() * box.size().getY() * box.size().getZ();
                scanProgress = 0;
                scanning = true;
                scanInitialized = false;
                dropCountdown = 0;
            }
        } else {
            scanning = false;
            if (dropCountdown == 0) {
                scanProgress = 0;
                scanTotal = 0;
            }
        }

        if (scanning) {
            scanMultipleBlocks();
            if (!scanning) {
                if (snapshotType == EnumSnapshotType.BLUEPRINT) {
                    scanEntities();
                }
                finishScanning();
                dropCountdown = DROP_TICKS;
            }
        }

        if (dropCountdown > 0) {
            dropCountdown--;

            scanProgress = (int) ((long) scanTotal * dropCountdown / DROP_TICKS);
            if (dropCountdown == 0) {
                scanProgress = 0;
                scanTotal = 0;
            }
        }

        if (!scannedThisTick.isEmpty() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            buildcraft.lib.fabric.PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel,
                    new net.minecraft.world.level.ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4),
                    new buildcraft.builders.snapshot.ArchitectScanPayload(new ArrayList<>(scannedThisTick))
            );
            scannedThisTick.clear();
        }
    }

    private void scanMultipleBlocks() {
        int maxPerTick = snapshotType.maxPerTick;
        for (int i = maxPerTick; i > 0; i--) {
            scanSingleBlock();
            if (!scanning) {
                break;
            }
        }
    }

    private void scanSingleBlock() {
        BlockPos size = box.size();
        if (!scanInitialized) {
            templateScannedBlocks = new BitSet(Snapshot.getDataSize(size));
            blueprintScannedData = new int[Snapshot.getDataSize(size)];
            scanX = 0;
            scanY = 0;
            scanZ = 0;
            scanInitialized = true;
        }

        BlockPos min = box.min();
        BlockPos worldScanPos = new BlockPos(min.getX() + scanX, min.getY() + scanY, min.getZ() + scanZ);
        BlockPos schematicPos = new BlockPos(scanX, scanY, scanZ);
        scannedThisTick.add(worldScanPos);

        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            templateScannedBlocks.set(
                Snapshot.posToIndex(size, schematicPos),
                !level.isEmptyBlock(worldScanPos)
            );
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            ISchematicBlock schematicBlock = readSchematicBlock(worldScanPos);
            int index = blueprintScannedPalette.indexOf(schematicBlock);
            if (index == -1) {
                index = blueprintScannedPalette.size();
                blueprintScannedPalette.add(schematicBlock);
            }
            blueprintScannedData[Snapshot.posToIndex(size, schematicPos)] = index;
        }

        scanProgress++;

        scanX++;
        if (scanX >= size.getX()) {
            scanX = 0;
            scanZ++;
            if (scanZ >= size.getZ()) {
                scanZ = 0;
                scanY++;
                if (scanY >= size.getY()) {

                    scanning = false;
                    scanInitialized = false;
                }
            }
        }
    }

    private ISchematicBlock readSchematicBlock(BlockPos worldScanPos) {
        return SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(
            level,
            box.min(),
            worldScanPos,
            level.getBlockState(worldScanPos),
            level.getBlockState(worldScanPos).getBlock()
        ));
    }

    @Nullable
    public Blueprint getOrRefreshLivePreview() {
        if (level == null || level.isClientSide()) return null;
        if (!isValid || !box.isInitialized()) return null;

        BlockPos size = box.size();
        long volume = (long) size.getX() * size.getY() * size.getZ();
        if (volume <= 0 || volume > LIVE_PREVIEW_MAX_VOLUME) return null;

        long now = level.getGameTime();
        if (cachedLivePreview != null && now - livePreviewGeneratedTick < LIVE_PREVIEW_TTL_TICKS) {
            return cachedLivePreview;
        }

        BlockState thisState = level.getBlockState(worldPosition);
        if (thisState.getBlock() != BCBuildersBlocks.ARCHITECT.get()) return null;
        Direction facing = thisState.getValue(HorizontalDirectionalBlock.FACING);

        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        List<ISchematicBlock> palette = new ArrayList<>();
        int[] data = new int[Snapshot.getDataSize(size)];
        BlockPos min = box.min();

        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    BlockPos worldScanPos = new BlockPos(min.getX() + x, min.getY() + y, min.getZ() + z);
                    BlockPos schematicPos = new BlockPos(x, y, z);
                    ISchematicBlock sb = readSchematicBlock(worldScanPos);
                    int index = palette.indexOf(sb);
                    if (index == -1) {
                        index = palette.size();
                        palette.add(sb);
                    }
                    data[Snapshot.posToIndex(size, schematicPos)] = index;
                }
            }
        }

        Blueprint preview = new Blueprint();
        preview.size = size;
        preview.facing = facing;
        preview.offset = box.min().subtract(worldPosition.relative(facing.getOpposite()));
        preview.palette.addAll(palette);
        preview.data = data;

        preview.computeKey();

        cachedLivePreview = preview;
        livePreviewGeneratedTick = now;
        return preview;
    }

    private void scanEntities() {
        BlockPos min = box.min();
        BlockPos max = box.max();
        level.getEntities((Entity) null, new AABB(
                min.getX(), min.getY(), min.getZ(),
                max.getX() + 1, max.getY() + 1, max.getZ() + 1),
                entity -> true
            ).stream()
            .map(entity ->
                SchematicEntityManager.getSchematicEntity(new SchematicEntityContext(
                    level,
                    box.min(),
                    entity
                ))
            )
            .filter(Objects::nonNull)
            .forEach(blueprintScannedEntities::add);
    }

    private void finishScanning() {
        BlockState thisState = level.getBlockState(worldPosition);
        if (thisState.getBlock() != BCBuildersBlocks.ARCHITECT.get()) {
            return;
        }

        Direction facing = thisState.getValue(HorizontalDirectionalBlock.FACING);
        Snapshot snapshot = Snapshot.create(snapshotType);
        snapshot.size = box.size();
        snapshot.facing = facing;
        snapshot.offset = box.min().subtract(worldPosition.relative(facing.getOpposite()));

        if (snapshot instanceof Template) {
            ((Template) snapshot).data = templateScannedBlocks;
        }
        if (snapshot instanceof Blueprint) {
            ((Blueprint) snapshot).palette.addAll(blueprintScannedPalette);
            ((Blueprint) snapshot).data = blueprintScannedData;
            ((Blueprint) snapshot).entities.addAll(blueprintScannedEntities);
        }
        snapshot.computeKey();
        GlobalSavedDataSnapshots.get(level).addSnapshot(snapshot);

        ItemStack stackIn = invSnapshotIn.getStackInSlot(0);
        stackIn.shrink(1);
        invSnapshotIn.setStackInSlot(0, stackIn.isEmpty() ? ItemStack.EMPTY : stackIn);

        ItemSnapshot usedItem = (snapshotType == EnumSnapshotType.BLUEPRINT)
            ? BCBuildersItems.BLUEPRINT_USED.get()
            : BCBuildersItems.TEMPLATE_USED.get();
        invSnapshotOut.setStackInSlot(0, usedItem.createUsedStack(
            new Header(
                snapshot.key,
                getOwner() != null ? getOwner().id() : new java.util.UUID(0, 0),
                new Date(),
                name
            )
        ));

        templateScannedBlocks = null;
        blueprintScannedData = null;
        blueprintScannedEntities.clear();
        if (getOwner() != null) {
            AdvancementUtil.unlockAdvancement(getOwner().id(), level, ADVANCEMENT);
            String paperCriterion = (snapshotType == EnumSnapshotType.BLUEPRINT)
                ? buildcraft.core.PaperAdvancement.WRITE_TO_BLUEPRINT
                : buildcraft.core.PaperAdvancement.WRITE_TO_TEMPLATE;
            AdvancementUtil.unlockAdvancement(getOwner().id(), level,
                buildcraft.core.PaperAdvancement.ID, paperCriterion);
        }
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (box.isInitialized()) {
            output.putBoolean("box_initialized", true);
            BlockPos bMin = box.min();
            BlockPos bMax = box.max();
            output.putInt("box_minX", bMin.getX());
            output.putInt("box_minY", bMin.getY());
            output.putInt("box_minZ", bMin.getZ());
            output.putInt("box_maxX", bMax.getX());
            output.putInt("box_maxY", bMax.getY());
            output.putInt("box_maxZ", bMax.getZ());
        } else {
            output.putBoolean("box_initialized", false);
        }
        output.putBoolean("markerBox", markerBox);
        output.putBoolean("scanning", scanning);
        output.putInt("snapshotType", snapshotType.ordinal());
        output.putBoolean("isValid", isValid);
        output.putString("name", name);

        output.store("items", CompoundTag.CODEC, itemManager.serializeNBT());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (input.getBooleanOr("box_initialized", false)) {
            int minX = input.getIntOr("box_minX", 0);
            int minY = input.getIntOr("box_minY", 0);
            int minZ = input.getIntOr("box_minZ", 0);
            int maxX = input.getIntOr("box_maxX", 0);
            int maxY = input.getIntOr("box_maxY", 0);
            int maxZ = input.getIntOr("box_maxZ", 0);
            box.reset();
            box.setMin(new BlockPos(minX, minY, minZ));
            box.setMax(new BlockPos(maxX, maxY, maxZ));
        }
        markerBox = input.getBooleanOr("markerBox", false);
        scanning = input.getBooleanOr("scanning", false);
        int stOrd = input.getIntOr("snapshotType", 0);
        EnumSnapshotType[] stValues = EnumSnapshotType.values();
        snapshotType = (stOrd >= 0 && stOrd < stValues.length) ? stValues[stOrd] : EnumSnapshotType.BLUEPRINT;
        isValid = input.getBooleanOr("isValid", false);
        name = input.getStringOr("name", "<unnamed>");
        input.read("items", CompoundTag.CODEC).ifPresent(itemManager::deserializeNBT);

        if (invSnapshotIn.getStackInSlot(0).isEmpty()) {
            input.read("invSnapshotIn", ItemStack.CODEC)
                .ifPresent(s -> invSnapshotIn.setStackInSlot(0, s));
        }
        if (invSnapshotOut.getStackInSlot(0).isEmpty()) {
            input.read("invSnapshotOut", ItemStack.CODEC)
                .ifPresent(s -> invSnapshotOut.setStackInSlot(0, s));
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("box:");
        left.add(" - min = " + box.min());
        left.add(" - max = " + box.max());
        left.add("scanning = " + scanning);
        left.add("isValid = " + isValid);
        left.add("scanProgress = " + scanProgress + "/" + scanTotal);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftbuilders.architect");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new ContainerArchitectTable(containerId, playerInv, this);
    }

    public ItemStack getSnapshotIn() {
        return invSnapshotIn.getStackInSlot(0);
    }

    public void setSnapshotIn(ItemStack stack) {

        invSnapshotIn.setStackInSlot(0, stack);
    }

    public ItemStack getSnapshotOut() {
        return invSnapshotOut.getStackInSlot(0);
    }

    public void setSnapshotOut(ItemStack stack) {
        invSnapshotOut.setStackInSlot(0, stack);
    }

    public boolean isScanning() {
        return scanning;
    }

    public int getScanProgress() {
        return scanProgress;
    }

    public int getScanTotal() {
        return scanTotal;
    }

    public boolean getIsValid() {
        return isValid;
    }

    public AABB getRenderBoundingBox() {
        if (box.isInitialized()) {
            BlockPos min = box.min();
            BlockPos max = box.max();
            return new AABB(
                Math.min(worldPosition.getX(), min.getX()),
                Math.min(worldPosition.getY(), min.getY()),
                Math.min(worldPosition.getZ(), min.getZ()),
                Math.max(worldPosition.getX() + 1, max.getX() + 1),
                Math.max(worldPosition.getY() + 1, max.getY() + 1),
                Math.max(worldPosition.getZ() + 1, max.getZ() + 1)
            );
        }
        return new AABB(worldPosition);
    }
}
