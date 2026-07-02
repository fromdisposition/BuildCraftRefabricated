/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.mj;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.EnergyStorage;

public class MjToRfAutoConvertor implements IMjReadable {
   final EnergyStorage fe;

   public static MjToRfAutoConvertor create(EnergyStorage fe) {
      if (fe == null) {
         return null;
      } else {
         return !MjAPI.isRfAutoConversionEnabled() ? null : new OfBoth(fe);
      }
   }

   public static IMjReceiver createReceiver(EnergyStorage storage) {
      return create(storage) instanceof IMjReceiver receiver ? receiver : null;
   }

   public static IMjPassiveProvider createProvider(EnergyStorage storage) {
      return create(storage) instanceof IMjPassiveProvider provider ? provider : null;
   }

   MjToRfAutoConvertor(EnergyStorage storage) {
      this.fe = storage;
   }

   @Override
   public boolean canConnect(IMjConnector other) {
      return true;
   }

   @Override
   public long getStored() {
      return this.fe.getAmount() * MjAPI.getRfConversion().mjPerRf;
   }

   @Override
   public long getCapacity() {
      return this.fe.getCapacity() * MjAPI.getRfConversion().mjPerRf;
   }

   long implGetPowerRequested() {
      try (Transaction tx = Transaction.openOuter()) {
         long accepted = this.fe.insert(Long.MAX_VALUE, tx);
         return accepted <= 0L ? 0L : accepted * MjAPI.getRfConversion().mjPerRf;
      }
   }

   long implReceivePower(long microJoules, boolean simulate) {
      long mjPerRf = MjAPI.getRfConversion().mjPerRf;
      long maxRf = microJoules / mjPerRf;
      if (maxRf <= 0L) {
         return microJoules;
      }

      if (simulate) {
         try (Transaction tx = Transaction.openOuter()) {
            long received = this.fe.insert(maxRf, tx);
            return microJoules - received * mjPerRf;
         }
      } else {
         try (Transaction tx = Transaction.openOuter()) {
            long received = this.fe.insert(maxRf, tx);
            tx.commit();
            return microJoules - received * mjPerRf;
         }
      }
   }

   long implExtractPower(long min, long max, boolean simulate) {
      long mjPerRf = MjAPI.getRfConversion().mjPerRf;
      long maxRf = max / mjPerRf;
      if (maxRf <= 0L) {
         return 0L;
      }

      long extractedMJ;
      try (Transaction simTx = Transaction.openOuter()) {
         long extractedRF = this.fe.extract(maxRf, simTx);
         extractedMJ = extractedRF * mjPerRf;
      }

      if (extractedMJ < min) {
         return 0L;
      }

      if (!simulate) {
         try (Transaction tx = Transaction.openOuter()) {
            long extractedRF = this.fe.extract(maxRf, tx);
            tx.commit();
            return extractedRF * mjPerRf;
         }
      } else {
         return extractedMJ;
      }
   }
}
