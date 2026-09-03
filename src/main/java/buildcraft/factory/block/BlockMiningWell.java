/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.lib.block.BcTileBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockMiningWell extends BcTileBlock {
   public static final Property<Direction> FACING = BuildCraftProperties.BLOCK_FACING;
   public BlockMiningWell(Properties properties) {
      super(properties, () -> BCFactoryBlockEntities.MINING_WELL, TileMiningWell::serverTick, TileMiningWell::clientTick, false);
      this.registerDefaultState((this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

}
