package buildcraft.factory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import buildcraft.fabric.BCRegistries;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.container.ContainerChute;
import buildcraft.factory.container.ContainerDistiller;
import buildcraft.factory.container.ContainerHeatExchange;
import buildcraft.factory.container.ContainerTank;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;

public final class BCFactoryMenuTypes {
    public static MenuType<ContainerAutoCraftItems> AUTO_WORKBENCH_ITEMS;
    public static MenuType<ContainerTank> TANK;
    public static MenuType<ContainerChute> CHUTE;
    public static MenuType<ContainerDistiller> DISTILLER;
    public static MenuType<ContainerHeatExchange> HEAT_EXCHANGE;

    private BCFactoryMenuTypes() {}

    public static void register() {
        AUTO_WORKBENCH_ITEMS = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCFactory.MODID, "auto_workbench_items"),
                ExtendedMenuTypes.create(ContainerAutoCraftItems::new));
        TANK = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCFactory.MODID, "tank"),
                ExtendedMenuTypes.create(ContainerTank::new));
        CHUTE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCFactory.MODID, "chute"),
                ExtendedMenuTypes.create(ContainerChute::new));
        DISTILLER = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCFactory.MODID, "distiller"),
                ExtendedMenuTypes.create(ContainerDistiller::new));
        HEAT_EXCHANGE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCFactory.MODID, "heat_exchange"),
                ExtendedMenuTypes.create(ContainerHeatExchange::new));
    }
}
