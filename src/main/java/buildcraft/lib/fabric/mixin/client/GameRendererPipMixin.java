package buildcraft.lib.fabric.mixin.client;

import buildcraft.builders.client.render.pip.BlueprintPipRenderer;
import buildcraft.builders.client.render.pip.TooltipBlueprintPipRenderer;
import buildcraft.robotics.client.render.pip.ZoneMapPipRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
//? if >= 26.2 {
//?} else if >= 26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.resources.model.ModelManager;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererPipMixin {
   //? if >= 26.2 {
   @ModifyArg(
      method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/ItemInHandRenderer;Lnet/minecraft/client/resources/model/ModelManager;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"
      ),
      index = 2,
      require = 0
   )
   private List<PictureInPictureRenderer<?>> buildcraft$appendBlueprintPipRenderer(List<PictureInPictureRenderer<?>> vanilla) {
      List<PictureInPictureRenderer<?>> renderers = new ArrayList<>(vanilla);
      renderers.add(new BlueprintPipRenderer());
      renderers.add(new TooltipBlueprintPipRenderer());
      renderers.add(new ZoneMapPipRenderer());
      return List.copyOf(renderers);
   }
   //?} else if >= 26.1 {
   /*// Stash RenderBuffers from the constructor args so @ModifyArg below can use it without
   // calling Minecraft.getInstance().renderBuffers(), which is null mid-construction.
   @Unique
   private RenderBuffers buildcraft$initRenderBuffers;

   @Inject(
      method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/ItemInHandRenderer;Lnet/minecraft/client/renderer/RenderBuffers;Lnet/minecraft/client/resources/model/ModelManager;)V",
      at = @At(value = "INVOKE", target = "Ljava/lang/Object;<init>()V", shift = At.Shift.AFTER),
      require = 0
   )
   private void buildcraft$captureRenderBuffers(Minecraft mc, ItemInHandRenderer ihr, RenderBuffers rb, ModelManager mm, CallbackInfo ci) {
      this.buildcraft$initRenderBuffers = rb;
   }

   @ModifyArg(
      method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/ItemInHandRenderer;Lnet/minecraft/client/renderer/RenderBuffers;Lnet/minecraft/client/resources/model/ModelManager;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"
      ),
      index = 4,
      require = 0
   )
   private List<PictureInPictureRenderer<?>> buildcraft$appendBlueprintPipRenderer(List<PictureInPictureRenderer<?>> vanilla) {
      BufferSource buffers = this.buildcraft$initRenderBuffers.bufferSource();
      List<PictureInPictureRenderer<?>> renderers = new ArrayList<>(vanilla);
      renderers.add(new BlueprintPipRenderer(buffers));
      renderers.add(new TooltipBlueprintPipRenderer(buffers));
      renderers.add(new ZoneMapPipRenderer(buffers));
      return List.copyOf(renderers);
   }
   *///?}
}
