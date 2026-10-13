/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import buildcraft.lib.block.BcHorizontalTileBlock;
import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.tile.TileBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockBuilder extends BcHorizontalTileBlock {
   public static final EnumProperty<EnumOptionalSnapshotType> SNAPSHOT_TYPE = EnumProperty.create("snapshot_type", EnumOptionalSnapshotType.class);

   public BlockBuilder(Properties properties) {
      super(properties);
      this.registerDefaultState(this.defaultBlockState().setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(SNAPSHOT_TYPE);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return super.getStateForPlacement(context).setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileBuilder(pos, state);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return blockEntityType != BCBuildersBlockEntities.BUILDER ? null : (lvl, pos, st, be) -> {
         if (be instanceof TileBuilder builder) {
            builder.tick();
         }
      };
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return this.openMenu(level, pos, player);
   }

}
