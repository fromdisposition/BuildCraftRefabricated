/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.tile.ITileOilSpring;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.factory.BCFactoryAttachments;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.mj.MjRedstoneBatteryReceiver;
import buildcraft.api.tiles.IDebuggable;

@SuppressWarnings("deprecation")
public class TilePump extends TileMiner implements IDebuggable {

    private static final Identifier ADVANCEMENT_DRAIN_ANY
        = Identifier.parse("buildcraftfactory:draining_the_world");
    private static final Identifier ADVANCEMENT_DRAIN_OIL
        = Identifier.parse("buildcraftfactory:oil_platform");
    private static final Identifier ADVANCEMENT_REFINE_AND_REDEFINE
        = Identifier.parse("buildcraftenergy:refine_and_redefine");

    private static final Direction[] SEARCH_NORMAL = new Direction[] {
        Direction.UP, Direction.NORTH, Direction.SOUTH,
        Direction.WEST, Direction.EAST
    };

    private static final Direction[] SEARCH_GASEOUS = new Direction[] {
        Direction.DOWN, Direction.NORTH, Direction.SOUTH,
        Direction.WEST, Direction.EAST
    };

    static final class FluidPath {
        public final BlockPos thisPos;
        @Nullable
        public final FluidPath parent;

        public FluidPath(BlockPos thisPos, FluidPath parent) {
            this.thisPos = thisPos;
            this.parent = parent;
        }

        public FluidPath and(BlockPos pos) {
            return new FluidPath(pos, this);
        }
    }

    private static final java.util.concurrent.atomic.AtomicLong CONFIG_REVISION
        = new java.util.concurrent.atomic.AtomicLong();

    public static void onConfigReloaded() {
        CONFIG_REVISION.incrementAndGet();
    }

