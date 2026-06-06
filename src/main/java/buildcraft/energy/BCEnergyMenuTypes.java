package buildcraft.energy;

import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import net.minecraft.world.inventory.MenuType;

public final class BCEnergyMenuTypes {
   public static MenuType<ContainerEngineStone_BC8> ENGINE_STONE;
   public static MenuType<ContainerEngineIron_BC8> ENGINE_IRON;
   public static MenuType<ContainerEngineRF> ENGINE_FE;
   public static MenuType<ContainerDynamoMJ> DYNAMO_MJ;

   private BCEnergyMenuTypes() {
   }

   public static void register() {
      ENGINE_STONE = BCRegistries.registerMenuType("buildcraftenergy", "engine_stone", ExtendedMenuTypes.create(ContainerEngineStone_BC8::new));
      ENGINE_IRON = BCRegistries.registerMenuType("buildcraftenergy", "engine_iron", ExtendedMenuTypes.create(ContainerEngineIron_BC8::new));
      ENGINE_FE = BCRegistries.registerMenuType("buildcraftenergy", "engine_rf", ExtendedMenuTypes.create(ContainerEngineRF::new));
      DYNAMO_MJ = BCRegistries.registerMenuType("buildcraftenergy", "mj_dynamo", ExtendedMenuTypes.create(ContainerDynamoMJ::new));
   }
}
