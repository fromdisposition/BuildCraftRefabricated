package buildcraft.fabric;

import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.client.renderer.RenderPipelines;

public final class BCRenderPipelinesFabric {
   private static boolean registered;

   private BCRenderPipelinesFabric() {
   }

   public static void register() {
      if (!registered) {
         RenderPipelines.register(BCLibRenderTypes.LED_PIPELINE);
         registered = true;
      }
   }
}
