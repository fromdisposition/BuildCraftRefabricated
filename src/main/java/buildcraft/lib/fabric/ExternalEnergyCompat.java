package buildcraft.lib.fabric;

import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * Detects whether the loaded modpack exposes Team Reborn {@code E} as a player-facing
 * energy unit (Tech Reborn and other mods that depend on {@code team_reborn_energy}).
 * BuildCraft always bundles the API library; this answers whether another gameplay mod uses it.
 */
public final class ExternalEnergyCompat {
   private static final Set<String> IGNORED_MOD_IDS = Set.of(
      "buildcraftrefabricated",
      "team_reborn_energy",
      "fabric",
      "fabricloader",
      "fabric-api",
      "minecraft",
      "java"
   );

   private static Boolean ecosystemPresent;

   private ExternalEnergyCompat() {
   }

   public static boolean isEcosystemPresent() {
      if (ecosystemPresent == null) {
         ecosystemPresent = detectEcosystem();
      }

      return ecosystemPresent;
   }

   static void resetForTests() {
      ecosystemPresent = null;
   }

   private static boolean detectEcosystem() {
      FabricLoader loader = FabricLoader.getInstance();

      for (ModContainer container : loader.getAllMods()) {
         ModMetadata metadata = container.getMetadata();
         String id = metadata.getId();
         if (IGNORED_MOD_IDS.contains(id)) {
            continue;
         }

         if (dependsOnExternalEnergy(metadata)) {
            return true;
         }
      }

      return loader.isModLoaded("techreborn");
   }

   private static boolean dependsOnExternalEnergy(ModMetadata metadata) {
      for (ModDependency dependency : metadata.getDepends()) {
         String modId = dependency.getModId();
         if ("team_reborn_energy".equals(modId) || "techreborn".equals(modId)) {
            return true;
         }
      }

      return false;
   }
}
