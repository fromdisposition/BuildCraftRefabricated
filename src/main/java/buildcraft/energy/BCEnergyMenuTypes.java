package buildcraft.energy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;

public final class BCEnergyMenuTypes {
    public static MenuType<ContainerEngineStone_BC8> ENGINE_STONE;
    public static MenuType<ContainerEngineIron_BC8> ENGINE_IRON;
    public static MenuType<ContainerEngineRF> ENGINE_FE;
    public static MenuType<ContainerDynamoMJ> DYNAMO_MJ;

    private BCEnergyMenuTypes() {}

    public static void register() {
        ENGINE_STONE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCEnergy.MODID, "engine_stone"),
                ExtendedMenuTypes.create(ContainerEngineStone_BC8::new));
        ENGINE_IRON = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCEnergy.MODID, "engine_iron"),
                ExtendedMenuTypes.create(ContainerEngineIron_BC8::new));
        ENGINE_FE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCEnergy.MODID, "engine_rf"),
                ExtendedMenuTypes.create(ContainerEngineRF::new));
        DYNAMO_MJ = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCEnergy.MODID, "mj_dynamo"),
                ExtendedMenuTypes.create(ContainerDynamoMJ::new));
    }
}
