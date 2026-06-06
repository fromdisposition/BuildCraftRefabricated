package buildcraft.lib.fabric.client;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class TooltipHoverContext {
   private static final ThreadLocal<ItemStack> HOVERED = new ThreadLocal<>();

   private TooltipHoverContext() {
   }

   public static void set(@Nullable ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         HOVERED.set(stack);
      } else {
         HOVERED.remove();
      }
   }

   public static @Nullable ItemStack get() {
      return HOVERED.get();
   }

   public static void clear() {
      HOVERED.remove();
   }
}
