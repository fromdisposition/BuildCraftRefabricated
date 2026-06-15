/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;

public class BCTransportConfig {
   public static boolean disableRfPipe = false;
   public static long mjPerItem = 1_000_000L;
   public static long mjPerMillibucket = 1_000L;
   public static int basePowerRate = 4;
   public static int baseRfRate = 40;
   public static int baseFlowRate = 10;

   public static void registerPowerTransferData() {
      int rate = basePowerRate;
      powerTransfer(BCTransportPipes.cobblePower, rate, 16, false);
      powerTransfer(BCTransportPipes.stonePower, rate * 2, 32, false);
      powerTransfer(BCTransportPipes.woodPower, rate * 4, 128, true);
      powerTransfer(BCTransportPipes.sandstonePower, rate * 4, 32, false);
      powerTransfer(BCTransportPipes.quartzPower, rate * 8, 32, false);
      powerTransfer(BCTransportPipes.ironPower, rate * 8, 32, false);
      powerTransfer(BCTransportPipes.goldPower, rate * 32, 32, false);
      powerTransfer(BCTransportPipes.diamondPower, rate * 64, 32, false);
      powerTransfer(BCTransportPipes.diaWoodPower, rate * 64, 32, true);
   }

   private static void powerTransfer(PipeDefinition def, int transferMultiplier, int resistanceDivisor, boolean recv) {
      long transfer = MjAPI.MJ * transferMultiplier;
      long resistance = MjAPI.MJ / resistanceDivisor;
      PipeApi.powerTransferData.put(def, PipeApi.PowerTransferInfo.createFromResistance(transfer, resistance, recv));
   }

   public static void registerRfTransferData() {
      int rate = baseRfRate;
      if (!disableRfPipe) {
         rfTransfer(BCTransportPipes.cobbleRf, rate, false);
         rfTransfer(BCTransportPipes.stoneRf, rate * 2, false);
         rfTransfer(BCTransportPipes.woodRf, rate * 4, true);
         rfTransfer(BCTransportPipes.sandstoneRf, rate * 4, false);
         rfTransfer(BCTransportPipes.quartzRf, rate * 8, false);
         rfTransfer(BCTransportPipes.ironRf, rate * 8, false);
         rfTransfer(BCTransportPipes.goldRf, rate * 32, false);
         rfTransfer(BCTransportPipes.diamondRf, rate * 64, false);
         rfTransfer(BCTransportPipes.diaWoodRf, rate * 64, true);
      }
   }

   private static void rfTransfer(PipeDefinition def, int maxTransfer, boolean recv) {
      PipeApi.rfTransferData.put(def, new PipeApi.RedstoneFluxTransferInfo(maxTransfer, recv));
   }

   public static void registerFluidTransferData() {
      int rate = baseFlowRate;
      fluidTransfer(BCTransportPipes.cobbleFluid, rate, 10);
      fluidTransfer(BCTransportPipes.woodFluid, rate, 10);
      fluidTransfer(BCTransportPipes.stoneFluid, rate * 2, 10);
      fluidTransfer(BCTransportPipes.sandstoneFluid, rate * 2, 10);
      fluidTransfer(BCTransportPipes.clayFluid, rate * 4, 10);
      fluidTransfer(BCTransportPipes.ironFluid, rate * 4, 10);
      fluidTransfer(BCTransportPipes.quartzFluid, rate * 4, 10);
      fluidTransfer(BCTransportPipes.diamondFluid, rate * 8, 10);
      fluidTransfer(BCTransportPipes.diaWoodFluid, rate * 8, 10);
      fluidTransfer(BCTransportPipes.goldFluid, rate * 8, 2);
      fluidTransfer(BCTransportPipes.voidFluid, rate * 8, 10);
   }

   private static void fluidTransfer(PipeDefinition def, int rate, int delay) {
      PipeApi.fluidTransferData.put(def, new PipeApi.FluidTransferInfo(rate, delay));
   }
}
