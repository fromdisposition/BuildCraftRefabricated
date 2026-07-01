/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.tile.TileMarker;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
//? if >= 1.21.10 {
import net.minecraft.world.level.ScheduledTickAccess;
//?}
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class BlockMarkerBase extends Block implements EntityBlock {
   private static final Map<Direction, VoxelShape> BOUNDING_BOXES = new EnumMap<>(Direction.class);

   public BlockMarkerBase(Properties properties) {
      super(properties);
      BlockState defaultState = this.defaultBlockState();
      defaultState = (BlockState)defaultState.setValue(BuildCraftProperties.BLOCK_FACING_6, Direction.UP);
      defaultState = (BlockState)defaultState.setValue(BuildCraftProperties.ACTIVE, false);
      this.registerDefaultState(defaultState);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BuildCraftProperties.BLOCK_FACING_6, BuildCraftProperties.ACTIVE});
   }

   public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext ctx) {
      Direction direction = (Direction)state.getValue(BuildCraftProperties.BLOCK_FACING_6);
      return BOUNDING_BOXES.getOrDefault(direction, Shapes.block());
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return Shapes.empty();
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      Direction facing = ctx.getClickedFace();
      Level world = ctx.getLevel();
      BlockPos pos = ctx.getClickedPos();
      BlockState state = this.defaultBlockState();
      return (BlockState)this.defaultBlockState().setValue(BuildCraftProperties.BLOCK_FACING_6, facing);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      Direction sideOn = (Direction)state.getValue(BuildCraftProperties.BLOCK_FACING_6);
      BlockPos neighborPos = pos.relative(sideOn.getOpposite());
      return level.getBlockState(neighborPos).isFaceSturdy(level, neighborPos, sideOn);
   }

   //? if >= 1.21.10 {
   protected BlockState updateShape(
      BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos,
      Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
   ) {
   //?} else {
   /*protected BlockState updateShape(
      BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level,
      BlockPos pos, BlockPos neighborPos
   ) {
   *///?}
      if (!state.canSurvive(level, pos)) {
         //? if >= 1.21.10 {
         scheduledTickAccess.scheduleTick(pos, this, 1);
         //?} else {
         /*level.scheduleTick(pos, this, 1);
         *///?}
      }

      //? if >= 1.21.10 {
      return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
      //?} else {
      /*return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
      *///?}
   }

   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, false);
         Block.popResource(level, pos, new ItemStack(this.asItem()));
      }
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      return (BlockState)state.setValue(BuildCraftProperties.BLOCK_FACING_6, rot.rotate((Direction)state.getValue(BuildCraftProperties.BLOCK_FACING_6)));
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.createTileEntity(pos, state);
   }

   public abstract BlockEntity createTileEntity(BlockPos var1, BlockState var2);

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileMarker<?> marker) {
         marker.buildcraft$onAttachedToLevel(level);
         marker.onPlacedBy(placer, stack);
      }
   }

   protected List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      return Collections.singletonList(new ItemStack(this.asItem()));
   }

   static {
      double halfWidth = 0.1;
      double h = 0.65;
      double nw = 0.5 - halfWidth;
      double pw = 0.5 + halfWidth;
      double ih = 1.0 - h;
      BOUNDING_BOXES.put(Direction.DOWN, Shapes.create(new AABB(nw, ih, nw, pw, 1.0, pw)));
      BOUNDING_BOXES.put(Direction.UP, Shapes.create(new AABB(nw, 0.0, nw, pw, h, pw)));
      BOUNDING_BOXES.put(Direction.SOUTH, Shapes.create(new AABB(nw, nw, 0.0, pw, pw, h)));
      BOUNDING_BOXES.put(Direction.NORTH, Shapes.create(new AABB(nw, nw, ih, pw, pw, 1.0)));
      BOUNDING_BOXES.put(Direction.EAST, Shapes.create(new AABB(0.0, nw, nw, h, pw, pw)));
      BOUNDING_BOXES.put(Direction.WEST, Shapes.create(new AABB(ih, nw, nw, 1.0, pw, pw)));
   }
}
