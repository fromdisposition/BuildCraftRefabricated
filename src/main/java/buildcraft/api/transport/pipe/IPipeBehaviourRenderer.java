package buildcraft.api.transport.pipe;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public interface IPipeBehaviourRenderer<B extends PipeBehaviour> {
   void render(B var1, double var2, double var4, double var6, float var8, VertexConsumer var9, Pose var10);
}
