package buildcraft.fabric.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
//?}
//? if < 1.21.10 {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}

/**
 * Version-neutral "submit custom geometry during the level render" event. On 1.21.5+ it wraps the deferred
 * SubmitNodeCollector; on 1.21.1 there is no submit pipeline, so it wraps the immediate MultiBufferSource and
 * {@link #submitCustomGeometry} draws straight away. Consumers use only {@link #getPoseStack()},
 * {@link #getCameraPos()} and {@link #submitCustomGeometry} so their code is identical on every node.
 */
public final class SubmitCustomGeometryEvent {
   private final PoseStack poseStack;
   //? if >= 1.21.10 {
   private final LevelRenderState levelRenderState;
   private final SubmitNodeCollector submitNodeCollector;

   public SubmitCustomGeometryEvent(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
      this.poseStack = poseStack;
      this.levelRenderState = levelRenderState;
      this.submitNodeCollector = submitNodeCollector;
   }

   public Vec3 getCameraPos() {
      return this.levelRenderState.cameraRenderState.pos;
   }

   public void submitCustomGeometry(PoseStack pose, RenderType renderType, BiConsumer<PoseStack.Pose, VertexConsumer> callback) {
      this.submitNodeCollector.submitCustomGeometry(pose, renderType, (p, vc) -> callback.accept(p, vc));
   }

   /** Raw collector for batch item rendering (ItemRenderUtil) on the deferred pipeline. */
   public SubmitNodeCollector getSubmitNodeCollector() {
      return this.submitNodeCollector;
   }
   //?}
   //? if < 1.21.10 {
   /*private final MultiBufferSource bufferSource;
   private final Vec3 cameraPos;

   public SubmitCustomGeometryEvent(PoseStack poseStack, Vec3 cameraPos, MultiBufferSource bufferSource) {
      this.poseStack = poseStack;
      this.cameraPos = cameraPos;
      this.bufferSource = bufferSource;
   }

   public Vec3 getCameraPos() {
      return this.cameraPos;
   }

   public void submitCustomGeometry(PoseStack pose, RenderType renderType, BiConsumer<PoseStack.Pose, VertexConsumer> callback) {
      callback.accept(pose.last(), this.bufferSource.getBuffer(renderType));
   }

   public MultiBufferSource getBufferSource() {
      return this.bufferSource;
   }
   *///?}

   public PoseStack getPoseStack() {
      return this.poseStack;
   }
}
