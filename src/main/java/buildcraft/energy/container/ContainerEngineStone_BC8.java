/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.BcMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerEngineStone_BC8 extends BcMenu {
   public final TileEngineStone_BC8 engine;
   private final ContainerData data;
   // Wide values are split into unsigned 16-bit chunks (see BcMenu.chunk16): vanilla data slots lose
   // everything above the low short on the wire.
   private static final int DATA_BURN_TIME = 0;
   private static final int DATA_TOTAL_BURN_TIME = 1;
   private static final int DATA_POWER = 2;
   private static final int DATA_HEAT = 6;
   private static final int DATA_OUTPUT = 8;
   private static final int DATA_POWER_STAGE = 12;
   private static final int DATA_IS_BURNING_ENGINE = 13;
   private static final int DATA_COUNT = 14;

   public ContainerEngineStone_BC8(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileEngineStone_BC8.class));
   }

   public ContainerEngineStone_BC8(int containerId, Inventory playerInv, final TileEngineStone_BC8 engine) {
      super(BCEnergyMenuTypes.ENGINE_STONE, containerId, playerInv.player);
      this.engine = engine;
      if (engine != null && engine.getLevel() != null && !engine.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0 -> engine.burnTime;
                  case 1 -> engine.totalBurnTime;
                  case 2, 3, 4, 5 -> chunk16(engine.getPower(), index - DATA_POWER);
                  case 6, 7 -> chunk16(Float.floatToIntBits(engine.getHeat()), index - DATA_HEAT);
                  case 8, 9, 10, 11 -> chunk16(engine.currentOutput, index - DATA_OUTPUT);
                  case 12 -> engine.getPowerStage().ordinal();
                  case 13 -> engine.isBurning() ? 1 : 0;
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
      this.addSlot(new ContainerEngineStone_BC8.FuelSlot(engine, 0, 80, 36));
      this.addFullPlayerInventory(8, 68, playerInv);
   }

   public int getBurnTime() {
      return this.data.get(0);
   }

   public int getTotalBurnTime() {
      return this.data.get(1);
   }

   public boolean isBurning() {
      return this.getBurnTime() > 0;
   }

   public float getBurnProgress() {
      int total = this.getTotalBurnTime();
      return total <= 0 ? 0.0F : (float)this.getBurnTime() / total;
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

   @Override
   public boolean stillValid(Player player) {
      return this.engine != null && Container.stillValidBlockEntity(this.engine, player);
   }

   private static class FuelContainer implements Container {
      private final TileEngineStone_BC8 engine;

      FuelContainer(TileEngineStone_BC8 engine) {
         this.engine = engine;
      }

      public int getContainerSize() {
         return 1;
      }

      public boolean isEmpty() {
         return this.engine == null || this.engine.getFuelStack().isEmpty();
      }

      public ItemStack getItem(int slot) {
         return this.engine != null ? this.engine.getFuelStack() : ItemStack.EMPTY;
      }

      public ItemStack removeItem(int slot, int count) {
         if (this.engine == null) {
            return ItemStack.EMPTY;
         }

         ItemStack stack = this.engine.getFuelStack();
         if (stack.isEmpty()) {
            return ItemStack.EMPTY;
         }

         ItemStack result = stack.split(count);
         if (stack.isEmpty()) {
            this.engine.setFuelStack(ItemStack.EMPTY);
         } else {
            this.engine.setFuelStack(stack);
         }

         return result;
      }

      public ItemStack removeItemNoUpdate(int slot) {
         if (this.engine == null) {
            return ItemStack.EMPTY;
         }

         ItemStack stack = this.engine.getFuelStack();
         this.engine.setFuelStack(ItemStack.EMPTY);
         return stack;
      }

      public void setItem(int slot, ItemStack stack) {
         if (this.engine != null) {
            this.engine.setFuelStack(stack);
         }
      }

      public void setChanged() {
         if (this.engine != null) {
            this.engine.setChanged();
         }
      }

      public boolean stillValid(Player player) {
         return true;
      }

      public void clearContent() {
         if (this.engine != null) {
            this.engine.setFuelStack(ItemStack.EMPTY);
         }
      }
   }

   private static class FuelSlot extends Slot {
      private final TileEngineStone_BC8 engine;

      public FuelSlot(TileEngineStone_BC8 engine, int index, int x, int y) {
         super(new ContainerEngineStone_BC8.FuelContainer(engine), index, x, y);
         this.engine = engine;
      }

      public boolean mayPlace(ItemStack stack) {
         return this.engine != null && this.engine.isValidFuel(stack);
      }
   }
}
