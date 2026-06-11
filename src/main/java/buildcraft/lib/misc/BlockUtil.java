/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.fluids.FluidStack;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class BlockUtil {
   public static double miningMultiplier = 1.0;

   
   public static boolean blocksMotion(BlockState state) {
      Block block = state.getBlock();
      return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && isSolid(state);
   }

   public static boolean isSolid(BlockState state) {
      return state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP);
   }

   public static boolean isSolid(BlockGetter level, BlockPos pos, BlockState state) {
      return state.isFaceSturdy(level, pos, Direction.UP);
   }

   public static boolean isLiquid(BlockState state) {
      return !state.getFluidState().isEmpty();
   }

   public static boolean canMachineBreak(ServerLevel level, BlockPos pos, GameProfile owner) {
      if (BCCoreConfig.minePlayerProtected.get()) {
         return true;
      }

      Player fp = BuildCraftAPI.fakePlayerProvider.getFakePlayer(level, owner, pos);
      BlockState state = level.getBlockState(pos);
      return BreakEventCompat.canBreak(level, pos, state, fp);
   }

   public static boolean canMachinePlace(ServerLevel level, BlockPos pos, GameProfile owner, BlockPos originPos) {
      if (BCCoreConfig.minePlayerProtected.get()) {
         return true;
      }

      BlockPos fakePos = originPos != null ? originPos : pos;
      Player fp = BuildCraftAPI.fakePlayerProvider.getFakePlayer(level, owner, fakePos);
      return PlaceEventCompat.canPlace(level, pos, fp);
   }

   public static boolean machineSetBlock(ServerLevel level, BlockPos pos, BlockState state, int flags, GameProfile owner, BlockPos originPos) {
      return !canMachinePlace(level, pos, owner, originPos) ? false : level.setBlock(pos, state, flags);
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
      return !fluidState.isEmpty() ? fluidState.getType() : getFluidWithFlowing(world.getBlockState(pos).getBlock());
   }

   @Nullable
   public static Fluid getFluid(Level world, BlockPos pos) {
      FluidState fluidState = world.getFluidState(pos);
      return !fluidState.isEmpty() && fluidState.isSource() ? fluidState.getType() : null;
   }

   @Nullable
   public static Fluid getFluidWithoutFlowing(BlockState state) {
      FluidState fluidState = state.getFluidState();
      return !fluidState.isEmpty() && fluidState.isSource() ? fluidState.getType() : null;
   }

   @Nullable
   public static FluidStack drainBlock(Level world, BlockPos pos, boolean doDrain) {
      return drainBlock(world, pos, doDrain, null);
   }

   @Nullable
   public static FluidStack drainBlock(Level world, BlockPos pos, boolean doDrain, @Nullable GameProfile owner) {
      FluidState fluidState = world.getFluidState(pos);
      if (!fluidState.isEmpty() && fluidState.isSource()) {
         Fluid fluid = fluidState.getType();
         if (doDrain) {
            if (world instanceof ServerLevel serverLevel && !canMachineBreak(serverLevel, pos, owner)) {
               return null;
            }

            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
         }

         return new FluidStack(fluid, 1000);
      } else {
         return null;
      }
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
         } else {
            return a.getZ() != b.getZ() ? Integer.compare(a.getZ(), b.getZ()) : 0;
         }
      };
   }

   public static boolean isUnbreakableBlock(Level world, BlockPos pos, GameProfile owner) {
      BlockState state = world.getBlockState(pos);
      return state.getDestroySpeed(world, pos) < 0.0F;
   }

   public static long computeBlockBreakPower(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() instanceof LiquidBlock) {
         return MjAPI.MJ;
      }

      float hardness = state.getDestroySpeed(world, pos);
      return (long)Math.floor((float)(16L * MjAPI.MJ) * ((hardness + 1.0F) * 2.0F) * miningMultiplier);
   }

   public static Optional<List<ItemStack>> breakBlockAndGetDrops(ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
      return breakBlockAndGetDropsWithXp(world, pos, tool, owner).map(BlockUtil.BreakResult::drops);
   }

   public static Optional<BlockUtil.BreakResult> breakBlockAndGetDropsWithXp(ServerLevel world, BlockPos pos, @Nonnull ItemStack tool, GameProfile owner) {
      BlockState state = world.getBlockState(pos);
      if (state.isAir()) {
         return Optional.of(new BlockUtil.BreakResult(List.of(), 0, FluidStack.EMPTY));
      }

      if (state.getDestroySpeed(world, pos) < 0.0F) {
         return Optional.empty();
      }

      if (!canMachineBreak(world, pos, owner)) {
         return Optional.empty();
      }

      BlockEntity be = world.getBlockEntity(pos);
      boolean tierGated = state.requiresCorrectToolForDrops() && !tool.isCorrectToolForDrops(state);
      List<ItemStack> drops = tierGated ? new ArrayList<>() : new ArrayList<>(Block.getDrops(state, world, pos, be, (Entity)null, tool));
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

      return Optional.of(new BlockUtil.BreakResult(drops, xp, capturedFluid));
   }

   public record BreakResult(List<ItemStack> drops, int xp, FluidStack capturedFluid) {
   }
}
