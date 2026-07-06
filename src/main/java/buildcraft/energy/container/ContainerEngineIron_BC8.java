/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.container;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ContainerEngineIron_BC8 extends BcMenu {
   public final TileEngineIron_BC8 engine;
   private final ContainerData data;
   public final WidgetFluidTank widgetFuel;
   public final WidgetFluidTank widgetCoolant;
   public final WidgetFluidTank widgetResidue;
   // Wide values are split into unsigned 16-bit chunks (see BcMenu.chunk16): vanilla data slots lose
   // everything above the low short on the wire. Tank amounts are also chunked — tanks hold thousands of mB.
   private static final int DATA_POWER = 0;
   private static final int DATA_HEAT = 4;
   private static final int DATA_POWER_STAGE = 6;
   private static final int DATA_BURNING = 7;
   private static final int DATA_CURRENT_OUTPUT = 8;
   private static final int DATA_FUEL_AMOUNT = 12;
   private static final int DATA_COOLANT_AMOUNT = 14;
   private static final int DATA_RESIDUE_AMOUNT = 16;
   private static final int DATA_FUEL_FLUID_ID = 18;
   private static final int DATA_COOLANT_FLUID_ID = 19;
   private static final int DATA_RESIDUE_FLUID_ID = 20;
   private static final int DATA_COUNT = 21;

   public ContainerEngineIron_BC8(int containerId, Inventory playerInv, final TileEngineIron_BC8 engine) {
      super(BCEnergyMenuTypes.ENGINE_IRON, containerId, playerInv.player);
      this.engine = engine;
      if (engine != null && engine.getLevel() != null && !engine.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0, 1, 2, 3 -> chunk16(engine.getPower(), index - DATA_POWER);
                  case 4, 5 -> chunk16(Float.floatToIntBits(engine.getHeat()), index - DATA_HEAT);
                  case 6 -> engine.getPowerStage().ordinal();
                  case 7 -> engine.isBurning() ? 1 : 0;
                  case 8, 9, 10, 11 -> chunk16(engine.getCurrentOutput(), index - DATA_CURRENT_OUTPUT);
                  case 12, 13 -> chunk16(engine.tankFuel.getAmountMb(), index - DATA_FUEL_AMOUNT);
                  case 14, 15 -> chunk16(engine.tankCoolant.getAmountMb(), index - DATA_COOLANT_AMOUNT);
                  case 16, 17 -> chunk16(engine.tankResidue.getAmountMb(), index - DATA_RESIDUE_AMOUNT);
                  case 18 -> ContainerEngineIron_BC8.getFluidRegistryId(engine.tankFuel.getFluidStack());
                  case 19 -> ContainerEngineIron_BC8.getFluidRegistryId(engine.tankCoolant.getFluidStack());
                  case 20 -> ContainerEngineIron_BC8.getFluidRegistryId(engine.tankResidue.getFluidStack());
                  default -> 0;
               };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
               return 21;
            }
         };
      } else {
         SimpleContainerData clientData = new SimpleContainerData(21);
         int heatBits = Float.floatToIntBits(20.0F);
         clientData.set(DATA_HEAT, chunk16(heatBits, 0));
         clientData.set(DATA_HEAT + 1, chunk16(heatBits, 1));
         this.data = clientData;
      }

      this.addDataSlots(this.data);
      this.addFullPlayerInventory(8, 95);
      this.widgetFuel = this.addWidget(new WidgetFluidTank(this, engine != null ? engine.tankFuel : null));
      this.widgetCoolant = this.addWidget(new WidgetFluidTank(this, engine != null ? engine.tankCoolant : null));
      this.widgetResidue = this.addWidget(new WidgetFluidTank(this, engine != null ? engine.tankResidue : null));
   }

   public ContainerEngineIron_BC8(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileEngineIron_BC8.class));
   }

   public long getSyncedPower() {
      return readLong64(this.data, DATA_POWER);
   }

   public float getSyncedHeat() {
      return readFloat32(this.data, DATA_HEAT);
   }

   public EnumPowerStage getSyncedPowerStage() {
      int ord = this.data.get(DATA_POWER_STAGE);
      EnumPowerStage[] values = EnumPowerStage.VALUES;
      return values[Math.min(Math.max(ord, 0), values.length - 1)];
   }

   public boolean isSyncedBurning() {
      return this.data.get(DATA_BURNING) != 0;
   }

   public long getSyncedCurrentOutput() {
      return readLong64(this.data, DATA_CURRENT_OUTPUT);
   }

   public int getSyncedFuelAmount() {
      return readInt32(this.data, DATA_FUEL_AMOUNT);
   }

   public int getSyncedCoolantAmount() {
      return readInt32(this.data, DATA_COOLANT_AMOUNT);
   }

   public int getSyncedResidueAmount() {
      return readInt32(this.data, DATA_RESIDUE_AMOUNT);
   }

   public Fluid getSyncedFuelFluid() {
      return getFluidFromRegistryId(this.data.get(DATA_FUEL_FLUID_ID));
   }

   public Fluid getSyncedCoolantFluid() {
      return getFluidFromRegistryId(this.data.get(DATA_COOLANT_FLUID_ID));
   }

   public Fluid getSyncedResidueFluid() {
      return getFluidFromRegistryId(this.data.get(DATA_RESIDUE_FLUID_ID));
   }

   private static int getFluidRegistryId(FluidStack stack) {
      return stack.isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(stack.getFluid());
   }

   private static Fluid getFluidFromRegistryId(int id) {
      if (id < 0) {
         return Fluids.EMPTY;
      }

      Fluid fluid = (Fluid)BuiltInRegistries.FLUID.byId(id);
      return fluid != null ? fluid : Fluids.EMPTY;
   }

   @Override
   public boolean stillValid(Player player) {
      return this.engine != null && Container.stillValidBlockEntity(this.engine, player);
   }
}
