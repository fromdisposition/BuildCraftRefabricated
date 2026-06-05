package buildcraft.lib.fabric.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import buildcraft.builders.client.render.pip.BlueprintPipRenderer;

@Mixin(GameRenderer.class)
public class GameRendererPipMixin {
    @ModifyArg(
            method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/ItemInHandRenderer;Lnet/minecraft/client/renderer/RenderBuffers;Lnet/minecraft/client/resources/model/ModelManager;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/renderer/state/gui/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"),
            index = 4)
    private List<PictureInPictureRenderer<?>> buildcraft$appendBlueprintPipRenderer(
            List<PictureInPictureRenderer<?>> vanilla) {
        List<PictureInPictureRenderer<?>> renderers = new ArrayList<>(vanilla);
        MultiBufferSource.BufferSource buffers =
                Minecraft.getInstance().renderBuffers().bufferSource();
        renderers.add(new BlueprintPipRenderer(buffers));
        return List.copyOf(renderers);
    }
}
