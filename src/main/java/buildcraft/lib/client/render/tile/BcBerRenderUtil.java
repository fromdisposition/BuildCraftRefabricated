package buildcraft.lib.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class BcBerRenderUtil {
   private BcBerRenderUtil() {
   }

   public static void submit(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, BiConsumer<Pose, VertexConsumer> draw) {
      collector.submitCustomGeometry(poseStack, renderType, draw::accept);
   }

   public static void submitWithPoseStack(
      PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, BiConsumer<PoseStack, VertexConsumer> draw
   ) {
      collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> draw.accept(copyPoseStack(pose), buffer));
   }

   public static PoseStack copyPoseStack(Pose pose) {
      PoseStack stack = new PoseStack();
      stack.pushPose();
      stack.last().set(pose);
      return stack;
   }
}
