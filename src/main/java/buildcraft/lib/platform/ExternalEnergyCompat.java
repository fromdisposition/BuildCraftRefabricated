package buildcraft.lib.platform;

import buildcraft.lib.BCLibConfig;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Detects whether the modpack ships a Team Reborn energy consumer (e.g. Tech Reborn).
 * BuildCraft bundles {@code team_reborn_energy} itself; this only tracks other gameplay mods.
 */
public final class ExternalEnergyCompat {
   private ExternalEnergyCompat() {
   }

   public static void init() {
      BCLibConfig.externalEnergyEcosystemPresent.set(detectEcosystem());
   }

   public static boolean isEcosystemPresent() {
      return BCLibConfig.externalEnergyEcosystemPresent.get();
   }

   private static boolean detectEcosystem() {
      try {
         FabricLoader loader = FabricLoader.getInstance();
         return loader.isModLoaded("techreborn")
            || loader.isModLoaded("industrialrevolution")
            || loader.isModLoaded("modern_industrialization");
      } catch (Throwable ignored) {
         return false;
      }
   }
}
