package buildcraft.builders;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import buildcraft.fabric.BCRegistries;
import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.builders.container.ContainerBuilder;
import buildcraft.builders.container.ContainerElectronicLibrary;
import buildcraft.builders.container.ContainerFiller;
import buildcraft.builders.container.ContainerFillerPlanner;
import buildcraft.builders.container.ContainerReplacer;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;

import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.lib.fabric.menu.FillerPlannerMenuKey;

public final class BCBuildersMenuTypes {
    public static MenuType<ContainerFiller> FILLER;
    public static MenuType<ContainerBuilder> BUILDER;
    public static MenuType<ContainerArchitectTable> ARCHITECT;
    public static MenuType<ContainerElectronicLibrary> LIBRARY;
    public static MenuType<ContainerReplacer> REPLACER;
    public static MenuType<ContainerFillerPlanner> FILLER_PLANNER;

    private BCBuildersMenuTypes() {}

    public static void register() {
        FILLER = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCBuilders.MODID, "filler"),
                ExtendedMenuTypes.<ContainerFiller>create(ContainerFiller::new));
        BUILDER = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCBuilders.MODID, "builder"),
                ExtendedMenuTypes.<ContainerBuilder>create(ContainerBuilder::new));
        ARCHITECT = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCBuilders.MODID, "architect"),
                ExtendedMenuTypes.<ContainerArchitectTable>create(ContainerArchitectTable::new));
        LIBRARY = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCBuilders.MODID, "library"),
                ExtendedMenuTypes.<ContainerElectronicLibrary>create(ContainerElectronicLibrary::new));
        REPLACER = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCBuilders.MODID, "replacer"),
                ExtendedMenuTypes.<ContainerReplacer>create(ContainerReplacer::new));
        FILLER_PLANNER = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCBuilders.MODID, "filler_planner"),
                new ExtendedMenuType<>(
                        (syncId, inv, key) -> new ContainerFillerPlanner(syncId, inv, key),
                        FillerPlannerMenuKey.STREAM_CODEC));
    }
}
