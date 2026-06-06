package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public class RenderEngine_BC8 extends BcBlockEntityRenderer<TileEngineBase_BC8, EngineRenderState> {
   private final BiFunction<TileEngineBase_BC8, Float, MutableQuad[]> quadProvider;

   public RenderEngine_BC8(BiFunction<TileEngineBase_BC8, Float, MutableQuad[]> quadProvider) {
      this.quadProvider = quadProvider;
   }

   public EngineRenderState createRenderState() {
      return new EngineRenderState();
   }

   public void submit(EngineRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      ProfilerFiller _profiler = Profiler.get();
      _profiler.push("buildcraft:engine_submit");

      try {
         TileEngineBase_BC8 engine = state.tile;
         if (engine == null) {
            return;
         }

         float partialTicks = state.partialTick;
         _profiler.push("buildcraft:engine_model_refresh");

         MutableQuad[] quads;
         try {
            quads = this.quadProvider.apply(engine, partialTicks);
         } finally {
            _profiler.pop();
         }

         if (quads != null && quads.length != 0) {
            poseStack.pushPose();
            int light = state.light;
            BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());

            for (MutableQuad quad : quads) {
               quad.setCalculatedDiffuse();
               quad.lighti(light);
               quad.render(poseStack.last(), buffer);
            }

            poseStack.popPose();
            return;
         }
      } finally {
         _profiler.pop();
      }
   }
}
