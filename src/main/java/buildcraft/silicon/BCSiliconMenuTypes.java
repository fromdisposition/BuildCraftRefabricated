package buildcraft.silicon;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.lib.fabric.menu.GateMenuKey;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.container.ContainerGate;
import buildcraft.silicon.container.ContainerIntegrationTable;

public final class BCSiliconMenuTypes {
    public static MenuType<ContainerAssemblyTable> ASSEMBLY_TABLE;
    public static MenuType<ContainerIntegrationTable> INTEGRATION_TABLE;
    public static MenuType<ContainerAdvancedCraftingTable> ADVANCED_CRAFTING_TABLE;
    public static MenuType<ContainerGate> GATE;

    private BCSiliconMenuTypes() {}

    public static void register() {
        ASSEMBLY_TABLE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCSilicon.MODID, "assembly_table"),
                ExtendedMenuTypes.create(ContainerAssemblyTable::new));
        INTEGRATION_TABLE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCSilicon.MODID, "integration_table"),
                ExtendedMenuTypes.create(ContainerIntegrationTable::new));
        ADVANCED_CRAFTING_TABLE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCSilicon.MODID, "advanced_crafting_table"),
                ExtendedMenuTypes.create(ContainerAdvancedCraftingTable::new));
        GATE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCSilicon.MODID, "gate"),
                new ExtendedMenuType<>(
                        (syncId, inv, key) -> new ContainerGate(syncId, inv, key),
                        GateMenuKey.STREAM_CODEC));
    }
}
