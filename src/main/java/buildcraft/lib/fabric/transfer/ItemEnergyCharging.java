package buildcraft.lib.fabric.transfer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;

// Must never name a Team Reborn energy type directly; every such reference lives behind {@link TrItemEnergyCharging}'s
// availability guard, or an absent dependency throws NoClassDefFoundError on first resolution.
public final class ItemEnergyCharging {
   private static final boolean AVAILABLE = FabricLoader.getInstance().isModLoaded("team_reborn_energy");

   private ItemEnergyCharging() {
   }

   public static boolean canCharge(ItemStack stack) {
      return AVAILABLE && TrItemEnergyCharging.canCharge(stack);
   }

   public static long getRequiredMj(ItemStack stack) {
      return AVAILABLE ? TrItemEnergyCharging.getRequiredMj(stack) : 0L;
   }

   public static long chargeMj(ItemStack stack, long microJoules) {
      return AVAILABLE ? TrItemEnergyCharging.chargeMj(stack, microJoules) : 0L;
   }
}
