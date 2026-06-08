/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.block;

import buildcraft.api.enums.EnumSpring;
import buildcraft.fabric.BCEnergyFluidsFabric;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class BlockSpring extends Block implements EntityBlock {
   @Nullable
   public static BlockSpring.OilTileFactory oilTileFactory;
   private final EnumSpring springType;

   public BlockSpring(EnumSpring springType, Properties properties) {
      super(properties.strength(-1.0F, 3600000.0F).sound(SoundType.STONE).randomTicks());
      this.springType = springType;
   }

   public EnumSpring getSpringType() {
      return this.springType;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      this.generateSpringBlock(level, pos, random);
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      super.onPlace(state, level, pos, oldState, isMoving);
      level.scheduleTick(pos, this, this.springType.tickRate);
      if (!level.isClientSide() && level instanceof ServerLevel server) {
         this.tryPlaceSpringLiquid(server, pos, server.getRandom(), true);
         server.scheduleTick(pos, this, 1);
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      this.generateSpringBlock(level, pos, random);
   }

   private void generateSpringBlock(ServerLevel level, BlockPos pos, RandomSource random) {
      level.scheduleTick(pos, this, this.springType.tickRate);
      this.tryPlaceSpringLiquid(level, pos, random, false);
   }

   private void tryPlaceSpringLiquid(ServerLevel level, BlockPos pos, RandomSource random, boolean ignoreChance) {
      BlockState liquidBlock = this.resolveLiquidBlock(level);
      if (this.springType.canGen && liquidBlock != null) {
         BlockPos upPos = pos.above();
         BlockState upState = level.getBlockState(upPos);
         boolean canPlace = upState.isAir() || !upState.equals(liquidBlock) && hasFluid(level, upPos);
         if (canPlace) {
            if (ignoreChance || this.springType.chance == -1 || random.nextInt(this.springType.chance) == 0) {
               level.setBlock(upPos, liquidBlock, 3);
            }
         }
      }
   }

   private BlockState resolveLiquidBlock(ServerLevel level) {
      if (this.springType == EnumSpring.OIL) {
         BlockState netherAware = BCEnergyFluidsFabric.oilSourceBlockStateForLevel(level);
         if (netherAware != null) {
            return netherAware;
         }
      }

      return this.springType.liquidBlock;
   }

   private static boolean hasFluid(Level level, BlockPos pos) {
      FluidState fluidState = level.getFluidState(pos);
      if (!fluidState.isEmpty()) {
         return true;
      }

      Block block = level.getBlockState(pos).getBlock();
      if (!(block instanceof LiquidBlock)) {
         return false;
      }

      FluidState defaultFluid = block.defaultBlockState().getFluidState();
      return !defaultFluid.isEmpty() && defaultFluid.getType() != Fluids.EMPTY;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.springType == EnumSpring.OIL && oilTileFactory != null ? oilTileFactory.create(pos, state) : null;
   }

   @FunctionalInterface
   public interface OilTileFactory {
      BlockEntity create(BlockPos var1, BlockState var2);
   }
}
