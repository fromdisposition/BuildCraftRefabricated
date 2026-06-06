package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.lib.fabric.menu.GateMenuKey;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.container.ContainerGate;
import buildcraft.silicon.container.ContainerIntegrationTable;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class BCSiliconMenuTypes {
   public static MenuType<ContainerAssemblyTable> ASSEMBLY_TABLE;
   public static MenuType<ContainerIntegrationTable> INTEGRATION_TABLE;
   public static MenuType<ContainerAdvancedCraftingTable> ADVANCED_CRAFTING_TABLE;
   public static MenuType<ContainerGate> GATE;

   private BCSiliconMenuTypes() {
   }

   public static void register() {
      ASSEMBLY_TABLE = BCRegistries.registerMenuType("buildcraftsilicon", "assembly_table", ExtendedMenuTypes.create(ContainerAssemblyTable::new));
      INTEGRATION_TABLE = BCRegistries.registerMenuType("buildcraftsilicon", "integration_table", ExtendedMenuTypes.create(ContainerIntegrationTable::new));
      ADVANCED_CRAFTING_TABLE = BCRegistries.registerMenuType(
         "buildcraftsilicon", "advanced_crafting_table", ExtendedMenuTypes.create(ContainerAdvancedCraftingTable::new)
      );
      GATE = BCRegistries.registerMenuType(
         "buildcraftsilicon", "gate", new ExtendedMenuType<>((int syncId, Inventory inv, GateMenuKey key) -> new ContainerGate(syncId, inv, key), GateMenuKey.STREAM_CODEC)
      );
   }
}