    private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, 16 * 1000);
    private boolean queueBuilt = false;
    private long builtAtRevision = -1;
    private final Map<BlockPos, FluidPath> paths = new HashMap<>();
    private BlockPos fluidConnection;
    private final Deque<BlockPos> queue = new ArrayDeque<>();
    private boolean isInfiniteWaterSource;
    private int rebuildDelay = 0;

    private BlockPos targetPos;

    @Nullable
    private BlockPos oilSpringPos;

    public TilePump(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.PUMP, pos, state);
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjRedstoneBatteryReceiver(battery);
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }

    private void buildQueue() {
        builtAtRevision = CONFIG_REVISION.get();
        queue.clear();
        paths.clear();
        isInfiniteWaterSource = false;
        oilSpringPos = null;
        targetPos = worldPosition.below();

        ColumnProbe probe = probeDown(level, worldPosition, BCCoreConfig.miningMaxDepth.get());
        BlockPos oilPos = probe.firstOil();
        BlockPos springPos = probe.spring();

        BlockPos seed;
        if (oilPos != null) {
            seed = oilPos;
            oilSpringPos = springPos;
        } else if (springPos != null) {
            oilSpringPos = springPos;
            return;
        } else if (probe.firstFluid() != null) {
            seed = probe.firstFluid();
        } else {
            return;
        }

        Fluid queueFluid = BlockUtil.getFluidWithFlowing(level, seed);
        if (queueFluid == null) {
            return;
        }

        targetPos = seed;
        fluidConnection = seed;
        LongSet checked = new LongOpenHashSet();
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        nextPosesToCheck.add(seed);
        paths.put(seed, new FluidPath(seed, null));
        checked.add(seed.asLong());
        if (BlockUtil.getFluid(level, seed) != null) {
            queue.add(seed);
        }

        buildQueue0(queueFluid, nextPosesToCheck, checked);
    }

    private void creditRefineAndRedefineFromPumpedOil(FluidStack drain) {
        if (getOwner() == null || level == null || level.isClientSide()) return;
        net.minecraft.server.MinecraftServer server = level.getServer();
        if (server == null) return;
        net.minecraft.server.level.ServerPlayer player = server.getPlayerList().getPlayer(getOwner().id());
        if (player == null) return;
        String baseName = BCEnergyFluidsFabric.getBaseName(drain.getFluid());
        if (baseName == null) return;
        var tracker = BCFactoryAttachments.get(player);
        String justSaturated = tracker.recordProduction(baseName, drain.getAmount());
        if (justSaturated != null) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_REFINE_AND_REDEFINE, justSaturated);
        }
    }

    private static boolean isOil(Fluid fluid) {
        Identifier id = BuiltInRegistries.FLUID.getKey(fluid);

        return id.getNamespace().equals("buildcraftenergy")
            && (id.getPath().equals("oil") || id.getPath().startsWith("oil_heat_"));
    }

    public record ColumnProbe(BlockPos firstFluid, BlockPos firstOil, BlockPos spring) {
    }

    public static ColumnProbe probeDown(Level level, BlockPos pumpPos, int maxDepth) {
        BlockPos firstFluid = null;
        BlockPos firstOil = null;
        for (BlockPos pos = pumpPos.below(); !level.isOutsideBuildHeight(pos); pos = pos.below()) {
            if (pumpPos.getY() - pos.getY() > maxDepth) {
                break;
            }
            Fluid fluid = BlockUtil.getFluidWithFlowing(level, pos);
            if (fluid != null) {
                if (firstFluid == null) {
                    firstFluid = pos;
                }
                if (firstOil == null && isOil(fluid)) {
                    firstOil = pos;
                }
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(BCCoreBlocks.SPRING_OIL)) {
                return new ColumnProbe(firstFluid, firstOil, pos);
            }
            if (level.isEmptyBlock(pos) || state.is(BCFactoryBlocks.TUBE)) {
                continue;
            }
            break;
        }
        return new ColumnProbe(firstFluid, firstOil, null);
    }

    private void buildQueue0(Fluid queueFluid, List<BlockPos> nextPosesToCheck, LongSet checked) {
        Direction[] directions = FluidUtilBC.isGaseous(queueFluid) ? SEARCH_GASEOUS : SEARCH_NORMAL;
        boolean isWater = !BCCoreConfig.pumpsConsumeWater.get()
                && FluidUtilBC.areFluidsEqual(queueFluid, Fluids.WATER);

        boolean targetPosIsInfinite = isWater && isInfiniteSourceAt(level, targetPos);
        isInfiniteWaterSource = targetPosIsInfinite;
        final int maxLengthSquared = BCCoreConfig.pumpMaxDistance.get() * BCCoreConfig.pumpMaxDistance.get();

        outer:
        while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos posToCheck : nextPosesToCheckCopy) {
                for (Direction side : directions) {
                    BlockPos offsetPos = posToCheck.relative(side);
                    if (offsetPos.distSqr(targetPos) > maxLengthSquared) {
                        continue;
                    }
                    boolean isNew = checked.add(offsetPos.asLong());
                    if (isNew) {
                        Fluid fluidAt = BlockUtil.getFluidWithFlowing(level, offsetPos);
                        boolean eq = FluidUtilBC.areFluidsEqual(fluidAt, queueFluid);
                        if (eq) {
                            FluidPath oldPath = paths.get(posToCheck);
                            FluidPath path = new FluidPath(offsetPos, oldPath);
                            paths.put(offsetPos, path);
                            if (BlockUtil.getFluid(level, offsetPos) != null) {
                                queue.add(offsetPos);
                            }
                            nextPosesToCheck.add(offsetPos);
                        }
                    }
                }

                if (targetPosIsInfinite) {
                    break outer;
                }
            }
        }

        if (isOil(queueFluid) && oilSpringPos == null) {
            List<BlockPos> springPositions = new ArrayList<>();
            BlockPos center = VecUtil.replaceValue(worldPosition, Axis.Y, level.getMinY());
            for (BlockPos spring : BlockPos.betweenClosed(center.offset(-10, 0, -10), center.offset(10, 0, 10))) {
                if (level.getBlockState(spring).is(BCCoreBlocks.SPRING_OIL)) {
                    BlockEntity tile = level.getBlockEntity(spring);
                    if (tile instanceof ITileOilSpring) {
                        springPositions.add(spring.immutable());
                    }
                }
            }
            switch (springPositions.size()) {
                case 0:
                    break;
                case 1:
                    oilSpringPos = springPositions.get(0);
                    break;
                default:
                    springPositions.sort(Comparator.comparingDouble(worldPosition::distSqr));
                    oilSpringPos = springPositions.get(0);
            }
        }
    }

    private boolean canDrain(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluid(level, blockPos);
        if (tank.getAmountAsInt(0) == 0) {
            return fluid != null;
        }
        return FluidUtilBC.areFluidsEqual(fluid, tank.getResource(0).getFluid());
    }

    public static boolean isInfiniteSourceAt(@Nullable Level level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        Fluid fluidBelow = BlockUtil.getFluidWithFlowing(level, pos.below());
        if (!FluidUtilBC.areFluidsEqual(fluidBelow, Fluids.WATER) && !below.isSolid()) {
            return false;
        }
        int sources = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Fluid neighbour = BlockUtil.getFluid(level, pos.relative(dir));
            if (FluidUtilBC.areFluidsEqual(neighbour, Fluids.WATER)) {
                if (++sources >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private void nextPos() {
        while (!queue.isEmpty()) {
            currentPos = queue.removeLast();
            if (canDrain(currentPos)) {
                updateLength();
                return;
            }
        }
        currentPos = null;
        updateLength();
    }

    @Override
    @Nullable
    protected BlockPos getTargetPos() {

        if (queue.isEmpty() && currentPos == null) {
            return null;
        }
        return targetPos;
    }

    @Override
    public void serverTick() {
        if (!queueBuilt || builtAtRevision != CONFIG_REVISION.get()) {
            buildQueue();
            queueBuilt = true;
        }

        super.serverTick();

        FluidUtilBC.pushFluidToNeighbors(level, worldPosition, tank);
    }

    @Override
    protected void mine() {
        if (tank.getAmountAsInt(0) > tank.getCapacityAsInt(0, FluidResource.EMPTY) / 2) {
            return;
        }

        if (rebuildDelay > 0) {
            rebuildDelay--;
        }

        long target = 10 * MjAPI.MJ;
        if (currentPos != null && paths.containsKey(currentPos)) {
            progress += (int) battery.extractPower(0, target - progress);
            if (progress < target) {
                return;
            }

            FluidStack drain = BlockUtil.drainBlock(level, currentPos, false);

            drain_attempt: {
                if (drain == null) {
                    break drain_attempt;
                }

                BlockPos invalid = getFirstInvalidPointOnPath(currentPos);
                if (invalid != null) {
                    break drain_attempt;
                } else if (!canDrain(currentPos)) {
                    break drain_attempt;
                }

                int inserted;
                try (Transaction tx = Transaction.openRoot()) {
                    FluidResource drainedResource = FluidUtilBC.canonicalFluidResource(FluidResource.of(drain));
                    inserted = tank.insert(0, drainedResource, drain.getAmount(), tx);
                    if (inserted > 0) {
                        tx.commit();
                    }
                }
                if (inserted <= 0) {
                    break drain_attempt;
                }
                progress = 0;

                if (getOwner() != null) {
                    AdvancementUtil.unlockAdvancement(getOwner().id(), level, ADVANCEMENT_DRAIN_ANY);
                }

                isInfiniteWaterSource = !BCCoreConfig.pumpsConsumeWater.get()
                        && FluidUtilBC.areFluidsEqual(drain.getFluid(), Fluids.WATER)
                        && isInfiniteSourceAt(level, targetPos);

                if (!isInfiniteWaterSource) {
                    BlockUtil.drainBlock(level, currentPos, true);
                    if (isOil(drain.getFluid())) {
                        if (getOwner() != null) {
                            AdvancementUtil.unlockAdvancement(getOwner().id(), level, ADVANCEMENT_DRAIN_OIL);
                        }
                        creditRefineAndRedefineFromPumpedOil(drain);
                        if (oilSpringPos != null) {
                            BlockEntity tile = level.getBlockEntity(oilSpringPos);
                            if (tile instanceof ITileOilSpring oilSpring) {
                                oilSpring.onPumpOil(getOwner(), currentPos);
                            }
                        }
                    }
                    paths.remove(currentPos);
                    nextPos();
                }
                return;
            }

            if (rebuildDelay > 0) {
                return;
            }
            rebuildDelay = 30;
        } else {
            if (currentPos == null && rebuildDelay > 0) {
                return;
            }
            rebuildDelay = 30;
        }
        buildQueue();
        nextPos();
    }

    @Nullable
    private BlockPos getFirstInvalidPointOnPath(BlockPos from) {
        FluidPath path = paths.get(from);
        if (path == null) {
            return from;
        }
        do {
            if (BlockUtil.getFluidWithFlowing(level, path.thisPos) == null) {
                return path.thisPos;
            }
        } while ((path = path.parent) != null);
        return null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output);
        if (oilSpringPos != null) {
            output.putBoolean("hasOilSpring", true);
            output.putInt("oilSpringX", oilSpringPos.getX());
            output.putInt("oilSpringY", oilSpringPos.getY());
            output.putInt("oilSpringZ", oilSpringPos.getZ());
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input);
        if (input.getBooleanOr("hasOilSpring", false)) {
            oilSpringPos = new BlockPos(
                input.getIntOr("oilSpringX", 0),
                input.getIntOr("oilSpringY", 0),
                input.getIntOr("oilSpringZ", 0));
        } else {
            oilSpringPos = null;
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {

        left.add("battery = " + battery.getDebugString());
        left.add("current = " + currentPos);
        left.add("wantedLength = " + wantedLength);
        left.add("currentLength = " + currentLength);
        left.add("lastLength = " + lastLength);
        left.add("isComplete = " + isComplete());
        left.add("progress = " + MjAPI.formatMj((long) progress));

        left.add("fluid = " + FluidUtilBC.getDebugString(tank.getResource(0).toStack(tank.getAmountAsInt(0))));
        left.add("queue size = " + queue.size());
        left.add("infinite = " + isInfiniteWaterSource);
    }

    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("current = " + currentPos);
        left.add("wantedLength = " + wantedLength);
        left.add("currentLength = " + currentLength);
        left.add("isComplete = " + isComplete());
        left.add("progress = " + MjAPI.formatMj((long) progress));
        left.add("fluid = " + FluidUtilBC.getDebugString(tank.getResource(0).toStack(tank.getAmountAsInt(0))));
        left.add("queue size = " + queue.size());
        left.add("infinite = " + isInfiniteWaterSource);
    }

    @Override
    public void setRemoved() {

        super.setRemoved();
    }

    @Override
    protected long getBatteryCapacity() {
        return 50 * MjAPI.MJ;
    }

}
