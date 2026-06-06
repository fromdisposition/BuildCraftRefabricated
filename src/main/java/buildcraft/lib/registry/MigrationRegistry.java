package buildcraft.lib.registry;

public final class MigrationRegistry {
   private static boolean initialized;

   private MigrationRegistry() {
   }

   public static void init() {
      if (!initialized) {
         initialized = true;
         LegacyAliases.init();
      }
   }
}
