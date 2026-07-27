/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.block;

import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.tile.TileLaser;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BlockLaser extends BaseEntityBlock {
   public static final MapCodec<BlockLaser> CODEC = simpleCodec(BlockLaser::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
   // Hitbox matching the model: the full 16x4 base plate on the mounting (-FACING) face plus a centred 6x6, 9-deep
   // emitter extending toward FACING -- so you can click and walk between closely-packed lasers, not a full cube.
   private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

   static {
      SHAPES.put(Direction.UP, Shapes.or(Block.box(0, 0, 0, 16, 4, 16), Block.box(5, 4, 5, 11, 13, 11)));
      SHAPES.put(Direction.DOWN, Shapes.or(Block.box(0, 12, 0, 16, 16, 16), Block.box(5, 3, 5, 11, 12, 11)));
      SHAPES.put(Direction.NORTH, Shapes.or(Block.box(0, 0, 12, 16, 16, 16), Block.box(5, 5, 3, 11, 11, 12)));
      SHAPES.put(Direction.SOUTH, Shapes.or(Block.box(0, 0, 0, 16, 16, 4), Block.box(5, 5, 4, 11, 11, 13)));
      SHAPES.put(Direction.EAST, Shapes.or(Block.box(0, 0, 0, 4, 16, 16), Block.box(4, 5, 5, 13, 11, 11)));
      SHAPES.put(Direction.WEST, Shapes.or(Block.box(12, 0, 0, 16, 16, 16), Block.box(3, 5, 5, 12, 11, 11)));
   }

   public BlockLaser(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.UP));
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileLaser(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCSiliconBlockEntities.LASER, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCSiliconBlockEntities.LASER, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPES.get(state.getValue(FACING));
   }
}
