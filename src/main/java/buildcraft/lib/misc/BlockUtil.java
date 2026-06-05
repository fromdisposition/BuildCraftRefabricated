/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.MjAPI;

import buildcraft.core.BCCoreConfig;

public class BlockUtil {

    public static double miningMultiplier = 1.0;

    private static final GameProfile MACHINE_FAKE_PROFILE = new GameProfile(
            UUID.nameUUIDFromBytes("BuildCraft".getBytes(StandardCharsets.UTF_8)),
            "[BuildCraft]"
    );

    public static boolean canMachineBreak(ServerLevel level, BlockPos pos, GameProfile owner) {
        if (BCCoreConfig.minePlayerProtected.get()) {
            return true;
        }
        GameProfile profile = (owner != null && owner.name() != null) ? owner : MACHINE_FAKE_PROFILE;
        Player fp = BuildCraftAPI.fakePlayerProvider.getFakePlayer(level, profile, pos);
        BlockState state = level.getBlockState(pos);
        return BreakEventCompat.canBreak(level, pos, state, fp);
    }

    @Nullable
    public static Fluid getFluidWithFlowing(Block block) {
        if (block instanceof LiquidBlock liquidBlock) {
            Fluid fluid = liquidBlock.defaultBlockState().getFluidState().getType();
            if (fluid != null && fluid != Fluids.EMPTY) {
                return fluid;
            }
        }
        return null;
    }

    @Nullable
    public static Fluid getFluidWithFlowing(Level world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            return fluidState.getType();
        }
        return getFluidWithFlowing(world.getBlockState(pos).getBlock());
    }

    @Nullable
    public static Fluid getFluid(Level world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos);
        if (!fluidState.isEmpty() && fluidState.isSource()) {
            return fluidState.getType();
        }
        return null;
    }

    @Nullable
    public static Fluid getFluidWithoutFlowing(BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() && fluidState.isSource()) {
            return fluidState.getType();
        }
        return null;
    }

    @Nullable
    public static FluidStack drainBlock(Level world, BlockPos pos, boolean doDrain) {
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.isEmpty() || !fluidState.isSource()) {
            return null;
        }
        Fluid fluid = fluidState.getType();
        if (doDrain) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        return new FluidStack(fluid, 1000);
    }

    public static Comparator<BlockPos> uniqueBlockPosComparator(Comparator<BlockPos> parent) {
        return (a, b) -> {
            int parentValue = parent.compare(a, b);
            if (parentValue != 0) {
                return parentValue;
            } else if (a.getX() != b.getX()) {
                return Integer.compare(a.getX(), b.getX());
            } else if (a.getY() != b.getY()) {
                return Integer.compare(a.getY(), b.getY());
            } else if (a.getZ() != b.getZ()) {
                return Integer.compare(a.getZ(), b.getZ());
            } else {
                return 0;
            }
        };
    }

    public static boolean isUnbreakableBlock(Level world, BlockPos pos, GameProfile owner) {
        BlockState state = world.getBlockState(pos);
        return state.getDestroySpeed(world, pos) < 0;
    }

    public static long computeBlockBreakPower(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock) {
            return MjAPI.MJ;
        }
        float hardness = state.getDestroySpeed(world, pos);
        return (long) Math.floor(16 * MjAPI.MJ * ((hardness + 1) * 2) * miningMultiplier);
    }

    public record BreakResult(List<ItemStack> drops, int xp, FluidStack capturedFluid) {}

    public static Optional<List<ItemStack>> breakBlockAndGetDrops(
            ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        return breakBlockAndGetDropsWithXp(world, pos, tool, owner).map(BreakResult::drops);
    }

    public static Optional<BreakResult> breakBlockAndGetDropsWithXp(
            ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return Optional.of(new BreakResult(List.of(), 0, FluidStack.EMPTY));
        }
        if (state.getDestroySpeed(world, pos) < 0) {
            return Optional.empty();
        }

        BlockEntity be = world.getBlockEntity(pos);

        boolean tierGated = state.requiresCorrectToolForDrops()
                && !tool.isCorrectToolForDrops(state);

        List<ItemStack> drops = tierGated
                ? new ArrayList<>()
                : new ArrayList<>(Block.getDrops(state, world, pos, be, (Entity) null, tool));

        int xp = 0;

        FluidStack capturedFluid = FluidStack.EMPTY;
        if (state.getBlock() instanceof LiquidBlock) {
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource()) {
                capturedFluid = new FluidStack(fluidState.getType(), 1000);
            }
        }

        if (state.getBlock() instanceof LiquidBlock) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else {
            world.destroyBlock(pos, false);
        }

        return Optional.of(new BreakResult(drops, xp, capturedFluid));
    }
}
