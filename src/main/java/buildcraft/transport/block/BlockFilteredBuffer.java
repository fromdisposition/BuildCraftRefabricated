/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.block;

import buildcraft.lib.block.BcTileBlock;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockFilteredBuffer extends BcTileBlock {
   public BlockFilteredBuffer(Properties properties) {
      super(properties, () -> BCTransportBlockEntities.FILTERED_BUFFER, null, null, true);
   }

   @Override
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileFilteredBuffer) {
         level.sendBlockUpdated(pos, state, state, 3);
      }
   }
}
