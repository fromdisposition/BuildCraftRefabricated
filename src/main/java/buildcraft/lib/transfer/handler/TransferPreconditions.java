package buildcraft.lib.transfer.handler;

import buildcraft.lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;

public class TransferPreconditions {
   private TransferPreconditions() {
   }

   public static void checkNonEmpty(FluidStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("Expected fluid stack to be non-empty: " + stack);
      }
   }

   public static void checkNonEmpty(ItemStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalArgumentException("Expected item stack to be non-empty: " + stack);
      }
   }

   public static void checkNonEmpty(FluidVariant variant) {
      if (variant.isBlank()) {
         throw new IllegalArgumentException("Expected fluid variant to be non-empty: " + variant);
      }
   }

   public static void checkNonEmpty(ItemVariant variant) {
      if (variant.isBlank()) {
         throw new IllegalArgumentException("Expected item variant to be non-empty: " + variant);
      }
   }

   public static void checkNonNegative(int value) {
      if (value < 0) {
         throw new IllegalArgumentException("Expected value to be non-negative: " + value);
      }
   }

   public static void checkNonEmptyNonNegative(FluidStack stack, int value) {
      checkNonEmpty(stack);
      checkNonNegative(value);
   }

   public static void checkNonEmptyNonNegative(ItemStack stack, int value) {
      checkNonEmpty(stack);
      checkNonNegative(value);
   }

   public static void checkNonEmptyNonNegative(ItemVariant variant, int value) {
      checkNonEmpty(variant);
      checkNonNegative(value);
   }
}
