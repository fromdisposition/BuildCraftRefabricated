/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.lib.fabric.menu.GateMenuKey;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.container.ContainerChargingTable;
import buildcraft.silicon.container.ContainerGate;
import buildcraft.silicon.container.ContainerIntegrationTable;
import buildcraft.silicon.container.ContainerPackager;
import buildcraft.silicon.container.ContainerProgrammingTable;
import buildcraft.silicon.container.ContainerStampingTable;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public final class BCSiliconMenuTypes {
   public static MenuType<ContainerAssemblyTable> ASSEMBLY_TABLE;
   public static MenuType<ContainerIntegrationTable> INTEGRATION_TABLE;
   public static MenuType<ContainerAdvancedCraftingTable> ADVANCED_CRAFTING_TABLE;
   public static MenuType<ContainerChargingTable> CHARGING_TABLE;
   public static MenuType<ContainerProgrammingTable> PROGRAMMING_TABLE;
   public static MenuType<ContainerStampingTable> STAMPING_TABLE;
   public static MenuType<ContainerPackager> PACKAGER;
   public static MenuType<ContainerGate> GATE;

   private BCSiliconMenuTypes() {
   }

   public static void register() {
      ASSEMBLY_TABLE = BCRegistries.registerMenuType("buildcraftsilicon", "assembly_table", ExtendedMenuTypes.create(ContainerAssemblyTable::new));
      INTEGRATION_TABLE = BCRegistries.registerMenuType("buildcraftsilicon", "integration_table", ExtendedMenuTypes.create(ContainerIntegrationTable::new));
      ADVANCED_CRAFTING_TABLE = BCRegistries.registerMenuType(
         "buildcraftsilicon", "advanced_crafting_table", ExtendedMenuTypes.create(ContainerAdvancedCraftingTable::new)
      );
      CHARGING_TABLE = BCRegistries.registerMenuType("buildcraftsilicon", "charging_table", ExtendedMenuTypes.create(ContainerChargingTable::new));
      PROGRAMMING_TABLE = BCRegistries.registerMenuType("buildcraftsilicon", "programming_table", ExtendedMenuTypes.create(ContainerProgrammingTable::new));
      STAMPING_TABLE = BCRegistries.registerMenuType("buildcraftsilicon", "stamping_table", ExtendedMenuTypes.create(ContainerStampingTable::new));
      PACKAGER = BCRegistries.registerMenuType("buildcraftsilicon", "packager", ExtendedMenuTypes.create(ContainerPackager::new));
      GATE = BCRegistries.registerMenuType(
         "buildcraftsilicon", "gate", new ExtendedMenuType<>((int syncId, Inventory inv, GateMenuKey key) -> new ContainerGate(syncId, inv, key), GateMenuKey.STREAM_CODEC)
      );
   }
}
