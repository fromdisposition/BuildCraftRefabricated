/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.block.BlockFloodGate;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.IBlockEntityLoadHook;

public class TileFloodGate extends BlockEntity implements IDebuggable, IBlockEntityLoadHook {

    private static final Direction[] SEARCH_NORMAL = new Direction[] {
        Direction.DOWN, Direction.NORTH, Direction.SOUTH,
        Direction.WEST, Direction.EAST
    };

    private static final Direction[] SEARCH_GASEOUS = new Direction[] {
        Direction.UP, Direction.NORTH, Direction.SOUTH,
        Direction.WEST, Direction.EAST
    };

    private static final int[] REBUILD_DELAYS = { 16, 32, 64, 128, 256 };

    private static final Identifier ADVANCEMENT_FLOODING_THE_WORLD =
        Identifier.parse("buildcraftfactory:flooding_the_world");

    private final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, 2000);
    public final EnumSet<Direction> openSides = EnumSet.of(
            Direction.DOWN, Direction.NORTH, Direction.SOUTH,
            Direction.WEST, Direction.EAST);
    public final Deque<BlockPos> queue = new ArrayDeque<>();
    private final Map<BlockPos, List<BlockPos>> paths = new HashMap<>();
    private int delayIndex = 0;
    private int tick = 0;
    private int lastSyncedAmount = 0;
    private FluidResource lastSyncedResource = FluidResource.EMPTY;

    private GameProfile owner;

    public TileFloodGate(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.FLOOD_GATE, pos, state);
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }

    @Nullable
    public GameProfile getOwner() {
        return owner;
    }

    public void onPlacedBy(@Nullable LivingEntity placer) {
        if (placer instanceof Player player) {
            owner = player.getGameProfile();
            setChanged();
        }
    }

    private int getCurrentDelay() {
        return REBUILD_DELAYS[delayIndex];
    }

    public void onSidesToggled() {
        queue.clear();
        delayIndex = 0;
        tick = 0;
    }

    private void buildQueue() {
        queue.clear();
        paths.clear();
        FluidResource fluid = tank.getResource(0);
        if (fluid.isEmpty()) {
            return;
        }
        Set<BlockPos> checked = new HashSet<>();
        checked.add(worldPosition);
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        for (Direction face : openSides) {
            BlockPos offset = worldPosition.relative(face);
            nextPosesToCheck.add(offset);
            paths.put(offset, ImmutableList.of(offset));
        }
        Direction[] directions = FluidUtilBC.isGaseous(fluid.toStack(1)) ? SEARCH_GASEOUS : SEARCH_NORMAL;

        outer:
        while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos toCheck : nextPosesToCheckCopy) {
                if (toCheck.distSqr(worldPosition) > 64 * 64) {
                    continue;
                }
                if (checked.add(toCheck)) {
                    if (canSearch(toCheck)) {
                        if (canFill(toCheck)) {
                            queue.push(toCheck);
                            if (queue.size() >= 4096) {
                                break outer;
                            }
                        }
                        List<BlockPos> checkPath = paths.get(toCheck);
                        for (Direction side : directions) {
                            BlockPos next = toCheck.relative(side);
                            if (checked.contains(next)) {
                                continue;
                            }
                            ImmutableList.Builder<BlockPos> pathBuilder = ImmutableList.builder();
                            pathBuilder.addAll(checkPath);
                            pathBuilder.add(next);
                            paths.put(next, pathBuilder.build());
                            nextPosesToCheck.add(next);
                        }
                    }
                }
            }
        }
    }

    private boolean canFill(BlockPos offsetPos) {
        if (level.isEmptyBlock(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, offsetPos);
        return fluid != null && FluidUtilBC.areFluidsEqual(fluid, tank.getResource(0).getFluid())
                && BlockUtil.getFluidWithoutFlowing(level.getBlockState(offsetPos)) == null;
    }

    private boolean canSearch(BlockPos offsetPos) {
        if (canFill(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluid(level, offsetPos);
        return FluidUtilBC.areFluidsEqual(fluid, tank.getResource(0).getFluid());
    }

    private boolean canFillThrough(BlockPos pos) {
        if (level.isEmptyBlock(pos)) {
            return false;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, pos);
        return FluidUtilBC.areFluidsEqual(fluid, tank.getResource(0).getFluid());
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        int currentAmount = tank.getAmountAsInt(0);
        FluidResource currentResource = tank.getResource(0);
        if (currentAmount != lastSyncedAmount
                || !FluidUtilBC.areEquivalentFluidResources(currentResource, lastSyncedResource)) {
            lastSyncedAmount = currentAmount;
            lastSyncedResource = currentResource;
            setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
        }

        if (tank.getAmountAsInt(0) < 1000) {
            return;
        }

        tick++;
        if (tick % 16 == 0) {
            if (!tank.getResource(0).isEmpty() && !queue.isEmpty()) {
                FluidResource res = tank.getResource(0);
                if (tank.getAmountAsInt(0) >= 1000) {
                    BlockPos currentPos = queue.removeLast();
                    List<BlockPos> path = paths.get(currentPos);
                    boolean canFill = true;
                    if (path != null) {
                        for (BlockPos p : path) {
                            if (p.equals(currentPos)) {
                                continue;
                            }
                            if (!canFillThrough(p)) {
                                canFill = false;
                                break;
                            }
                        }
                    }
                    if (canFill && canFill(currentPos)) {

                        Fluid fluidType = res.getFluid();
                        BlockState fluidBlock = fluidType.defaultFluidState().createLegacyBlock();
                        if (!fluidBlock.isAir()) {
                            level.setBlock(currentPos, fluidBlock, Block.UPDATE_ALL);
                            try (Transaction tx = Transaction.openRoot()) {
                                tank.extract(0, res, 1000, tx);
                                tx.commit();
                            }

                            if (owner != null) {
                                AdvancementUtil.unlockAdvancement(owner.id(), level, ADVANCEMENT_FLOODING_THE_WORLD);
                            }
                            delayIndex = 0;
                            tick = 0;
                        }
                    } else {
                        buildQueue();
                    }
                }
            }
        }

        if (queue.isEmpty() && tick >= getCurrentDelay()) {
            delayIndex = Math.min(delayIndex + 1, REBUILD_DELAYS.length - 1);
            tick = 0;
            buildQueue();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (owner != null && owner.id() != null) {
            output.putString("ownerUUID", owner.id().toString());
            if (owner.name() != null) {
                output.putString("ownerName", owner.name());
            }
        }
        byte sides = 0;
        for (Direction face : Direction.values()) {
            if (openSides.contains(face)) {
                sides |= (byte) (1 << face.get3DDataValue());
            }
        }
        output.putByte("openSides", sides);

        tank.serialize(output);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String ownerUuid = input.getStringOr("ownerUUID", "");
        if (!ownerUuid.isEmpty()) {
            try {
                owner = new GameProfile(UUID.fromString(ownerUuid), input.getStringOr("ownerName", "Unknown"));
            } catch (IllegalArgumentException e) {
                owner = null;
            }
        }
        byte sides = input.getByteOr("openSides", (byte) 0b011111);
        openSides.clear();
        for (Direction face : Direction.values()) {
            if (((sides >> face.get3DDataValue()) & 1) == 1) {
                openSides.add(face);
            }
        }

        tank.deserialize(input);
        syncOpenSidesToBlockState();
    }

    @Override
    public void onLoad() {
        syncOpenSidesToBlockState();
    }

    private void syncOpenSidesToBlockState() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof BlockFloodGate)) {
            return;
        }
        BlockState newState = state;
        for (Map.Entry<Direction, Property<Boolean>> entry : BlockFloodGate.CONNECTED_MAP.entrySet()) {
            newState = newState.setValue(entry.getValue(), openSides.contains(entry.getKey()));
        }
        if (newState != state) {
            level.setBlock(worldPosition, newState, Block.UPDATE_CLIENTS);
        }
        onSidesToggled();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("fluid = " + FluidUtilBC.getDebugString(tank));
        left.add("owner = " + (owner != null ? owner.name() : "none"));
        left.add("openSides = " + openSides.stream().map(Enum::name).collect(Collectors.joining(", ")));
        left.add("delay = " + getCurrentDelay());
        left.add("tick = " + tick);
        left.add("queue size = " + queue.size());
        left.add("paths size = " + paths.size());
    }

    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
        BlockState state = getBlockState();
        List<String> open = new ArrayList<>();
        for (Map.Entry<Direction, Property<Boolean>> e : BlockFloodGate.CONNECTED_MAP.entrySet()) {
            if (state.getValue(e.getValue())) {
                open.add(e.getKey().name());
            }
        }
        left.add("openSides (state) = " + String.join(", ", open));
    }
}
