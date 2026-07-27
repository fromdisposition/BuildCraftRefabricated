/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileConstructionMarker;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.builders.tile.TileReplacer;
import buildcraft.fabric.BCRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCBuildersBlockEntities {
   public static BlockEntityType<TileFiller> FILLER;
   public static BlockEntityType<TileBuilder> BUILDER;
   public static BlockEntityType<TileArchitectTable> ARCHITECT;
   public static BlockEntityType<TileElectronicLibrary> LIBRARY;
   public static BlockEntityType<TileReplacer> REPLACER;
   public static BlockEntityType<TileQuarry> QUARRY;
   public static BlockEntityType<TileConstructionMarker> CONSTRUCTION_MARKER;

   private BCBuildersBlockEntities() {
   }

   public static void register() {
      FILLER = BCRegistries.registerBlockEntity("buildcraftbuilders", "filler", TileFiller::new, BCBuildersBlocks.FILLER);
      BUILDER = BCRegistries.registerBlockEntity("buildcraftbuilders", "builder", TileBuilder::new, BCBuildersBlocks.BUILDER);
      ARCHITECT = BCRegistries.registerBlockEntity("buildcraftbuilders", "architect", TileArchitectTable::new, BCBuildersBlocks.ARCHITECT);
      LIBRARY = BCRegistries.registerBlockEntity("buildcraftbuilders", "library", TileElectronicLibrary::new, BCBuildersBlocks.LIBRARY);
      REPLACER = BCRegistries.registerBlockEntity("buildcraftbuilders", "replacer", TileReplacer::new, BCBuildersBlocks.REPLACER);
      QUARRY = BCRegistries.registerBlockEntity("buildcraftbuilders", "quarry", TileQuarry::new, BCBuildersBlocks.QUARRY);
      CONSTRUCTION_MARKER = BCRegistries.registerBlockEntity(
         "buildcraftbuilders", "construction_marker", TileConstructionMarker::new, BCBuildersBlocks.CONSTRUCTION_MARKER
      );
   }
}
