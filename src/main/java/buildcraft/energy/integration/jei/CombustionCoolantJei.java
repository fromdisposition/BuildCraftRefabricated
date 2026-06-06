package buildcraft.energy.integration.jei;

import buildcraft.lib.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;

public record CombustionCoolantJei(ItemStack item, FluidStack fluid, float coolingPerMb) {
   public boolean isSolid() {
      return !this.item.isEmpty();
   }
}
