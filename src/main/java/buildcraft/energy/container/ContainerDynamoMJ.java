/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class ContainerDynamoMJ extends BcMenu {
   private static final ItemHandlerSimple FALLBACK_UPGRADES = createFallbackUpgrades();
   public final TileDynamoMJ dynamo;
   private final ContainerData data;
   // Wide values are split into unsigned 16-bit chunks (see BcMenu.chunk16): vanilla data slots lose
   // everything above the low short on the wire.
   private static final int DATA_POWER = 0;
   private static final int DATA_HEAT = 4;
   private static final int DATA_OUTPUT = 6;
   private static final int DATA_POWER_STAGE = 10;
   private static final int DATA_IS_BURNING_ENGINE = 11;
   private static final int DATA_FE_STORED = 12;
   private static final int DATA_COUNT = 14;

   public ContainerDynamoMJ(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileDynamoMJ.class));
   }

   public ContainerDynamoMJ(int containerId, Inventory playerInv, final TileDynamoMJ dynamo) {
      super(BCEnergyMenuTypes.DYNAMO_MJ, containerId, playerInv.player);
      this.dynamo = dynamo;
      if (dynamo != null && dynamo.getLevel() != null && !dynamo.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0, 1, 2, 3 -> chunk16(dynamo.getMjBattery().getStored(), index - DATA_POWER);
                  case 4, 5 -> chunk16(Float.floatToIntBits(dynamo.getHeat()), index - DATA_HEAT);
                  case 6, 7, 8, 9 -> chunk16(dynamo.getCurrentOutput(), index - DATA_OUTPUT);
                  case 10 -> dynamo.getPowerStage().ordinal();
                  case 11 -> dynamo.isBurning() ? 1 : 0;
                  case 12, 13 -> chunk16(dynamo.getCurrentFe(), index - DATA_FE_STORED);
                  default -> 0;
               };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
               return 14;
            }
         };
      } else {
         SimpleContainerData clientData = new SimpleContainerData(14);
         int heatBits = Float.floatToIntBits(20.0F);
         clientData.set(DATA_HEAT, chunk16(heatBits, 0));
         clientData.set(DATA_HEAT + 1, chunk16(heatBits, 1));
         this.data = clientData;
      }

      this.addDataSlots(this.data);
      ItemHandlerSimple upgrades = dynamo != null ? dynamo.upgrades : FALLBACK_UPGRADES;

      for (int slot = 0; slot < 4; slot++) {
         this.addSlot(new SlotBase(upgrades, slot, 44 + 18 * slot, 44) {
            @Override
            public int getMaxStackSize() {
               return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
               return 1;
            }
         });
      }

      this.addFullPlayerInventory(8, 95, playerInv);
   }

   public long getSyncedPower() {
      return readLong64(this.data, DATA_POWER);
   }

   public float getSyncedHeat() {
      return readFloat32(this.data, DATA_HEAT);
   }

   public EnumPowerStage getSyncedPowerStage() {
      int ordinal = this.data.get(DATA_POWER_STAGE);
      EnumPowerStage[] values = EnumPowerStage.values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : EnumPowerStage.BLUE;
   }

   public boolean isSyncedBurningEngine() {
      return this.data.get(DATA_IS_BURNING_ENGINE) != 0;
   }

   public long getSyncedCurrentOutput() {
      return readLong64(this.data, DATA_OUTPUT);
   }

   public int getSyncedFeStored() {
      return readInt32(this.data, DATA_FE_STORED);
   }

   @Override
   public boolean stillValid(Player player) {
      return this.dynamo != null && !this.dynamo.isRemoved()
         ? player.distanceToSqr(this.dynamo.getBlockPos().getX() + 0.5, this.dynamo.getBlockPos().getY() + 0.5, this.dynamo.getBlockPos().getZ() + 0.5) <= 64.0
         : false;
   }

   private static ItemHandlerSimple createFallbackUpgrades() {
      ItemHandlerSimple upgrades = new ItemHandlerSimple(4, 1);
      upgrades.setChecker((slot, stack) -> false);
      return upgrades;
   }
}
