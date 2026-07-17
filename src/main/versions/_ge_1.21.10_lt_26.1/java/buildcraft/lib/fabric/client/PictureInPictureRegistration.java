package buildcraft.lib.fabric.client;

import buildcraft.builders.client.render.pip.BlueprintPipRenderer;
import buildcraft.builders.client.render.pip.TooltipBlueprintPipRenderer;
import buildcraft.robotics.client.render.pip.ZoneMapPipRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

/**
 * 1.21.x names the PiP registry {@link SpecialGuiElementRegistry}; 26.x renamed it to
 * {@code PictureInPictureRendererRegistry} (see the _ge_26.1 copy). Loaded via reflection from
 * {@link buildcraft.fabric.BuildCraftFabricClient}, which skips nodes without this class -- 1.21.1 has no
 * picture-in-picture system at all.
 */
public final class PictureInPictureRegistration {
   private PictureInPictureRegistration() {
   }

   public static void register() {
      SpecialGuiElementRegistry.register(ctx -> new BlueprintPipRenderer(ctx.vertexConsumers()));
      SpecialGuiElementRegistry.register(ctx -> new TooltipBlueprintPipRenderer(ctx.vertexConsumers()));
      SpecialGuiElementRegistry.register(ctx -> new ZoneMapPipRenderer(ctx.vertexConsumers()));
   }
}
