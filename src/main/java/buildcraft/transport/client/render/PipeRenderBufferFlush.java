package buildcraft.transport.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class PipeRenderBufferFlush {
   private static final Map<RenderType, Boolean> USED_LAYERS = new IdentityHashMap<>();

   private PipeRenderBufferFlush() {
   }

   public static BufferSource bufferSource() {
      return Minecraft.getInstance().renderBuffers().bufferSource();
   }

   public static VertexConsumer getBuffer(RenderType type) {
      USED_LAYERS.put(type, Boolean.TRUE);
      return bufferSource().getBuffer(type);
   }

   public static void flushFrame() {
      if (!USED_LAYERS.isEmpty()) {
         BufferSource source = bufferSource();

         for (RenderType type : USED_LAYERS.keySet()) {
            source.endBatch(type);
         }

         USED_LAYERS.clear();
      }
   }
}
