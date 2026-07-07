package buildcraft.lib.fabric.transfer;

import buildcraft.api.mj.MjAPI;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

public final class ItemEnergyCharging {
   private ItemEnergyCharging() {
   }

   public static boolean canCharge(ItemStack stack) {
      return getStorage(stack) != null;
   }

   public static long getRequiredMj(ItemStack stack) {
      EnergyStorage storage = getStorage(stack);
      if (storage == null) {
         return 0L;
      }

      long mjPerRf = MjAPI.getRfConversion().mjPerRf;
      if (mjPerRf <= 0L) {
         return 0L;
      }

      long spareRf = storage.getCapacity() - storage.getAmount();
      return spareRf * mjPerRf;
   }

   public static long chargeMj(ItemStack stack, long microJoules) {
      EnergyStorage storage = getStorage(stack);
      if (storage == null || microJoules <= 0L) {
         return 0L;
      }

      long mjPerRf = MjAPI.getRfConversion().mjPerRf;
      if (mjPerRf <= 0L) {
         return 0L;
      }

      long maxRf = microJoules / mjPerRf;
      if (maxRf <= 0L) {
         return 0L;
      }

      try (Transaction transaction = Transaction.openOuter()) {
         long insertedRf = storage.insert(maxRf, transaction);
         if (insertedRf <= 0L) {
            return 0L;
         }

         transaction.commit();
         return insertedRf * mjPerRf;
      }
   }

   private static EnergyStorage getStorage(ItemStack stack) {
      if (stack.isEmpty()) {
         return null;
      }

      StackSlot slot = new StackSlot(stack);
      ContainerItemContext context = ContainerItemContext.ofSingleSlot(slot);
      EnergyStorage storage = context.find(EnergyStorage.ITEM);
      if (storage == null || !storage.supportsInsertion()) {
         return null;
      }

      return storage;
   }

   private static final class StackSlot extends SingleVariantStorage<ItemVariant> {
      private final ItemStack stack;

      private StackSlot(ItemStack stack) {
         this.stack = stack;
         this.variant = ItemVariant.of(stack);
         this.amount = stack.getCount();
      }

      @Override
      protected ItemVariant getBlankVariant() {
         return ItemVariant.blank();
      }

      @Override
      protected long getCapacity(ItemVariant variant) {
         return variant.isBlank() ? 64L : variant.toStack().getMaxStackSize();
      }

      @Override
      protected void onFinalCommit() {
         if (!this.variant.isBlank()) {
            ItemStack updated = this.variant.toStack();
            updated.setCount((int)this.amount);
            this.stack.applyComponents(updated.getComponents());
         }
      }
   }
}
