/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.IFuelManager;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.fluid.CombinedFluidStorage;
import buildcraft.lib.fabric.transfer.fluid.SingleFluidTank;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.AdvancementUtil;
import java.util.List;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileEngineIron_BC8 extends TileEngineBase_BC8 implements MenuProvider, BlockEntityExtendedMenu {
   private static final Identifier ADVANCEMENT_POWERING_UP = Identifier.parse("buildcraftenergy:powering_up");
   private static final Identifier ADVANCEMENT_ICE_COOL = Identifier.parse("buildcraftenergy:ice_cool");
   public static final int MAX_FLUID = 10000;
   public static final double COOLDOWN_RATE = 0.05;
   public static final int MAX_COOLANT_PER_TICK = 40;
   public static final double HEAT_PER_MJ = 0.0023;
   public static final double IDEAL_HEAT = 204.0;
   public final SingleFluidTank tankFuel = new SingleFluidTank(10000, SingleFluidTank.TankAccess.filteredInput(this::isValidFuel));
   public final SingleFluidTank tankCoolant = new SingleFluidTank(10000, SingleFluidTank.TankAccess.filteredInput(this::isValidCoolant));
   public final SingleFluidTank tankResidue = new SingleFluidTank(10000, SingleFluidTank.TankAccess.MACHINE_OUTPUT);
   private int penaltyCooling = 0;
   private boolean lastPowered = false;
   private double burnTime;
   private double residueAmount = 0.0;
   private IFuel currentFuel;

   public TileEngineIron_BC8(BlockPos pos, BlockState state) {
      super(BCEnergyBlockEntities.ENGINE_IRON, pos, state);
   }

   @Nonnull
   @Override
   protected IMjConnector createConnector() {
      return new EngineConnector(false);
   }

   @Override
   public boolean isBurning() {
      return !this.tankFuel.isEmpty() && this.tankFuel.getAmountMb() > 0 && this.penaltyCooling == 0 && this.isRedstonePowered;
   }

   @Override
   protected void engineUpdate() {
      this.burn();
   }

   protected void burn() {
      if (this.getPowerStage() != EnumPowerStage.OVERHEAT) {
         FluidStack fuel = this.tankFuel.getFluidStack();
         if (this.currentFuel == null || this.currentFuel.getFluid().getFluid() != fuel.getFluid()) {
            this.currentFuel = BuildcraftFuelRegistry.fuel.getFuel(fuel);
         }

         if (!fuel.isEmpty() && this.currentFuel != null) {
            if (this.penaltyCooling <= 0) {
               if (this.isRedstonePowered) {
                  this.lastPowered = true;
                  if (this.getOwner() != null && this.level != null) {
                     AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, ADVANCEMENT_POWERING_UP);
                  }

                  if (this.burnTime > 0.0 || fuel.getAmount() > 0) {
                     if (this.burnTime > 0.0) {
                        this.burnTime--;
                     }

                     if (this.burnTime <= 0.0) {
                        if (this.tankFuel.getAmountMb() <= 0) {
                           this.currentFuel = null;
                           this.currentOutput = 0L;
                           return;
                        }

                        Transaction tx = Transaction.openOuter();

                        try {
                           this.tankFuel.extractMbInternal(fuel, 1, tx);
                           tx.commit();
                        } catch (Throwable var12) {
                           if (tx != null) {
                              try {
                                 tx.close();
                              } catch (Throwable var11) {
                                 var12.addSuppressed(var11);
                              }
                           }

                           throw var12;
                        }

                        if (tx != null) {
                           tx.close();
                        }

                        this.burnTime = this.burnTime + this.currentFuel.getTotalBurningTime() / 1000.0;
                        if (this.currentFuel instanceof IFuelManager.IDirtyFuel dirtyFuel) {
                           FluidStack residueFluid = dirtyFuel.getResidue().copy();
                           this.residueAmount = this.residueAmount + residueFluid.getAmount() / 1000.0;
                           if (this.residueAmount >= 1.0) {
                              int residueInt = Mth.floor(this.residueAmount);
                              Transaction txx = Transaction.openOuter();

                              try {
                                 int filled = this.tankResidue.insertMbInternal(residueFluid, residueInt, txx);
                                 if (filled > 0) {
                                    txx.commit();
                                 }

                                 this.residueAmount -= filled;
                              } catch (Throwable var14) {
                                 if (txx != null) {
                                    try {
                                       txx.close();
                                    } catch (Throwable var10) {
                                       var14.addSuppressed(var10);
                                    }
                                 }

                                 throw var14;
                              }

                              if (txx != null) {
                                 txx.close();
                              }
                           }
                        }
                     }

                     this.addPower(this.currentFuel.getPowerPerCycle());
                     this.heat = this.heat + (float)(this.currentFuel.getPowerPerCycle() * 0.0023 / MjAPI.MJ);
                  }
               } else if (this.lastPowered) {
                  this.lastPowered = false;
                  this.penaltyCooling = 10;
               }
            }

            if (this.burnTime <= 0.0 && this.tankFuel.getAmountMb() <= 0) {
               Transaction tx = Transaction.openOuter();

               try {
                  FluidStack remaining = this.tankFuel.getFluidStack();
                  if (!remaining.isEmpty()) {
                     this.tankFuel.extractMbInternal(remaining, 10000, tx);
                  }

                  tx.commit();
               } catch (Throwable var13) {
                  if (tx != null) {
                     try {
                        tx.close();
                     } catch (Throwable var9) {
                        var13.addSuppressed(var9);
                     }
                  }

                  throw var13;
               }

               if (tx != null) {
                  tx.close();
               }
            }
         }
      }
   }

   @Override
   public void updateHeatLevel() {
      double target;
      if (!(this.heat > 20.0F) || this.penaltyCooling <= 0 && this.isRedstonePowered) {
         if (this.heat > 204.0) {
            target = 204.0;
         } else {
            target = this.heat;
         }
      } else {
         this.heat -= 0.05F;
         target = 20.0;
      }

      if (target != this.heat) {
         double coolingBuffer = 0.0;
         double extraHeat = this.heat - target;
         if (extraHeat > 0.0 && this.tankCoolant.getAmountMb() > 0) {
            FluidStack coolRes = this.tankCoolant.getFluidStack();
            float coolPerMb = BuildcraftFuelRegistry.coolant.getDegreesPerMb(coolRes.copyWithAmount(1), this.heat);
            if (coolPerMb > 0.0F) {
               int coolantAmount = Math.min(40, this.tankCoolant.getAmountMb());
               coolingBuffer += coolantAmount * coolPerMb;
               Transaction tx = Transaction.openOuter();

               try {
                  this.tankCoolant.extractMbInternal(coolRes, coolantAmount, tx);
                  tx.commit();
               } catch (Throwable var14) {
                  if (tx != null) {
                     try {
                        tx.close();
                     } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                     }
                  }

                  throw var14;
               }

               if (tx != null) {
                  tx.close();
               }

               if (!coolRes.isEmpty() && !coolRes.getFluid().isSame(Fluids.WATER) && this.getOwner() != null && this.level != null) {
                  AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, ADVANCEMENT_ICE_COOL);
               }
            }
         }

         this.heat -= (float)coolingBuffer;
         this.getPowerStage();
      }

      if (this.heat <= 20.0F && this.penaltyCooling > 0) {
         this.penaltyCooling--;
      }

      if (this.heat <= 20.0F) {
         this.heat = 20.0F;
      }
   }

   @Override
   public double getPistonSpeed() {
      switch (this.getPowerStage()) {
         case BLUE:
            return 0.04;
         case GREEN:
            return 0.05;
         case YELLOW:
            return 0.06;
         case RED:
            return 0.07;
         default:
            return 0.0;
      }
   }

   public boolean isActive() {
      return this.penaltyCooling <= 0;
   }

   @Override
   public long getMaxPower() {
      return 10000L * MjAPI.MJ;
   }

   @Override
   public long maxPowerReceived() {
      return 2000L * MjAPI.MJ;
   }

   @Override
   public long maxPowerExtracted() {
      return 500L * MjAPI.MJ;
   }

   @Override
   public float explosionRange() {
      return 4.0F;
   }

   @Override
   protected int getMaxChainLength() {
      return 4;
   }

   @Override
   public long getCurrentOutput() {
      return this.currentFuel == null ? 0L : this.currentFuel.getPowerPerCycle();
   }

   @Override
   public long minPowerReceived() {
      return MjAPI.MJ;
   }

   protected void addPower(long microMj) {
      this.power = Math.min(this.power + microMj, this.getMaxPower());
   }

   private boolean isValidFuel(FluidStack fluid) {
      return BuildcraftFuelRegistry.fuel != null && BuildcraftFuelRegistry.fuel.getFuel(fluid) != null;
   }

   private boolean isValidCoolant(FluidStack fluid) {
      return BuildcraftFuelRegistry.coolant != null && BuildcraftFuelRegistry.coolant.getCoolant(fluid) != null;
   }

   private boolean isResidue(FluidStack fluid) {
      if (this.level != null && this.level.isClientSide()) {
         return true;
      } else {
         return this.currentFuel instanceof IFuelManager.IDirtyFuel dirtyFuel ? FluidStack.isSameFluid(fluid, dirtyFuel.getResidue()) : false;
      }
   }

   public Storage<FluidVariant> getCombinedFluidStorage() {
      return new CombinedFluidStorage(List.of(this.tankFuel, this.tankCoolant), List.of(this.tankResidue));
   }

   @Nullable
   public Storage<FluidVariant> getSidedFluidStorage(@Nullable Direction direction) {
      return direction != null && direction != this.getOrientation() ? this.getCombinedFluidStorage() : null;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("penaltyCooling", this.penaltyCooling);
      output.putDouble("burnTime", this.burnTime);
      output.putDouble("residueAmount", this.residueAmount);
      if (!this.tankFuel.isEmpty()) {
         Identifier fuelId = BuiltInRegistries.FLUID.getKey(this.tankFuel.getFluidStack().getFluid());
         output.putString("fuelFluid", fuelId.toString());
         output.putInt("fuelAmount", this.tankFuel.getAmountMb());
      }

      if (!this.tankCoolant.isEmpty()) {
         Identifier coolId = BuiltInRegistries.FLUID.getKey(this.tankCoolant.getFluidStack().getFluid());
         output.putString("coolantFluid", coolId.toString());
         output.putInt("coolantAmount", this.tankCoolant.getAmountMb());
      }

      if (!this.tankResidue.isEmpty()) {
         Identifier resId = BuiltInRegistries.FLUID.getKey(this.tankResidue.getFluidStack().getFluid());
         output.putString("residueFluid", resId.toString());
         output.putInt("residueAmountTank", this.tankResidue.getAmountMb());
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.penaltyCooling = input.getIntOr("penaltyCooling", 0);
      this.burnTime = input.getDoubleOr("burnTime", 0.0);
      this.residueAmount = Math.max(0.0, input.getDoubleOr("residueAmount", 0.0));
      this.loadTank(input, "fuelFluid", "fuelAmount", this.tankFuel);
      this.loadTank(input, "coolantFluid", "coolantAmount", this.tankCoolant);
      this.loadTank(input, "residueFluid", "residueAmountTank", this.tankResidue);
   }

   private void loadTank(ValueInput input, String fluidKey, String amountKey, SingleFluidTank tank) {
      String fluidId = input.getStringOr(fluidKey, "");
      if (!fluidId.isEmpty()) {
         Identifier id = Identifier.tryParse(fluidId);
         if (id != null) {
            Fluid fluid = Mc26Compat.getFluid(id);
            if (fluid != null && fluid != Fluids.EMPTY) {
               int amount = input.getIntOr(amountKey, 0);
               if (amount > 0) {
                  tank.setContents(new FluidStack(fluid, amount));
               }
            }
         }
      }
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftenergy.engine_iron");
   }

   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerEngineIron_BC8(containerId, playerInv, this);
   }
}
