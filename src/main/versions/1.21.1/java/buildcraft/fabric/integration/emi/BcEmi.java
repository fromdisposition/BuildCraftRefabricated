package buildcraft.fabric.integration.emi;

import buildcraft.lib.fluid.stack.FluidStack;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** Shared helpers for the EMI integration (1.21.1 only — EMI does not exist for the 26.x line). */
final class BcEmi {
   /** EMI works in droplets: 81000 per bucket, i.e. 81 per mB. */
   private static final long DROPLETS_PER_MB = 81;

   static final int TEXT_COLOUR = 0xFF3F3F3F;

   private BcEmi() {
   }

   /** Category with the icon of its workstation, titled by the existing JEI lang key. */
   static EmiRecipeCategory category(String path, ItemLike icon, String titleKey) {
      return new EmiRecipeCategory(Identifier.parse("buildcraftrefabricated:" + path), EmiStack.of(new ItemStack(icon))) {
         @Override
         public Component getName() {
            return Component.translatable(titleKey);
         }
      };
   }

   static EmiStack fluid(FluidStack stack) {
      if (stack == null || stack.isEmpty()) {
         return EmiStack.EMPTY;
      }

      return EmiStack.of(stack.getFluid(), stack.getAmount() * DROPLETS_PER_MB);
   }
}
