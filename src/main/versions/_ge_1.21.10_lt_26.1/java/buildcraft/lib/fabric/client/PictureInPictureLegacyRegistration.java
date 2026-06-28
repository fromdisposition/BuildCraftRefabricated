package buildcraft.lib.fabric.client;

import buildcraft.builders.client.render.pip.BlueprintPipRenderer;
import buildcraft.builders.client.render.pip.TooltipBlueprintPipRenderer;
import buildcraft.robotics.client.render.pip.ZoneMapPipRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;

/**
 * 1.21.x registers PiP renderers through Fabric's {@link SpecialGuiElementRegistry}; 26.x hooks
 * {@code GameRenderer} directly via mixin. Loaded only on 1.21.x builds via reflection from
 * {@link buildcraft.fabric.BuildCraftFabricClient}.
 */
public final class PictureInPictureLegacyRegistration {
   private PictureInPictureLegacyRegistration() {
   }

   public static void register() {
      SpecialGuiElementRegistry.register(ctx -> new BlueprintPipRenderer(ctx.vertexConsumers()));
      SpecialGuiElementRegistry.register(ctx -> new TooltipBlueprintPipRenderer(ctx.vertexConsumers()));
      SpecialGuiElementRegistry.register(ctx -> new ZoneMapPipRenderer(ctx.vertexConsumers()));
   }
}
