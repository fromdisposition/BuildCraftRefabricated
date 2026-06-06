package buildcraft.core;

import buildcraft.energy.BCEnergyItems;
import buildcraft.fabric.BCBuildersCreativeEntries;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.fabric.BCRegistries;
import buildcraft.fabric.BCRoboticsCreativeEntries;
import buildcraft.fabric.BCSiliconCreativeEntries;
import buildcraft.factory.BCFactoryItems;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.item.ItemGuideNote;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Row;

public final class BCCoreCreativeTabs {
   public static CreativeModeTab MAIN_TAB;

   private BCCoreCreativeTabs() {
   }

   public static void register() {
      MAIN_TAB = BCRegistries.registerCreativeTab(
         "buildcraftcore",
         "main",
         CreativeModeTab.builder(Row.TOP, 1)
            .title(Component.translatable("itemGroup.buildcraft.main"))
            .icon(() -> new ItemStack(BCCoreItems.GEAR_WOOD))
            .displayItems((parameters, output) -> {
               output.accept(BCCoreItems.WRENCH);
               output.accept(BCCoreItems.MARKER_CONNECTOR);
               output.accept(BCCoreItems.MARKER_VOLUME);
               output.accept(BCCoreItems.MARKER_PATH);
               output.accept(BCCoreItems.VOLUME_BOX);
               output.accept(BCCoreItems.LIST);
               output.accept(BCCoreItems.PAINTBRUSH);
               output.accept(BCCoreItems.FRAGILE_FLUID_CONTAINER);
               output.accept(BCCoreItems.ENGINE_REDSTONE);
               output.accept(BCCoreItems.ENGINE_CREATIVE);
               if (BCEnergyItems.ENGINE_STONE != null) {
                  output.accept(BCEnergyItems.ENGINE_STONE);
               }

               if (BCEnergyItems.ENGINE_IRON != null) {
                  output.accept(BCEnergyItems.ENGINE_IRON);
               }

               if (BCEnergyItems.ENGINE_FE != null) {
                  output.accept(BCEnergyItems.ENGINE_FE);
               }

               if (BCEnergyItems.DYNAMO_MJ != null) {
                  output.accept(BCEnergyItems.DYNAMO_MJ);
               }

               for (BCEnergyFluidsFabric.FluidEntry entry : BCEnergyFluidsFabric.ALL) {
                  output.accept(new ItemStack(entry.still().getBucket()));
               }

               if (BCEnergyItems.GLOB_OF_OIL != null) {
                  output.accept(BCEnergyItems.GLOB_OF_OIL);
               }

               if (BCFactoryItems.AUTOWORKBENCH_ITEM != null) {
                  output.accept(BCFactoryItems.AUTOWORKBENCH_ITEM);
                  output.accept(BCFactoryItems.MINING_WELL);
                  output.accept(BCFactoryItems.PUMP);
                  output.accept(BCFactoryItems.FLOOD_GATE);
                  output.accept(BCFactoryItems.TANK);
                  output.accept(BCFactoryItems.CHUTE);
                  output.accept(BCFactoryItems.DISTILLER);
                  output.accept(BCFactoryItems.HEAT_EXCHANGE);
                  output.accept(BCFactoryItems.WATER_GEL_SPAWN);
                  output.accept(BCFactoryItems.GELLED_WATER);
               }

               BCBuildersCreativeEntries.addMainTabItems(output);
               BCSiliconCreativeEntries.addMainTabItems(output);
               BCRoboticsCreativeEntries.addMainTabItems(output);
               output.accept(BCCoreItems.SPRING_WATER);
               output.accept(BCCoreItems.SPRING_OIL);
               output.accept(BCCoreItems.DECORATED_LASER);
               output.accept(BCLibItems.GUIDE);
               output.accept(BCLibItems.GUIDE_CONFIG);
               output.accept(ItemGuideNote.withPage(Identifier.parse("buildcraft:block/quarry")));
               if (BCCore.DEV) {
                  if (BCCoreItems.POWER_TESTER != null) {
                     output.accept(BCCoreItems.POWER_TESTER);
                  }

                  if (BCCoreItems.GOGGLES != null) {
                     output.accept(BCCoreItems.GOGGLES);
                  }

                  if (BCCoreItems.MAP_LOCATION != null) {
                     output.accept(BCCoreItems.MAP_LOCATION);
                  }

                  if (BCLibItems.DEBUGGER != null) {
                     output.accept(BCLibItems.DEBUGGER);
                  }

                  if (BCCoreItems.DECORATED_DESTROY != null) {
                     output.accept(BCCoreItems.DECORATED_DESTROY);
                  }

                  if (BCCoreItems.DECORATED_BLUEPRINT != null) {
                     output.accept(BCCoreItems.DECORATED_BLUEPRINT);
                  }

                  if (BCCoreItems.DECORATED_TEMPLATE != null) {
                     output.accept(BCCoreItems.DECORATED_TEMPLATE);
                  }

                  if (BCCoreItems.DECORATED_PAPER != null) {
                     output.accept(BCCoreItems.DECORATED_PAPER);
                  }

                  if (BCCoreItems.DECORATED_LEATHER != null) {
                     output.accept(BCCoreItems.DECORATED_LEATHER);
                  }
               }

               output.accept(BCCoreItems.GEAR_WOOD);
               output.accept(BCCoreItems.GEAR_STONE);
               output.accept(BCCoreItems.GEAR_IRON);
               output.accept(BCCoreItems.GEAR_GOLD);
               output.accept(BCCoreItems.GEAR_DIAMOND);
               output.accept(BCCoreItems.DIAMOND_SHARD);
            })
            .build()
      );
   }
}
