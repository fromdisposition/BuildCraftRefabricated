/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.factory.tile.TileMiner;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BlockTube extends Block {
   public static final MapCodec<BlockTube> CODEC = simpleCodec(BlockTube::new);

   public BlockTube(Properties props) {
      super(props);
   }

   protected MapCodec<? extends Block> codec() {
      return CODEC;
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   protected boolean propagatesSkylightDown(BlockState state) {
      return true;
   }

   protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
   }

   protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      return isOwnedByMinerColumn(level, pos) ? 0.0F : super.getDestroyProgress(state, player, level, pos);
   }

   private static boolean isOwnedByMinerColumn(BlockGetter level, BlockPos pos) {
      BlockPos checkPos = pos;

      while (level.getBlockState(checkPos = checkPos.above()).getBlock() instanceof BlockTube) {
      }

      return level.getBlockEntity(checkPos) instanceof TileMiner;
   }
}
