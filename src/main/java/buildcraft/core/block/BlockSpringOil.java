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

/**
 * Oil spring — the only spring variant that carries a block entity ({@link buildcraft.energy.tile.TileSpringOil},
 * which tracks per-player pump progress and the black_gold advancement). The water spring is a plain
 * {@link BlockSpring}: declaring EntityBlock on a block with no tile makes vanilla treat every spring_water
 * position as block-entity-bearing and log "Tried to load a block entity ... but failed" on chunk load.
 *
 * The tile factory is injected from the energy module (which owns TileSpringOil) at registration time, since
 * core must not depend on energy.
 */
public class BlockSpringOil extends BlockSpring implements EntityBlock {
   @Nullable
   public static OilTileFactory oilTileFactory;

   public BlockSpringOil(Properties properties) {
      super(EnumSpring.OIL, properties);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return oilTileFactory != null ? oilTileFactory.create(pos, state) : null;
   }

   @FunctionalInterface
   public interface OilTileFactory {
      BlockEntity create(BlockPos pos, BlockState state);
   }
}
