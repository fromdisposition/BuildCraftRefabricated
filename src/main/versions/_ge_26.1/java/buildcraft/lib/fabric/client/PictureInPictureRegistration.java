package buildcraft.lib.fabric.client;

import buildcraft.builders.client.render.pip.BlueprintPipRenderer;
import buildcraft.builders.client.render.pip.TooltipBlueprintPipRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;

// Uses Fabric's {@link PictureInPictureRendererRegistry} rather than mixin-appending to GameRenderer's renderer list:
// under NeoForge/Sinytra Connector that vanilla list holds registration objects, not renderers, so injected ones ClassCastException.
public final class PictureInPictureRegistration {
   private PictureInPictureRegistration() {
   }

   public static void register() {
      //? if >= 26.2 {
      PictureInPictureRendererRegistry.register(ctx -> new BlueprintPipRenderer());
      PictureInPictureRendererRegistry.register(ctx -> new TooltipBlueprintPipRenderer());
      //?} else {
      /*PictureInPictureRendererRegistry.register(ctx -> new BlueprintPipRenderer(ctx.bufferSource()));
      PictureInPictureRendererRegistry.register(ctx -> new TooltipBlueprintPipRenderer(ctx.bufferSource()));
      *///?}
   }
}
