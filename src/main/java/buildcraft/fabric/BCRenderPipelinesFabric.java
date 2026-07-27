package buildcraft.fabric;

import buildcraft.lib.client.render.BCLibRenderTypes;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.RenderPipelines;
//?}

public final class BCRenderPipelinesFabric {
   private static boolean registered;

   private BCRenderPipelinesFabric() {
   }

   public static void register() {
      if (!registered) {
         //? if >= 1.21.10 {
         RenderPipelines.register(BCLibRenderTypes.LED_PIPELINE);
         //?}
         registered = true;
      }
   }
}
