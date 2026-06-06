package buildcraft.builders;

import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.builders.container.ContainerBuilder;
import buildcraft.builders.container.ContainerElectronicLibrary;
import buildcraft.builders.container.ContainerFiller;
import buildcraft.builders.container.ContainerFillerPlanner;
import buildcraft.builders.container.ContainerReplacer;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.lib.fabric.menu.FillerPlannerMenuKey;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class BCBuildersMenuTypes {
   public static MenuType<ContainerFiller> FILLER;
   public static MenuType<ContainerBuilder> BUILDER;
   public static MenuType<ContainerArchitectTable> ARCHITECT;
   public static MenuType<ContainerElectronicLibrary> LIBRARY;
   public static MenuType<ContainerReplacer> REPLACER;
   public static MenuType<ContainerFillerPlanner> FILLER_PLANNER;

   private BCBuildersMenuTypes() {
   }

   public static void register() {
      FILLER = BCRegistries.registerMenuType("buildcraftbuilders", "filler", ExtendedMenuTypes.create(ContainerFiller::new));
      BUILDER = BCRegistries.registerMenuType("buildcraftbuilders", "builder", ExtendedMenuTypes.create(ContainerBuilder::new));
      ARCHITECT = BCRegistries.registerMenuType("buildcraftbuilders", "architect", ExtendedMenuTypes.create(ContainerArchitectTable::new));
      LIBRARY = BCRegistries.registerMenuType("buildcraftbuilders", "library", ExtendedMenuTypes.create(ContainerElectronicLibrary::new));
      REPLACER = BCRegistries.registerMenuType("buildcraftbuilders", "replacer", ExtendedMenuTypes.create(ContainerReplacer::new));
      FILLER_PLANNER = BCRegistries.registerMenuType(
         "buildcraftbuilders",
         "filler_planner",
         new ExtendedMenuType<>((int syncId, Inventory inv, FillerPlannerMenuKey key) -> new ContainerFillerPlanner(syncId, inv, key), FillerPlannerMenuKey.STREAM_CODEC)
      );
   }
}
