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
      Transaction tx = Transaction.openOuter();

      long accepted;
      try {
         accepted = this.fe.insert(Long.MAX_VALUE, tx);
      } catch (Throwable var7) {
         if (tx != null) {
            try {
               tx.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (tx != null) {
         tx.close();
      }

      return accepted <= 0L ? 0L : accepted * MjAPI.getRfConversion().mjPerRf;
   }

   long implReceivePower(long microJoules, boolean simulate) {
      long mjPerRf = MjAPI.getRfConversion().mjPerRf;
      long maxRf = microJoules / mjPerRf;
      if (maxRf <= 0L) {
         return microJoules;
      }

      if (simulate) {
         Transaction tx = Transaction.openOuter();

         long var19;
         try {
            long received = this.fe.insert(maxRf, tx);
            var19 = microJoules - received * mjPerRf;
         } catch (Throwable var16) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var14) {
                  var16.addSuppressed(var14);
               }
            }

            throw var16;
         }

         if (tx != null) {
            tx.close();
         }

         return var19;
      } else {
         Transaction tx = Transaction.openOuter();

         long var11;
         try {
            long received = this.fe.insert(maxRf, tx);
            tx.commit();
            var11 = microJoules - received * mjPerRf;
         } catch (Throwable var15) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var13) {
                  var15.addSuppressed(var13);
               }
            }

            throw var15;
         }

         if (tx != null) {
            tx.close();
         }

         return var11;
      }
   }

   long implExtractPower(long min, long max, boolean simulate) {
      long mjPerRf = MjAPI.getRfConversion().mjPerRf;
      long maxRf = max / mjPerRf;
      if (maxRf <= 0L) {
         return 0L;
      }

      Transaction simTx = Transaction.openOuter();

      long extractedMJ;
      try {
         long extractedRF = this.fe.extract(maxRf, simTx);
         extractedMJ = extractedRF * mjPerRf;
      } catch (Throwable var20) {
         if (simTx != null) {
            try {
               simTx.close();
            } catch (Throwable var18) {
               var20.addSuppressed(var18);
            }
         }

         throw var20;
      }

      if (simTx != null) {
         simTx.close();
      }

      if (extractedMJ < min) {
         return 0L;
      }

      if (!simulate) {
         simTx = Transaction.openOuter();

         long var15;
         try {
            long extractedRF = this.fe.extract(maxRf, simTx);
            simTx.commit();
            var15 = extractedRF * mjPerRf;
         } catch (Throwable var19) {
            if (simTx != null) {
               try {
                  simTx.close();
               } catch (Throwable var17) {
                  var19.addSuppressed(var17);
               }
            }

            throw var19;
         }

         if (simTx != null) {
            simTx.close();
         }

         return var15;
      } else {
         return extractedMJ;
      }
   }
}
