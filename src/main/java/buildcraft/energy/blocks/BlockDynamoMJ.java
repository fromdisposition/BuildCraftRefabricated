/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.blocks;

import buildcraft.lib.compat.BcInteract;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.BlockDropsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
//? if >= 1.21.10 {
import net.minecraft.world.level.redstone.Orientation;
//?}
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockDynamoMJ extends Block implements EntityBlock, ICustomRotationHandler {
   public BlockDynamoMJ(Properties properties) {
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
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileDynamoMJ(pos, state);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileDynamoMJ dynamo) {
         dynamo.onPlacedBy(placer, stack);
      }
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return !level.isClientSide() ? (lvl, pos, st, be) -> {
         if (be instanceof TileDynamoMJ dynamo) {
            TileDynamoMJ.serverTick(lvl, pos, st, dynamo);
         }
      } : (lvl, pos, st, be) -> {
         if (be instanceof TileDynamoMJ dynamo) {
            dynamo.clientTick();
         }
      };
   }

   @Override
   public InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
      if (world.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else if (world.getBlockEntity(pos) instanceof TileDynamoMJ dynamo && dynamo.attemptRotation()) {
         world.setBlock(pos, (BlockState)state.setValue(BuildCraftProperties.BLOCK_FACING_6, dynamo.getOrientation()), 3);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.FAIL;
      }
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      return BcInteract.toItem(bcUseItemOn(stack, state, level, pos, player, hand, hitResult));
   }

   protected InteractionResult bcUseItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (stack.getItem() instanceof IItemPipe pipe) {
         InteractionResult placed = EnginePipeInteraction.tryPlacePipe(pipe, stack, level, player, hand, hitResult, PipeApi.flowPower, PipeApi.flowRf);
         return placed != null ? placed : this.openGui(state, level, pos, player);
      } else {
         if (player.isShiftKeyDown()) {
            return this.openGui(state, level, pos, player);
         }

         if (stack.getItem() instanceof IToolWrench) {
            if (level.getBlockEntity(pos) instanceof TileEngineBase_BC8 engine && engine.hasAlternateReceiver()) {
               return InteractionResult.PASS;
            } else {
               if (!level.isClientSide()) {
                  level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, 1.3F);
               }

               player.swing(hand);
               return InteractionResult.CONSUME;
            }
         } else {
            return this.openGui(state, level, pos, player);
         }
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return this.openGui(state, level, pos, player);
   }

   private InteractionResult openGui(BlockState state, Level level, BlockPos pos, Player player) {
      return EngineBlockGui.open(level, pos, player, TileDynamoMJ.class);
   }

   //? if >= 1.21.10 {
   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean isMoving) {
   //?} else {
   /*protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
   *///?}
      if (level.getBlockEntity(pos) instanceof TileDynamoMJ dynamo) {
         dynamo.onNeighborUpdate();
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileDynamoMJ dynamo) {
         BlockDropsUtil.dropItems(level, pos, dynamo.upgrades);
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
