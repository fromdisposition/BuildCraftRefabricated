package buildcraft.builders;

import buildcraft.builders.block.BlockArchitectTable;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.block.BlockElectronicLibrary;
import buildcraft.builders.block.BlockFiller;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockQuarry;
import buildcraft.builders.block.BlockReplacer;
import buildcraft.fabric.BCRegistries;
import net.minecraft.world.level.block.SoundType;

public final class BCBuildersBlocks {
   public static BlockFrame FRAME;
   public static BlockFiller FILLER;
   public static BlockBuilder BUILDER;
   public static BlockArchitectTable ARCHITECT;
   public static BlockElectronicLibrary LIBRARY;
   public static BlockReplacer REPLACER;
   public static BlockQuarry QUARRY;

   private BCBuildersBlocks() {
   }

   public static void register() {
      FRAME = BCRegistries.registerBlock(
         "buildcraftbuilders", "frame", BlockFrame::new, p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      FILLER = BCRegistries.registerBlock(
         "buildcraftbuilders", "filler", BlockFiller::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      BUILDER = BCRegistries.registerBlock(
         "buildcraftbuilders", "builder", BlockBuilder::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      ARCHITECT = BCRegistries.registerBlock(
         "buildcraftbuilders", "architect", BlockArchitectTable::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      LIBRARY = BCRegistries.registerBlock(
         "buildcraftbuilders", "library", BlockElectronicLibrary::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      REPLACER = BCRegistries.registerBlock(
         "buildcraftbuilders", "replacer", BlockReplacer::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      QUARRY = BCRegistries.registerBlock(
         "buildcraftbuilders", "quarry", BlockQuarry::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.ANVIL).requiresCorrectToolForDrops()
      );
   }
}
