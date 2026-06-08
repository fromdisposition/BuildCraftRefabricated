/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.tile.TileConstructionMarker;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockConstructionMarker extends HorizontalDirectionalBlock implements EntityBlock {
   public static final MapCodec<BlockConstructionMarker> CODEC = simpleCodec(BlockConstructionMarker::new);

   public BlockConstructionMarker(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileConstructionMarker(pos, state);
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (!(stack.getItem() instanceof ItemSnapshot snapshot) || !snapshot.isUsed()) {
         return InteractionResult.PASS;
      }

      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileConstructionMarker marker && !marker.hasBlueprint()) {
         marker.setBlueprint(stack.copyWithCount(1));
         if (!player.getAbilities().instabuild) {
            stack.shrink(1);
         }
      }

      return InteractionResult.SUCCESS;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileConstructionMarker marker && marker.hasBlueprint()) {
         ItemStack removed = marker.removeBlueprint();
         if (!removed.isEmpty() && !player.addItem(removed)) {
            Block.popResource(level, pos, removed);
         }
      }

      return InteractionResult.SUCCESS;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TileConstructionMarker marker) {
         marker.onPlacedBy(placer, stack);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileConstructionMarker marker) {
         ItemStack blueprint = marker.getBlueprintStack();
         if (!blueprint.isEmpty()) {
            Block.popResource(level, pos, blueprint);
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
