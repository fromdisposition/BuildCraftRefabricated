/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjRfConversion;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.fabric.transfer.EnergyStorageOps;
import buildcraft.lib.fabric.transfer.BcTransfers;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

public class TileEngineRF extends TileElectricEngineBase {
   public TileEngineRF(BlockPos pos, BlockState state) {
      super(BCEnergyBlockEntities.ENGINE_FE, pos, state, MAX_FE, 0);
   }

   @Nullable
   public EnergyStorage getSidedEnergyStorage(@Nullable Direction direction) {
      return direction != null && direction != this.getOrientation() ? this.energyStorage : null;
   }

   @Override
   public boolean isBurning() {
      return this.getCurrentFe() > 0 && this.isRedstonePowered;
   }

   public int getFeConsumptionRate() {
      long mjPerTick = this.getMjPerTick();
      long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
      return mjPerRf == 0L ? 0 : (int)(mjPerTick / mjPerRf);
   }

   @Override
   protected void engineUpdate() {
      // The Energy Engine is the RF->MJ bridge: it always converts, regardless of the global auto-conversion
      // mode, which only gates whether ordinary machines and pipes accept FE directly.
      this.pullFeFromNeighbors();

      this.currentOutput = 0L;
      int currentFe = this.getCurrentFe();
      if (currentFe > 0 && this.isRedstonePowered) {
         long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
         if (mjPerRf != 0L) {
            int maxFe = this.getFeConsumptionRate();
            // Clamp to the FE that still fits in the MJ buffer (partial fill) rather than an all-or-nothing gate.
            long room = this.getMaxPower() - this.power;
            int feConsumed = (int) Math.max(0L, Math.min((long) Math.min(currentFe, maxFe), room / mjPerRf));
            if (feConsumed > 0) {
               long mjGenerated = feConsumed * mjPerRf;
               this.currentOutput = mjGenerated;
               this.power += mjGenerated;
               this.energyStorage.set(currentFe - feConsumed);
            }
         }
      }
   }

   private void pullFeFromNeighbors() {
      int currentFe = this.getCurrentFe();
      if (this.level != null && currentFe < 10000) {
         for (Direction dir : Direction.values()) {
            if (dir != this.orientation) {
               if (currentFe >= 10000) {
                  break;
               }

               BlockPos neighborPos = this.getBlockPos().relative(dir);
               EnergyStorage storage = BcTransfers.energy(this.level, neighborPos, dir.getOpposite());
               if (storage != null) {
                  int want = 10000 - currentFe;
                  if (want <= 0) {
                     break;
                  }

                  int extracted = EnergyStorageOps.extract(storage, want, true);
                  if (extracted > 0) {
                     currentFe += extracted;
                     this.energyStorage.set(currentFe);
                  }
               }
            }
         }
      }
   }

   @Nonnull
   @Override
   protected IMjConnector createConnector() {
      return new EngineConnector(false);
   }

   @Override
   public long getMaxPower() {
      return 1000L * MjAPI.MJ;
   }

   @Override
   public long maxPowerReceived() {
      return 200L * MjAPI.MJ;
   }

   @Override
   public long maxPowerExtracted() {
      return 500L * MjAPI.MJ;
   }

   @Override
   public long getCurrentOutput() {
      return this.currentOutput;
   }

   @Override
   protected int getMaxChainLength() {
      return 4;
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("block.buildcraftenergy.engine_rf");
   }

   @Override
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerEngineRF(containerId, playerInv, this);
   }
}
