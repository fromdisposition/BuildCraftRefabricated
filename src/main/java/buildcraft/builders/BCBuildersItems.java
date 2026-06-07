package buildcraft.builders;

import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.item.ItemFillerPlanner;
import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.fabric.BCRegistries;
import net.minecraft.world.item.BlockItem;

public final class BCBuildersItems {
   public static BlockItem FRAME;
   public static BlockItem FILLER;
   public static BlockItem BUILDER;
   public static BlockItem ARCHITECT;
   public static BlockItem LIBRARY;
   public static BlockItem REPLACER;
   public static BlockItem QUARRY;
   public static BlockItem CONSTRUCTION_MARKER;
   public static ItemSnapshot BLUEPRINT_CLEAN;
   public static ItemSnapshot BLUEPRINT_USED;
   public static ItemSnapshot TEMPLATE_CLEAN;
   public static ItemSnapshot TEMPLATE_USED;
   public static ItemSchematicSingle SCHEMATIC_SINGLE_CLEAN;
   public static ItemSchematicSingle SCHEMATIC_SINGLE_USED;
   public static ItemFillerPlanner FILLER_PLANNER;

   private BCBuildersItems() {
   }

   public static void register() {
      FRAME = BCRegistries.registerBlockItem("buildcraftbuilders", "frame", BCBuildersBlocks.FRAME);
      FILLER = BCRegistries.registerBlockItem("buildcraftbuilders", "filler", BCBuildersBlocks.FILLER);
      BUILDER = BCRegistries.registerBlockItem("buildcraftbuilders", "builder", BCBuildersBlocks.BUILDER);
      ARCHITECT = BCRegistries.registerBlockItem("buildcraftbuilders", "architect", BCBuildersBlocks.ARCHITECT);
      LIBRARY = BCRegistries.registerBlockItem("buildcraftbuilders", "library", BCBuildersBlocks.LIBRARY);
      REPLACER = BCRegistries.registerBlockItem("buildcraftbuilders", "replacer", BCBuildersBlocks.REPLACER);
      QUARRY = BCRegistries.registerBlockItem("buildcraftbuilders", "quarry", BCBuildersBlocks.QUARRY);
      CONSTRUCTION_MARKER = BCRegistries.registerBlockItem("buildcraftbuilders", "construction_marker", BCBuildersBlocks.CONSTRUCTION_MARKER);
      BLUEPRINT_CLEAN = BCRegistries.registerItem(
         "buildcraftbuilders", "blueprint_clean", props -> new ItemSnapshot(props, EnumSnapshotType.BLUEPRINT, false), p -> p.stacksTo(16)
      );
      BLUEPRINT_USED = BCRegistries.registerItem(
         "buildcraftbuilders", "blueprint_used", props -> new ItemSnapshot(props, EnumSnapshotType.BLUEPRINT, true), p -> p.stacksTo(1)
      );
      TEMPLATE_CLEAN = BCRegistries.registerItem(
         "buildcraftbuilders", "template_clean", props -> new ItemSnapshot(props, EnumSnapshotType.TEMPLATE, false), p -> p.stacksTo(16)
      );
      TEMPLATE_USED = BCRegistries.registerItem(
         "buildcraftbuilders", "template_used", props -> new ItemSnapshot(props, EnumSnapshotType.TEMPLATE, true), p -> p.stacksTo(1)
      );
      SCHEMATIC_SINGLE_CLEAN = BCRegistries.registerItem(
         "buildcraftbuilders", "schematic_single_clean", props -> new ItemSchematicSingle(props, false), p -> p.stacksTo(16)
      );
      SCHEMATIC_SINGLE_USED = BCRegistries.registerItem(
         "buildcraftbuilders", "schematic_single_used", props -> new ItemSchematicSingle(props, true), p -> p.stacksTo(1)
      );
      FILLER_PLANNER = BCRegistries.registerItem("buildcraftbuilders", "filler_planner", ItemFillerPlanner::new, p -> p.stacksTo(1));
   }
}
