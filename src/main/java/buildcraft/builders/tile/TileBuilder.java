/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.builders.tile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;

import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.container.ContainerBuilder;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BlueprintBuilder;
import buildcraft.builders.snapshot.EnumContainerContentsMode;
import buildcraft.builders.snapshot.EnumFluidHandlingMode;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.ITileForBlueprintBuilder;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.TemplateBuilder;

public class TileBuilder extends TileBC_Neptune
    implements IDebuggable, ITileForTemplateBuilder, ITileForBlueprintBuilder, MenuProvider,
    buildcraft.lib.fabric.menu.BlockEntityExtendedMenu {

    public static final int RESOURCE_SLOTS = 27;
    public static final int TANK_COUNT = 4;
    public static final int TANK_CAPACITY = 8 * 1000;

    private static final Identifier ADVANCEMENT_PAVING_THE_WAY =
        Identifier.parse("buildcraftbuilders:paving_the_way");
    private static final Identifier ADVANCEMENT_START_OF_SOMETHING_BIG =
        Identifier.parse("buildcraftbuilders:start_of_something_big");

    public static final long BIG_STRUCTURE_THRESHOLD = 1024L;

    private final MjBattery battery = new MjBattery(16000 * MjAPI.MJ);
    private final MjBatteryReceiver mjReceiver = new MjBatteryReceiver(battery);
    private boolean canExcavate = true;
    private EnumFluidHandlingMode fluidMode = EnumFluidHandlingMode.NO_REPLACE;
    private EnumContainerContentsMode containerContentsMode = EnumContainerContentsMode.INCLUDE;

    public List<BlockPos> path = null;

    private List<BlockPos> basePoses = new ArrayList<>();
    private int currentBasePosIndex = 0;
    private Snapshot snapshot = null;
    public EnumSnapshotType snapshotType = null;
    private Template.BuildingInfo templateBuildingInfo = null;
    private Blueprint.BuildingInfo blueprintBuildingInfo = null;
    @SuppressWarnings("WeakerAccess")
    public TemplateBuilder templateBuilder = new TemplateBuilder(this);
    @SuppressWarnings("WeakerAccess")
    public BlueprintBuilder blueprintBuilder = new BlueprintBuilder(this);
    private Box currentBox = new Box();
    private Rotation rotation = null;

    private boolean isDone = false;

    private boolean wasDoneLastTick = false;

    private long bigStructureCellsBuilt = 0L;

    private boolean pavingTheWayGranted = false;
    private boolean startOfSomethingBigGranted = false;

    private ItemStack invSnapshot = ItemStack.EMPTY;
    private final NonNullList<ItemStack> invResources = NonNullList.withSize(RESOURCE_SLOTS, ItemStack.EMPTY);

    private final FluidStacksResourceHandler[] tanks = new FluidStacksResourceHandler[] {
        makeDirtyingTank(),
        makeDirtyingTank(),
        makeDirtyingTank(),
        makeDirtyingTank(),
    };

    private FluidStacksResourceHandler makeDirtyingTank() {
        return new FluidStacksResourceHandler(1, TANK_CAPACITY) {
            @Override
            public int insert(int slot, FluidResource resource, int amount, TransactionContext ctx) {
                int moved = super.insert(slot, resource, amount, ctx);
                if (moved > 0) setChanged();
                return moved;
            }

            @Override
            public int extract(int slot, FluidResource resource, int amount, TransactionContext ctx) {
                int moved = super.extract(slot, resource, amount, ctx);
                if (moved > 0) setChanged();
                return moved;
            }
        };
    }

    private final ResourceHandler<FluidResource> tankManager = new ResourceHandler<>() {
        @Override
        public int size() {
            return tanks.length;
        }

        @Override
        public FluidResource getResource(int slot) {
            return slot >= 0 && slot < tanks.length ? tanks[slot].getResource(0) : FluidResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int slot) {
            return slot >= 0 && slot < tanks.length ? tanks[slot].getAmountAsLong(0) : 0;
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return slot >= 0 && slot < tanks.length ? tanks[slot].getCapacityAsLong(0, resource) : 0;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return slot >= 0 && slot < tanks.length && tanks[slot].isValid(0, resource);
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext ctx) {
            return slot >= 0 && slot < tanks.length ? tanks[slot].insert(0, resource, amount, ctx) : 0;
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext ctx) {
            return slot >= 0 && slot < tanks.length ? tanks[slot].extract(0, resource, amount, ctx) : 0;
        }
    };

    private final ResourceHandler<ItemResource> pipeItemHandler = new ResourceHandler<>() {
        @Override
        public int size() { return invResources.size(); }

        @Override
        public ItemResource getResource(int slot) {
            if (slot < 0 || slot >= invResources.size()) return ItemResource.EMPTY;
            ItemStack stack = invResources.get(slot);
            return stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack);
        }

        @Override
        public long getAmountAsLong(int slot) {
            if (slot < 0 || slot >= invResources.size()) return 0;
            return invResources.get(slot).getCount();
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            if (slot < 0 || slot >= invResources.size()) return 0;

            return resource == null || resource.isEmpty() ? 64 : resource.toStack(1).getMaxStackSize();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return slot >= 0 && slot < invResources.size();
        }

        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext ctx) {
            if (slot < 0 || slot >= invResources.size() || resource == null || resource.isEmpty() || amount <= 0) return 0;
            ItemStack existing = invResources.get(slot);
            int maxStack = getCapacityAsInt(resource);
            if (existing.isEmpty()) {
                int moved = Math.min(amount, maxStack);
                ItemStack placed = resource.toStack(moved);

                invResources.set(slot, placed);
                onResourcesChanged();
                return moved;
            }
            if (!ItemStack.isSameItemSameComponents(existing, resource.toStack(1))) return 0;
            int space = Math.min(maxStack, existing.getMaxStackSize()) - existing.getCount();
            if (space <= 0) return 0;
            int moved = Math.min(space, amount);
            existing.grow(moved);
            onResourcesChanged();
            return moved;
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext ctx) {

            return 0;
        }

        private int getCapacityAsInt(ItemResource resource) {
            return (int) Math.min(Integer.MAX_VALUE, getCapacityAsLong(0, resource));
        }
    };

    private final IItemTransactor invResourcesTransactor = new IItemTransactor() {
        @Override
        public ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;

            ItemStack remaining = stack.copy();
            NonNullList<ItemStack> scratch = simulate || allOrNone
                ? copyInventory()
                : invResources;

            for (int i = 0; i < scratch.size() && !remaining.isEmpty(); i++) {
                ItemStack slot = scratch.get(i);
                if (slot.isEmpty()) continue;
                if (!ItemStack.isSameItemSameComponents(slot, remaining)) continue;
                int max = Math.min(slot.getMaxStackSize(), remaining.getMaxStackSize());
                int space = max - slot.getCount();
                if (space <= 0) continue;
                int moved = Math.min(space, remaining.getCount());
                slot.grow(moved);
                remaining.shrink(moved);
                scratch.set(i, slot);
            }

            for (int i = 0; i < scratch.size() && !remaining.isEmpty(); i++) {
                ItemStack slot = scratch.get(i);
                if (!slot.isEmpty()) continue;
                int moved = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                ItemStack placed = remaining.copyWithCount(moved);
                scratch.set(i, placed);
                remaining.shrink(moved);
            }

            if (allOrNone && !remaining.isEmpty()) {
                return stack;
            }

            if (!simulate && (simulate || allOrNone)) {

                for (int i = 0; i < scratch.size(); i++) {
                    invResources.set(i, scratch.get(i));
                }
            }

            if (!simulate) {
                onResourcesChanged();
            }
            return remaining;
        }

        @Override
        public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
            if (max <= 0) return ItemStack.EMPTY;

            ItemStack accumulated = ItemStack.EMPTY;
            NonNullList<ItemStack> scratch = copyInventory();

            for (int i = 0; i < scratch.size() && accumulated.getCount() < max; i++) {
                ItemStack slot = scratch.get(i);
                if (slot.isEmpty()) continue;
                if (filter != null && !filter.matches(slot)) continue;

                if (accumulated.isEmpty()) {
                    int take = Math.min(max, slot.getCount());
                    accumulated = slot.copyWithCount(take);
                    slot.shrink(take);
                    scratch.set(i, slot.isEmpty() ? ItemStack.EMPTY : slot);
                } else if (ItemStack.isSameItemSameComponents(accumulated, slot)) {
                    int want = max - accumulated.getCount();
                    int take = Math.min(want, slot.getCount());
                    accumulated.grow(take);
                    slot.shrink(take);
                    scratch.set(i, slot.isEmpty() ? ItemStack.EMPTY : slot);
                }
            }

            if (accumulated.getCount() < min) {
                return ItemStack.EMPTY;
            }

            if (!simulate) {
                for (int i = 0; i < scratch.size(); i++) {
                    invResources.set(i, scratch.get(i));
                }
                onResourcesChanged();
            }
            return accumulated;
        }
    };

    @SuppressWarnings("this-escape")
    public TileBuilder(BlockPos pos, BlockState state) {
        super(BCBuildersBlockEntities.BUILDER, pos, state);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        buildcraft.builders.BCBuildersEventDist.INSTANCE.invalidateBuilder(this);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        buildcraft.builders.BCBuildersEventDist.INSTANCE.validateBuilder(this);
    }

    public MjBatteryReceiver getMjReceiver() {
        return mjReceiver;
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandler(Direction facing) {
        return pipeItemHandler;
    }

    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        if (level == null || level.isClientSide()) return;

        super.onPlacedBy(placer, stack);

        Direction facing = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        BlockEntity inFront = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
        if (inFront instanceof IPathProvider provider) {
            ImmutableList<BlockPos> copiedPath = ImmutableList.copyOf(provider.getPath());
            if (copiedPath.size() >= 2) {
                path = copiedPath;
                provider.removeFromWorld();
            }
        }
        updateBasePoses();
        updateSnapshot(true);
    }

    public void tick() {
        if (level == null) return;

        if (level.isClientSide()) {

            SnapshotBuilder<?> b = getBuilder();
            if (b != null) {
                b.clientTick();
            }
            return;
        }

        if (snapshot == null && !invSnapshot.isEmpty() && invSnapshot.getItem() instanceof ItemSnapshot) {
            Snapshot.Header header = ItemSnapshot.getHeader(invSnapshot);
            if (header != null) {
                Snapshot resolved = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
                if (resolved != null) {
                    snapshot = resolved;
                    snapshotType = resolved.getType();
                    if (basePoses.isEmpty()) {
                        updateBasePoses();
                    }
                    updateSnapshot(false);
                }
            }
        }

        battery.tick(level, worldPosition);
        SnapshotBuilder<?> builder = getBuilder();

        if (builder != null && getBuildingInfo() != null) {

            if (level.getGameTime() % 5 == 1) {
                builder.onNetworkSync();
            }
            isDone = builder.tick();
            boolean justCompletedBasePos = isDone && !wasDoneLastTick;
            wasDoneLastTick = isDone;
            if (isDone) {

                builder.onNetworkSync();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

                if (justCompletedBasePos) {
                    tryGrantBuilderAdvancements();
                }
                if (currentBasePosIndex < basePoses.size() - 1) {
                    currentBasePosIndex++;
                    if (currentBasePosIndex >= basePoses.size()) {
                        currentBasePosIndex = basePoses.size() - 1;
                    }
                    updateSnapshot(true);
                }
            }

            if (level.getGameTime() % 5 == 0) {
                MessageUtil.sendUpdateToTrackingPlayers(this);
            }
        }
    }

    boolean shouldGrantPavingTheWay() {
        return path != null
            && path.size() >= 2
            && !basePoses.isEmpty()
            && currentBasePosIndex == basePoses.size() - 1;
    }

    private void tryGrantBuilderAdvancements() {
        if (level == null || level.isClientSide() || getOwner() == null) {
            return;
        }
        java.util.UUID ownerId = getOwner().id();
        if (!startOfSomethingBigGranted && snapshot != null) {
            bigStructureCellsBuilt += snapshot.countNonAirCells();
            if (bigStructureCellsBuilt >= BIG_STRUCTURE_THRESHOLD) {
                if (AdvancementUtil.unlockAdvancement(ownerId, level, ADVANCEMENT_START_OF_SOMETHING_BIG)) {
                    startOfSomethingBigGranted = true;
                }
            }
        }
        if (!pavingTheWayGranted && shouldGrantPavingTheWay()) {
            if (AdvancementUtil.unlockAdvancement(ownerId, level, ADVANCEMENT_PAVING_THE_WAY)) {
                pavingTheWayGranted = true;
            }
        }
    }

    private void updateSnapshot(boolean canGetFacing) {
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::cancel);
        if (snapshot != null && getCurrentBasePos() != null) {
            snapshotType = snapshot.getType();
            if (canGetFacing) {
                rotation = Arrays.stream(Rotation.values())
                    .filter(r -> r.rotate(snapshot.facing) == getBlockState().getValue(HorizontalDirectionalBlock.FACING))
                    .findFirst().orElse(null);
            }
            if (snapshot.getType() == EnumSnapshotType.TEMPLATE) {
                templateBuildingInfo = ((Template) snapshot).new BuildingInfo(getCurrentBasePos(), rotation);
            }
            if (snapshot.getType() == EnumSnapshotType.BLUEPRINT) {
                blueprintBuildingInfo = ((Blueprint) snapshot).new BuildingInfo(getCurrentBasePos(), rotation);

                if (containerContentsMode == EnumContainerContentsMode.IGNORE) {
                    blueprintBuildingInfo.refreshRequiredItemsForContentsMode(containerContentsMode);
                }
            }
            currentBox = Optional.ofNullable(getBuildingInfo()).map(buildingInfo -> buildingInfo.box).orElse(null);
            Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
        } else {
            snapshotType = null;
            rotation = null;
            templateBuildingInfo = null;
            blueprintBuildingInfo = null;
            currentBox = null;
        }
        if (currentBox == null) {
            currentBox = new Box();
        }
        syncBlockStateToSnapshot();
    }

    private void updateBasePoses() {
        basePoses.clear();
        if (path != null) {
            int max = path.size() - 1;
            basePoses.add(path.get(0));
            for (int i = 1; i <= max; i++) {
                basePoses.addAll(PositionUtil.getAllOnPath(path.get(i - 1), path.get(i)));
            }
        } else {
            basePoses.add(worldPosition.relative(
                getBlockState().getValue(HorizontalDirectionalBlock.FACING).getOpposite()));
        }
    }

    private BlockPos getCurrentBasePos() {
        return currentBasePosIndex < basePoses.size() ? basePoses.get(currentBasePosIndex) : null;
    }

    public void onSnapshotSlotChanged(ItemStack newStack) {
        if (level == null || level.isClientSide()) return;
        currentBasePosIndex = 0;
        snapshot = null;
        if (newStack.getItem() instanceof ItemSnapshot) {
            Snapshot.Header header = ItemSnapshot.getHeader(newStack);
            if (header != null) {
                Snapshot newSnapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
                if (newSnapshot != null) {
                    snapshot = newSnapshot;
                }
            }
        }
        if (basePoses.isEmpty()) {
            updateBasePoses();
        }
        updateSnapshot(true);
    }

    private void syncBlockStateToSnapshot() {
        if (level == null || level.isClientSide()) return;
        BlockState cur = getBlockState();
        if (!cur.hasProperty(BlockBuilder.SNAPSHOT_TYPE)) return;
        EnumOptionalSnapshotType desired = EnumOptionalSnapshotType.fromNullable(snapshotType);
        if (cur.getValue(BlockBuilder.SNAPSHOT_TYPE) != desired) {
            level.setBlock(worldPosition, cur.setValue(BlockBuilder.SNAPSHOT_TYPE, desired), 3);
        }
    }

    public void onResourcesChanged() {
        setChanged();
        if (level != null && level.isClientSide()) return;
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
    }

    public ItemStack getSnapshot() {
        return invSnapshot;
    }

    public void setSnapshot(ItemStack stack) {
        invSnapshot = stack;
        onSnapshotSlotChanged(stack);
        setChanged();
    }

    public ItemStack getResource(int slot) {
        return slot >= 0 && slot < invResources.size() ? invResources.get(slot) : ItemStack.EMPTY;
    }

    public void setResource(int slot, ItemStack stack) {
        if (slot < 0 || slot >= invResources.size()) return;
        invResources.set(slot, stack);
        onResourcesChanged();
    }

    public FluidStacksResourceHandler getTank(int i) {
        return (i >= 0 && i < tanks.length) ? tanks[i] : null;
    }

    private NonNullList<ItemStack> copyInventory() {
        NonNullList<ItemStack> copy = NonNullList.withSize(invResources.size(), ItemStack.EMPTY);
        for (int i = 0; i < invResources.size(); i++) {
            copy.set(i, invResources.get(i).copy());
        }
        return copy;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("battery_mj", battery.getStored());
        output.putBoolean("canExcavate", canExcavate);
        output.putInt("fluidMode", fluidMode.ordinal());
        output.putInt("containerContentsMode", containerContentsMode.ordinal());
        output.putInt("currentBasePosIndex", currentBasePosIndex);
        if (rotation != null) {
            output.putInt("rotation", rotation.ordinal());
        }

        if (currentBox.isInitialized()) {
            output.putBoolean("box_initialized", true);
            BlockPos bMin = currentBox.min();
            BlockPos bMax = currentBox.max();
            output.putInt("box_minX", bMin.getX());
            output.putInt("box_minY", bMin.getY());
            output.putInt("box_minZ", bMin.getZ());
            output.putInt("box_maxX", bMax.getX());
            output.putInt("box_maxY", bMax.getY());
            output.putInt("box_maxZ", bMax.getZ());
        } else {
            output.putBoolean("box_initialized", false);
        }

        if (!invSnapshot.isEmpty()) {
            output.store("invSnapshot", ItemStack.CODEC, invSnapshot);
        }
        for (int i = 0; i < invResources.size(); i++) {
            ItemStack stack = invResources.get(i);
            if (!stack.isEmpty()) {
                output.store("invRes_" + i, ItemStack.CODEC, stack);
            }
        }

        for (int i = 0; i < tanks.length; i++) {
            FluidResource res = tanks[i].getResource(0);
            if (!res.isEmpty()) {
                Identifier id = BuiltInRegistries.FLUID.getKey(res.getFluid());
                if (id != null) {
                    output.putString("tank_" + i + "_fluid", id.toString());
                    output.putInt("tank_" + i + "_amount", (int) tanks[i].getAmountAsLong(0));
                }
            }
        }

        if (snapshotType != null) {
            output.putInt("snapshotType", snapshotType.ordinal());
        }

        if (path != null) {
            output.putInt("path_count", path.size());
            for (int i = 0; i < path.size(); i++) {
                BlockPos p = path.get(i);
                output.putInt("path_" + i + "_x", p.getX());
                output.putInt("path_" + i + "_y", p.getY());
                output.putInt("path_" + i + "_z", p.getZ());
            }
        }

        output.putLong("bigStructureCellsBuilt", bigStructureCellsBuilt);
        output.putBoolean("pavingTheWayGranted", pavingTheWayGranted);
        output.putBoolean("startOfSomethingBigGranted", startOfSomethingBigGranted);
        output.putBoolean("wasDoneLastTick", wasDoneLastTick);

        SnapshotBuilder<?> activeBuilder = getBuilder();
        if (activeBuilder != null) {
            output.store("builderState", CompoundTag.CODEC, activeBuilder.serializeNBT());
            output.store("builderClientData", CompoundTag.CODEC, activeBuilder.serializeClientNBT());
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long stored = input.getLongOr("battery_mj", 0L);

        battery.setStored(stored);
        canExcavate = input.getBooleanOr("canExcavate", true);
        fluidMode = EnumFluidHandlingMode.fromOrdinal(input.getIntOr("fluidMode", 0));
        containerContentsMode = EnumContainerContentsMode.fromOrdinal(input.getIntOr("containerContentsMode", 0));
        currentBasePosIndex = input.getIntOr("currentBasePosIndex", 0);
        int rotOrdinal = input.getIntOr("rotation", -1);
        if (rotOrdinal >= 0 && rotOrdinal < Rotation.values().length) {
            rotation = Rotation.values()[rotOrdinal];
        }

        if (input.getBooleanOr("box_initialized", false)) {
            int minX = input.getIntOr("box_minX", 0);
            int minY = input.getIntOr("box_minY", 0);
            int minZ = input.getIntOr("box_minZ", 0);
            int maxX = input.getIntOr("box_maxX", 0);
            int maxY = input.getIntOr("box_maxY", 0);
            int maxZ = input.getIntOr("box_maxZ", 0);
            currentBox.reset();
            currentBox.setMin(new BlockPos(minX, minY, minZ));
            currentBox.setMax(new BlockPos(maxX, maxY, maxZ));
        }

        invSnapshot = input.read("invSnapshot", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        for (int i = 0; i < invResources.size(); i++) {
            invResources.set(i, input.read("invRes_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }

        for (int i = 0; i < tanks.length; i++) {
            try (Transaction tx = Transaction.openRoot()) {
                FluidResource existing = tanks[i].getResource(0);
                if (!existing.isEmpty()) {
                    tanks[i].extract(0, existing, Integer.MAX_VALUE, tx);
                }
                String fluidId = input.getStringOr("tank_" + i + "_fluid", "");
                if (!fluidId.isEmpty()) {
                    Identifier id = Identifier.tryParse(fluidId);
                    if (id != null) {
                        Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
                        if (fluid != null && fluid != Fluids.EMPTY) {
                            int amount = input.getIntOr("tank_" + i + "_amount", 0);
                            if (amount > 0) {
                                tanks[i].insert(0, FluidResource.of(fluid), amount, tx);
                            }
                        }
                    }
                }
                tx.commit();
            }
        }

        bigStructureCellsBuilt = input.getLongOr("bigStructureCellsBuilt", 0L);
        pavingTheWayGranted = input.getBooleanOr("pavingTheWayGranted", false);
        startOfSomethingBigGranted = input.getBooleanOr("startOfSomethingBigGranted", false);
        wasDoneLastTick = input.getBooleanOr("wasDoneLastTick", false);

        int pathCount = input.getIntOr("path_count", 0);
        if (pathCount >= 2) {
            ImmutableList.Builder<BlockPos> rebuilt = ImmutableList.builder();
            for (int i = 0; i < pathCount; i++) {
                rebuilt.add(new BlockPos(
                    input.getIntOr("path_" + i + "_x", 0),
                    input.getIntOr("path_" + i + "_y", 0),
                    input.getIntOr("path_" + i + "_z", 0)
                ));
            }
            path = rebuilt.build();
        } else {
            path = null;
        }

        if (level != null && level.isClientSide()) {
            int stOrdinal = input.getIntOr("snapshotType", -1);
            if (stOrdinal >= 0 && stOrdinal < EnumSnapshotType.values().length) {
                snapshotType = EnumSnapshotType.values()[stOrdinal];
            }
        }

        List<SnapshotBuilder<?>.BreakTask> savedBreak = new ArrayList<>();
        List<SnapshotBuilder<?>.PlaceTask> savedPlace = new ArrayList<>();
        if (level != null && level.isClientSide()) {
            SnapshotBuilder<?> prev = getBuilder();
            if (prev != null) {
                savedBreak.addAll(prev.clientBreakTasks);
                savedPlace.addAll(prev.clientPlaceTasks);
            }
        }

        if (!invSnapshot.isEmpty() && invSnapshot.getItem() instanceof ItemSnapshot) {
            Snapshot.Header header = ItemSnapshot.getHeader(invSnapshot);
            if (header != null && level != null) {
                Snapshot newSnapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
                if (newSnapshot != null) {
                    snapshot = newSnapshot;
                    snapshotType = newSnapshot.getType();
                    updateBasePoses();
                    updateSnapshot(false);
                }
            }
        }

        SnapshotBuilder<?> active = getBuilder();
        if (active != null) {
            input.read("builderState", CompoundTag.CODEC).ifPresent(active::deserializeNBT);
            input.read("builderClientData", CompoundTag.CODEC).ifPresent(tag ->
                applyBuilderClientData(active, tag, savedBreak, savedPlace)
            );
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void applyBuilderClientData(SnapshotBuilder active, CompoundTag tag,
            List savedBreak, List savedPlace) {
        active.loadClientNBT(tag, savedBreak, savedPlace);
    }

    public Box getBox() {
        return currentBox;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("basePoses = " + (basePoses == null ? "null" : basePoses.size()));
        left.add("currentBasePosIndex = " + currentBasePosIndex);
        left.add("isDone = " + isDone);
        left.add("snapshotType = " + snapshotType);
    }

    @Override
    public Level getWorldBC() {
        return level;
    }

    @Override
    public MjBattery getBattery() {
        return battery;
    }

    @Override
    public BlockPos getBuilderPos() {
        return worldPosition;
    }

    @Override
    public boolean canExcavate() {
        return canExcavate;
    }

    @Override
    public EnumFluidHandlingMode getFluidMode() {
        return fluidMode;
    }

    public void cycleFluidMode() {
        fluidMode = fluidMode.next();
        setChanged();
        SnapshotBuilder<?> b = getBuilder();
        if (b != null) {
            b.invalidateChecksForFluidPositions();
        }
    }

    @Override
    public EnumContainerContentsMode getContainerContentsMode() {
        return containerContentsMode;
    }

    public void cycleContainerContentsMode() {
        containerContentsMode = containerContentsMode.next();
        setChanged();
        if (blueprintBuildingInfo != null) {
            blueprintBuildingInfo.refreshRequiredItemsForContentsMode(containerContentsMode);
        }
        SnapshotBuilder<?> b = getBuilder();
        if (b != null) {
            b.resourcesChanged();

            if (b instanceof BlueprintBuilder bb) {
                bb.refreshDisplayForContentsMode();
            }
        }
    }

    @Override
    public SnapshotBuilder<?> getBuilder() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return templateBuilder;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return blueprintBuilder;
        }
        return null;
    }

    private Snapshot.BuildingInfo getBuildingInfo() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return templateBuildingInfo;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return blueprintBuildingInfo;
        }
        return null;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return templateBuildingInfo;
    }

    @Override
    public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
        return blueprintBuildingInfo;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResourcesTransactor;
    }

    @Override
    public ResourceHandler<FluidResource> getTankManager() {
        return tankManager;
    }

    @Override
    public ItemStack getBreakingTool() {
        return new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);
    }

    @Override
    public void onBlockBroken(BlockPos brokenPos, java.util.List<ItemStack> drops, int xp,
            buildcraft.lib.fluids.FluidStack capturedFluid) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        for (ItemStack stack : drops) {
            if (stack.isEmpty()) continue;
            ItemStack remaining = invResourcesTransactor.insert(stack.copy(), false, false);
            if (!remaining.isEmpty()) {
                net.minecraft.world.level.block.Block.popResource(serverLevel, brokenPos, remaining);
            }
        }
        if (xp > 0) {
            net.minecraft.world.entity.ExperienceOrb.award(
                    serverLevel,
                    net.minecraft.world.phys.Vec3.atCenterOf(worldPosition),
                    xp);
        }

        if (!capturedFluid.isEmpty() && getFluidMode() == buildcraft.builders.snapshot.EnumFluidHandlingMode.CLEAR) {
            try (buildcraft.lib.transfer.transaction.Transaction tx =
                    buildcraft.lib.transfer.transaction.Transaction.openRoot()) {
                tankManager.insert(
                        buildcraft.lib.transfer.fluid.FluidResource.of(capturedFluid),
                        capturedFluid.getAmount(),
                        tx);
                tx.commit();
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftbuilders.builder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new ContainerBuilder(containerId, playerInv, this);
    }
}
