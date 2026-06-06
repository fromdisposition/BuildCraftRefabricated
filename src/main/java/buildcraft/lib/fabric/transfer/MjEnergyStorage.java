package buildcraft.lib.fabric.transfer;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

public final class MjEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
   private final MjBattery battery;

   public MjEnergyStorage(MjBattery battery) {
      this.battery = battery;
   }

   public static MjEnergyStorage createIfRfEnabled(MjBattery battery) {
      return MjAPI.isRfAutoConversionEnabled() ? new MjEnergyStorage(battery) : null;
   }

   private static long mjPerRf() {
      return MjAPI.getRfConversion().mjPerRf;
   }

   public boolean supportsInsertion() {
      return mjPerRf() > 0L;
   }

   public long insert(long maxAmount, TransactionContext transaction) {
      if (maxAmount <= 0L) {
         return 0L;
      }

      long mjpr = mjPerRf();
      if (mjpr <= 0L) {
         return 0L;
      }

      long space = this.battery.getCapacity() - this.battery.getStored();
      if (space <= 0L) {
         return 0L;
      }

      long maxRfBySpace = space / mjpr;
      long acceptedRf = Math.min(maxAmount, maxRfBySpace);
      if (acceptedRf <= 0L) {
         return 0L;
      }

      this.updateSnapshots(transaction);
      this.battery.addPower(acceptedRf * mjpr, false);
      return acceptedRf;
   }

   public boolean supportsExtraction() {
      return mjPerRf() > 0L;
   }

   public long extract(long maxAmount, TransactionContext transaction) {
      if (maxAmount <= 0L) {
         return 0L;
      }

      long mjpr = mjPerRf();
      if (mjpr <= 0L) {
         return 0L;
      }

      long maxRfByStored = this.battery.getStored() / mjpr;
      long extractRf = Math.min(maxAmount, maxRfByStored);
      if (extractRf <= 0L) {
         return 0L;
      }

      this.updateSnapshots(transaction);
      this.battery.extractPower(0L, extractRf * mjpr);
      return extractRf;
   }

   public long getAmount() {
      long mjpr = mjPerRf();
      return mjpr <= 0L ? 0L : this.battery.getStored() / mjpr;
   }

   public long getCapacity() {
      long mjpr = mjPerRf();
      return mjpr <= 0L ? 0L : this.battery.getCapacity() / mjpr;
   }

   protected Long createSnapshot() {
      return this.battery.getStored();
   }

   protected void readSnapshot(Long snapshot) {
      this.battery.setStored(snapshot);
   }
}
