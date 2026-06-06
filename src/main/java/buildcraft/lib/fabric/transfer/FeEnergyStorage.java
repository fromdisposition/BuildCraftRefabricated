package buildcraft.lib.fabric.transfer;

import buildcraft.lib.common.util.ValueIOSerializable;
import buildcraft.lib.transfer.handler.TransferPreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.reborn.energy.api.EnergyStorage;

public class FeEnergyStorage implements EnergyStorage, ValueIOSerializable {
   protected int energy;
   protected final int capacity;
   protected final int maxInsert;
   protected final int maxExtract;
   private final SnapshotParticipant<Integer> energyJournal = new SnapshotParticipant<Integer>() {
      private int snapshotEnergy;

      protected Integer createSnapshot() {
         this.snapshotEnergy = FeEnergyStorage.this.energy;
         return FeEnergyStorage.this.energy;
      }

      protected void readSnapshot(Integer snapshot) {
         FeEnergyStorage.this.energy = snapshot;
      }

      protected void onFinalCommit() {
         if (FeEnergyStorage.this.energy != this.snapshotEnergy) {
            FeEnergyStorage.this.onEnergyChanged(this.snapshotEnergy);
         }
      }
   };

   public FeEnergyStorage(int capacity) {
      this(capacity, capacity);
   }

   public FeEnergyStorage(int capacity, int maxTransfer) {
      this(capacity, maxTransfer, maxTransfer);
   }

   public FeEnergyStorage(int capacity, int maxInsert, int maxExtract) {
      this(capacity, maxInsert, maxExtract, 0);
   }

   public FeEnergyStorage(int capacity, int maxInsert, int maxExtract, int energy) {
      TransferPreconditions.checkNonNegative(capacity);
      TransferPreconditions.checkNonNegative(maxInsert);
      TransferPreconditions.checkNonNegative(maxExtract);
      TransferPreconditions.checkNonNegative(energy);
      this.capacity = capacity;
      this.maxInsert = maxInsert;
      this.maxExtract = maxExtract;
      this.energy = energy;
   }

   @Override
   public void serialize(ValueOutput output) {
      output.putInt("energy", this.energy);
   }

   @Override
   public void deserialize(ValueInput input) {
      this.energy = Math.max(0, input.getIntOr("energy", 0));
   }

   public void set(int amount) {
      TransferPreconditions.checkNonNegative(amount);
      if (this.energy != amount) {
         int previousAmount = this.energy;
         this.energy = amount;
         this.onEnergyChanged(previousAmount);
      }
   }

   protected void onEnergyChanged(int previousAmount) {
   }

   public boolean supportsInsertion() {
      return this.maxInsert > 0;
   }

   public boolean supportsExtraction() {
      return this.maxExtract > 0;
   }

   public long insert(long maxAmount, TransactionContext transaction) {
      if (maxAmount <= 0L) {
         return 0L;
      } else {
         int amount = saturate(maxAmount);
         TransferPreconditions.checkNonNegative(amount);
         int inserted = Math.min(this.capacity - this.energy, Math.min(amount, this.maxInsert));
         if (inserted > 0) {
            this.energyJournal.updateSnapshots(transaction);
            this.energy += inserted;
            return inserted;
         } else {
            return 0L;
         }
      }
   }

   public long extract(long maxAmount, TransactionContext transaction) {
      if (maxAmount <= 0L) {
         return 0L;
      } else {
         int amount = saturate(maxAmount);
         TransferPreconditions.checkNonNegative(amount);
         int extracted = Math.min(this.energy, Math.min(amount, this.maxExtract));
         if (extracted > 0) {
            this.energyJournal.updateSnapshots(transaction);
            this.energy -= extracted;
            return extracted;
         } else {
            return 0L;
         }
      }
   }

   public long getAmount() {
      return this.energy;
   }

   public long getCapacity() {
      return this.capacity;
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }
}
