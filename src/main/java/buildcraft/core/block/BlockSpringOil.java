/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.block;

import buildcraft.api.enums.EnumSpring;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// EntityBlock stays on the oil spring only: declaring it on a tile-less block makes vanilla log
// "Tried to load a block entity ... but failed" at every such position on chunk load.
public class BlockSpringOil extends BlockSpring implements EntityBlock {
   // Injected by the energy module: core must not depend on energy.
   @Nullable
   public static OilTileFactory oilTileFactory;

   public BlockSpringOil(Properties properties) {
      super(EnumSpring.OIL, properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return oilTileFactory != null ? oilTileFactory.create(pos, state) : null;
   }

   @FunctionalInterface
   public interface OilTileFactory {
      BlockEntity create(BlockPos pos, BlockState state);
   }
}
