/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.mj.MjRfConversion;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.fabric.transfer.EnergyStorageOps;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.lib.mj.MjBatteryReceiver;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class TileDynamoMJ extends TileElectricEngineBase {
   public static final long MAX_MJ = 1000L * MjAPI.MJ;
   private final MjBattery mjBattery;
   private final MjBatteryReceiver mjConnector;

   public TileDynamoMJ(BlockPos pos, BlockState state) {
      super(BCEnergyBlockEntities.DYNAMO_MJ, pos, state, 0, MAX_FE);
      this.mjBattery = new MjBattery(MAX_MJ);
      this.mjConnector = new MjBatteryReceiver(this.mjBattery);
   }

   @Nullable
   public EnergyStorage getSidedEnergyStorage(@Nullable Direction direction) {
      return direction != null && direction == this.getOrientation() ? this.energyStorage : null;
   }

   @Nonnull
   @Override
   protected IMjConnector createConnector() {
      return this.mjConnector;
   }

   public MjBattery getMjBattery() {
      return this.mjBattery;
   }

   public MjBatteryReceiver getMjReceiver() {
      return this.mjConnector;
   }

   @Override
   public boolean isBurning() {
      return this.mjBattery.getStored() > 0L && this.isRedstonePowered;
   }

   public int getFeProductionRate(long mjInput) {
      long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
      return mjPerRf == 0L ? 0 : (int)(mjInput / mjPerRf);
   }

   @Override
   protected void engineUpdate() {
      // The MJ Dynamo is the MJ->RF bridge, so it always converts regardless of the global auto-conversion mode.
      this.sendFeToReceiver();

      this.currentOutput = 0L;
      long mjStored = this.mjBattery.getStored();
      if (mjStored > 0L) {
         if (this.isRedstonePowered) {
            long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
            if (mjPerRf == 0L) {
               return;
            }

            int genFe = this.getFeProductionRate(this.getMjPerTick());
            int maxFe = (int)Math.min(genFe, mjStored / mjPerRf);
            int currentFe = this.getCurrentFe();
            maxFe = Math.min(maxFe, 10000 - currentFe);
            if (maxFe <= 0) {
               return;
            }

            if (this.mjBattery.extractPower(maxFe * mjPerRf)) {
               this.currentOutput = maxFe;
               this.energyStorage.set(currentFe + maxFe);
            }
         } else {
            this.currentOutput = 0L;
         }
      }
   }

   private void sendFeToReceiver() {
      int currentFe = this.getCurrentFe();
      if (this.level != null && currentFe > 0) {
         EnergyStorage receiver = this.getFeReceiver(this.orientation);
         if (receiver != null) {
            int accepted = EnergyStorageOps.insert(receiver, currentFe, true);
            if (accepted > 0) {
               this.energyStorage.set(currentFe - accepted);
            }
         }
      }
   }

   @Nullable
   public EnergyStorage getFeReceiver(Direction side) {
      if (this.level == null) {
         return null;
      }

      BlockPos pos = this.getBlockPos();

      for (int len = 0; len <= this.getMaxChainLength(); len++) {
         BlockPos targetPos = pos.relative(side);
         BlockEntity tile = this.level.getBlockEntity(targetPos);
         if (tile == null) {
            return null;
         }

         if (tile.getClass() != this.getClass()) {
            return BcTransfers.energy(this.level, targetPos, side.getOpposite());
         }

         if (((TileDynamoMJ)tile).orientation != side) {
            return null;
         }

         pos = targetPos;
      }

      return null;
   }

   @Nullable
   @Override
   public IMjReceiver getReceiverToPower(Direction side) {
      return this.getFeReceiver(side) != null ? new IMjReceiver() {
         @Override
         public long getPowerRequested() {
            return 1L;
         }

         @Override
         public long receivePower(long microJoules, boolean simulate) {
            return 0L;
         }

         @Override
         public boolean canConnect(IMjConnector other) {
            return true;
         }
      } : null;
   }

   @Override
   public long getMaxPower() {
      return MAX_MJ;
   }

   @Override
   public long maxPowerReceived() {
      return 0L;
   }

   @Override
   public long maxPowerExtracted() {
      return 0L;
   }

   @Override
   public long extractPower(long min, long max, boolean doExtract) {
      return !doExtract && this.currentOutput > 0L ? Math.max(min, 1L) : 0L;
   }

   @Override
   protected void sendPower(@Nullable IMjReceiver receiver) {
   }

   @Override
   public long getCurrentOutput() {
      long mjPerRf = MjRfConversion.createParsed(BCLibConfig.mjRfConversionAmount.get()).mjPerRf;
      return mjPerRf <= 0L ? 0L : this.currentOutput * mjPerRf;
   }

   @Override
   protected int getMaxChainLength() {
      return 3;
   }

   @Override
   protected void writeData(BcValueOut output) {
      super.writeData(output);
      output.putLong("mjStored", this.mjBattery.getStored());
   }

   @Override
   public void readData(BcValueIn input) {
      super.readData(input);
      CompoundTag mjTag = new CompoundTag();
      mjTag.putLong("stored", input.getLongOr("mjStored", 0L));
      this.mjBattery.deserializeNBT(mjTag);
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftenergy.mj_dynamo");
   }

   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerDynamoMJ(containerId, playerInv, this);
   }
}
