package buildcraft.lib.fabric.client;

import buildcraft.builders.client.render.pip.BlueprintPipRenderer;
import buildcraft.builders.client.render.pip.TooltipBlueprintPipRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

// 1.21.x names the PiP registry {@link SpecialGuiElementRegistry}; 26.x renamed it to PictureInPictureRendererRegistry
// (see the _ge_26.1 copy). Loaded via reflection from {@link buildcraft.fabric.BuildCraftFabricClient}.
public final class PictureInPictureRegistration {
   private PictureInPictureRegistration() {
   }

   public static void register() {
      SpecialGuiElementRegistry.register(ctx -> new BlueprintPipRenderer(ctx.vertexConsumers()));
      SpecialGuiElementRegistry.register(ctx -> new TooltipBlueprintPipRenderer(ctx.vertexConsumers()));
   }
}
