/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.engine;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class BlockEngineBase_BC8 extends Block implements EntityBlock, ICustomRotationHandler {
   public BlockEngineBase_BC8(Properties properties) {
      super(properties.noOcclusion());
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(BuildCraftProperties.BLOCK_FACING_6, Direction.UP));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BuildCraftProperties.BLOCK_FACING_6});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      return (BlockState)this.defaultBlockState().setValue(BuildCraftProperties.BLOCK_FACING_6, ctx.getClickedFace());
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      return (BlockState)state.setValue(BuildCraftProperties.BLOCK_FACING_6, rot.rotate((Direction)state.getValue(BuildCraftProperties.BLOCK_FACING_6)));
   }

   public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
      Direction facing = (Direction)state.getValue(BuildCraftProperties.BLOCK_FACING_6);

      return switch (facing) {
         case DOWN -> Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0);
         case UP -> Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
         case NORTH -> Block.box(0.0, 0.0, 12.0, 16.0, 16.0, 16.0);
         case SOUTH -> Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0);
         case WEST -> Block.box(12.0, 0.0, 0.0, 16.0, 16.0, 16.0);
         case EAST -> Block.box(0.0, 0.0, 0.0, 4.0, 16.0, 16.0);
         default -> throw new MatchException(null, null);
      };
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public abstract BlockEntity newBlockEntity(BlockPos var1, BlockState var2);

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileEngineBase_BC8 engine) {
         engine.onPlacedBy(placer, stack);
      }
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return !level.isClientSide() ? (lvl, pos, st, be) -> {
         if (be instanceof TileEngineBase_BC8 engine) {
            TileEngineBase_BC8.serverTick(lvl, pos, st, engine);
         }
      } : (lvl, pos, st, be) -> {
         if (be instanceof TileEngineBase_BC8 engine) {
            engine.clientTick();
         }
      };
   }

   @Override
   public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
      if (world.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else if (world.getBlockEntity(pos) instanceof TileEngineBase_BC8 engine && engine.attemptRotation()) {
         world.setBlock(pos, (BlockState)state.setValue(BuildCraftProperties.BLOCK_FACING_6, engine.getOrientation()), 3);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.FAIL;
      }
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return InteractionResult.PASS;
   }

   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean isMoving) {
      if (level.getBlockEntity(pos) instanceof TileEngineBase_BC8 engine) {
         engine.onNeighborUpdate();
      }
   }
}
