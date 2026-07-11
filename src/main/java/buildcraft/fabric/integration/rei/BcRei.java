package buildcraft.fabric.integration.rei;

import buildcraft.lib.misc.LocaleUtil;
import dev.architectury.fluid.FluidStack;
import java.util.List;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** Small REI glue: fluid amounts are millibuckets on the BC side, droplets (x81) on the Fabric side. */
final class BcRei {
   static final int MB_TO_DROPLETS = 81;

   private BcRei() {
   }

   static EntryIngredient fluid(Fluid fluid, int milliBuckets) {
      return EntryIngredient.of(EntryStacks.of(FluidStack.create(fluid, (long)milliBuckets * MB_TO_DROPLETS)));
   }

   static EntryIngredient fluid(buildcraft.lib.fluid.stack.FluidStack stack) {
      return fluid(stack.getFluid(), stack.getAmount());
   }

   static EntryIngredient item(ItemStack stack) {
      return stack.isEmpty() ? EntryIngredient.empty() : EntryIngredients.of(stack);
   }

   static EntryIngredient itemAlternatives(List<ItemStack> stacks) {
      return stacks.isEmpty() ? EntryIngredient.empty() : EntryIngredients.ofItemStacks(stacks);
   }

   static Component mjText(String key, long microMj) {
      return Component.literal(LocaleUtil.localize(key, LocaleUtil.localizeMj(microMj)));
   }
}
