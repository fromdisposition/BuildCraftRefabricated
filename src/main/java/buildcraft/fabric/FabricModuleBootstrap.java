package buildcraft.fabric;

public final class FabricModuleBootstrap {
   private FabricModuleBootstrap() {
   }

   public static void registerContent(Runnable blocks, Runnable items, Runnable blockEntities, Runnable menuTypes) {
      blocks.run();
      items.run();
      blockEntities.run();
      menuTypes.run();
   }

   public static void registerCapabilities(Runnable mjCapabilities, Runnable nativeTransfer) {
      mjCapabilities.run();
      nativeTransfer.run();
   }

   public static void registerStandardModule(Runnable blocks, Runnable items, Runnable blockEntities, Runnable menuTypes, Runnable mjCapabilities, Runnable nativeTransfer) {
      registerContent(blocks, items, blockEntities, menuTypes);
      registerCapabilities(mjCapabilities, nativeTransfer);
   }
}
