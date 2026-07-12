package buildcraft.fabric;

public final class BCClientRegistriesFabric {
   private BCClientRegistriesFabric() {
   }

   public static void register() {
      BCRenderPipelinesFabric.register();
      BCItemTintSourcesFabric.register();
   }
}
