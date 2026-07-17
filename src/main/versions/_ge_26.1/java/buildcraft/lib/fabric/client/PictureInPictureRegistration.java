package buildcraft.lib.fabric.client;

import buildcraft.builders.client.render.pip.BlueprintPipRenderer;
import buildcraft.builders.client.render.pip.TooltipBlueprintPipRenderer;
import buildcraft.robotics.client.render.pip.ZoneMapPipRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;

/**
 * 26.x registers PiP renderers through Fabric's {@link PictureInPictureRendererRegistry} -- the same registry
 * 1.21.x exposes under its older name (see the _ge_1.21.10_lt_26.1 copy). Going through the API rather than
 * appending to GameRenderer's renderer list by mixin is what keeps this working wherever the API is reimplemented:
 * on NeoForge (Forgified Fabric API / Sinytra Connector) that vanilla list holds NeoForge registration objects,
 * not renderer instances, so hand-injected renderers there died with a ClassCastException.
 * Loaded via reflection from {@link buildcraft.fabric.BuildCraftFabricClient}.
 */
public final class PictureInPictureRegistration {
   private PictureInPictureRegistration() {
   }

   public static void register() {
      //? if >= 26.2 {
      PictureInPictureRendererRegistry.register(ctx -> new BlueprintPipRenderer());
      PictureInPictureRendererRegistry.register(ctx -> new TooltipBlueprintPipRenderer());
      PictureInPictureRendererRegistry.register(ctx -> new ZoneMapPipRenderer());
      //?} else {
      /*PictureInPictureRendererRegistry.register(ctx -> new BlueprintPipRenderer(ctx.bufferSource()));
      PictureInPictureRendererRegistry.register(ctx -> new TooltipBlueprintPipRenderer(ctx.bufferSource()));
      PictureInPictureRendererRegistry.register(ctx -> new ZoneMapPipRenderer(ctx.bufferSource()));
      *///?}
   }
}
