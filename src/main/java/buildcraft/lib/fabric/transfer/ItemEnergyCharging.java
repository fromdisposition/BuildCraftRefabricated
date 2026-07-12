package buildcraft.lib.fabric.transfer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;

/**
 * Charging of items that expose the Team Reborn energy API. That API is an optional dependency, so this class must not
 * name any of its types: every direct reference lives in {@link TrItemEnergyCharging}, which is only touched from
 * behind the availability guard. Without the guard the JVM resolves {@code team.reborn.energy.api.EnergyStorage} on the
 * first call and throws NoClassDefFoundError (e.g. dragging an item over a Charging Table slot).
 */
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
