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
   private static final int DATA_POWER_HI = 0;
   private static final int DATA_POWER_LO = 1;
   private static final int DATA_HEAT = 2;
   private static final int DATA_POWER_STAGE = 3;
   private static final int DATA_BURNING = 4;
   private static final int DATA_CURRENT_OUTPUT_HI = 5;
   private static final int DATA_CURRENT_OUTPUT_LO = 6;
   private static final int DATA_FUEL_AMOUNT = 7;
   private static final int DATA_COOLANT_AMOUNT = 8;
   private static final int DATA_RESIDUE_AMOUNT = 9;
   private static final int DATA_FUEL_FLUID_ID = 10;
   private static final int DATA_COOLANT_FLUID_ID = 11;
   private static final int DATA_RESIDUE_FLUID_ID = 12;
   private static final int DATA_COUNT = 13;

   public ContainerEngineIron_BC8(int containerId, Inventory playerInv, final TileEngineIron_BC8 engine) {
      super(BCEnergyMenuTypes.ENGINE_IRON, containerId, playerInv.player);
      this.engine = engine;
      if (engine != null && engine.getLevel() != null && !engine.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0 -> (int)(engine.getPower() >>> 32);
                  case 1 -> (int)(engine.getPower() & 4294967295L);
                  case 2 -> Float.floatToIntBits(engine.getHeat());
                  case 3 -> engine.getPowerStage().ordinal();
                  case 4 -> engine.isBurning() ? 1 : 0;
                  case 5 -> (int)(engine.getCurrentOutput() >>> 32);
                  case 6 -> (int)(engine.getCurrentOutput() & 4294967295L);
                  case 7 -> engine.tankFuel.getAmountMb();
                  case 8 -> engine.tankCoolant.getAmountMb();
                  case 9 -> engine.tankResidue.getAmountMb();
                  case 10 -> ContainerEngineIron_BC8.getFluidRegistryId(engine.tankFuel.getFluidStack());
                  case 11 -> ContainerEngineIron_BC8.getFluidRegistryId(engine.tankCoolant.getFluidStack());
                  case 12 -> ContainerEngineIron_BC8.getFluidRegistryId(engine.tankResidue.getFluidStack());
                  default -> 0;
               };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
               return 13;
            }
         };
      } else {
         SimpleContainerData clientData = new SimpleContainerData(13);
         clientData.set(2, Float.floatToIntBits(20.0F));
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
      return (long)this.data.get(0) << 32 | this.data.get(1) & 4294967295L;
   }

   public float getSyncedHeat() {
      return Float.intBitsToFloat(this.data.get(2));
   }

   public EnumPowerStage getSyncedPowerStage() {
      int ord = this.data.get(3);
      EnumPowerStage[] values = EnumPowerStage.VALUES;
      return values[Math.min(ord, values.length - 1)];
   }

   public boolean isSyncedBurning() {
      return this.data.get(4) != 0;
   }

   public long getSyncedCurrentOutput() {
      return (long)this.data.get(5) << 32 | this.data.get(6) & 4294967295L;
   }

   public int getSyncedFuelAmount() {
      return this.data.get(7);
   }

   public int getSyncedCoolantAmount() {
      return this.data.get(8);
   }

   public int getSyncedResidueAmount() {
      return this.data.get(9);
   }

   public Fluid getSyncedFuelFluid() {
      return getFluidFromRegistryId(this.data.get(10));
   }

   public Fluid getSyncedCoolantFluid() {
      return getFluidFromRegistryId(this.data.get(11));
   }

   public Fluid getSyncedResidueFluid() {
      return getFluidFromRegistryId(this.data.get(12));
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
      return this.engine != null && !this.engine.isRemoved()
         ? player.distanceToSqr(this.engine.getBlockPos().getX() + 0.5, this.engine.getBlockPos().getY() + 0.5, this.engine.getBlockPos().getZ() + 0.5) <= 64.0
         : false;
   }
}
