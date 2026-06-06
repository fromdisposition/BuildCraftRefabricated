package buildcraft.transport.pipe.flow;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

public final class PipeEnergySectionStorage implements EnergyStorage {
   private final PipeFlowRedstoneFlux.Section section;

   public PipeEnergySectionStorage(PipeFlowRedstoneFlux.Section section) {
      this.section = section;
   }

   public long insert(long maxAmount, TransactionContext transaction) {
      return maxAmount <= 0L ? 0L : this.section.insert(saturate(maxAmount), transaction);
   }

   public long extract(long maxAmount, TransactionContext transaction) {
      return maxAmount <= 0L ? 0L : this.section.extract(saturate(maxAmount), transaction);
   }

   public long getAmount() {
      return this.section.getAmountAsLong();
   }

   public long getCapacity() {
      return this.section.getCapacityAsLong();
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }
}
