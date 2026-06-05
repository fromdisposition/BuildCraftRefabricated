package buildcraft.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLibItems;

public final class BCCoreCreativeTabs {
    public static CreativeModeTab MAIN_TAB;

    private BCCoreCreativeTabs() {}

    public static void register() {
        MAIN_TAB = net.minecraft.core.Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                BCRegistries.id(BCCore.MODID, "main"),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
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
                            if (buildcraft.energy.BCEnergyItems.ENGINE_STONE != null) {
                                output.accept(buildcraft.energy.BCEnergyItems.ENGINE_STONE);
                            }
                            if (buildcraft.energy.BCEnergyItems.ENGINE_IRON != null) {
                                output.accept(buildcraft.energy.BCEnergyItems.ENGINE_IRON);
                            }
                            if (buildcraft.energy.BCEnergyItems.ENGINE_FE != null) {
                                output.accept(buildcraft.energy.BCEnergyItems.ENGINE_FE);
                            }
                            if (buildcraft.energy.BCEnergyItems.DYNAMO_MJ != null) {
                                output.accept(buildcraft.energy.BCEnergyItems.DYNAMO_MJ);
                            }
                            for (buildcraft.fabric.BCEnergyFluidsFabric.FluidEntry entry :
                                    buildcraft.fabric.BCEnergyFluidsFabric.ALL) {
                                output.accept(new ItemStack(entry.still().getBucket()));
                            }
                            if (buildcraft.energy.BCEnergyItems.GLOB_OF_OIL != null) {
                                output.accept(buildcraft.energy.BCEnergyItems.GLOB_OF_OIL);
                            }
                            if (buildcraft.factory.BCFactoryItems.AUTOWORKBENCH_ITEM != null) {
                                output.accept(buildcraft.factory.BCFactoryItems.AUTOWORKBENCH_ITEM);
                                output.accept(buildcraft.factory.BCFactoryItems.MINING_WELL);
                                output.accept(buildcraft.factory.BCFactoryItems.PUMP);
                                output.accept(buildcraft.factory.BCFactoryItems.FLOOD_GATE);
                                output.accept(buildcraft.factory.BCFactoryItems.TANK);
                                output.accept(buildcraft.factory.BCFactoryItems.CHUTE);
                                output.accept(buildcraft.factory.BCFactoryItems.DISTILLER);
                                output.accept(buildcraft.factory.BCFactoryItems.HEAT_EXCHANGE);
                                output.accept(buildcraft.factory.BCFactoryItems.WATER_GEL_SPAWN);
                                output.accept(buildcraft.factory.BCFactoryItems.GELLED_WATER);
                            }
                            buildcraft.fabric.BCBuildersCreativeEntries.addMainTabItems(output);
                            buildcraft.fabric.BCSiliconCreativeEntries.addMainTabItems(output);
                            buildcraft.fabric.BCRoboticsCreativeEntries.addMainTabItems(output);
                            output.accept(BCCoreItems.SPRING_WATER);
                            output.accept(BCCoreItems.SPRING_OIL);
                            output.accept(BCCoreItems.DECORATED_LASER);
                            output.accept(BCLibItems.GUIDE);
                            output.accept(BCLibItems.GUIDE_CONFIG);
                            output.accept(BCLibItems.GUIDE_NOTE);
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
                        .build());
    }
}
