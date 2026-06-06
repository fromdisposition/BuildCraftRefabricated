package buildcraft.api.transport.pluggable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IPlugDynamicRenderer<P extends PipePluggable> {
   void render(P var1, double var2, double var4, double var6, float var8, VertexConsumer var9, PoseStack var10);
}
