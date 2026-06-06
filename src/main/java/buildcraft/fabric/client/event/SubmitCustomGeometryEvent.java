package buildcraft.fabric.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;

public final class SubmitCustomGeometryEvent {
   private final PoseStack poseStack;
   private final LevelRenderState levelRenderState;
   private final SubmitNodeCollector submitNodeCollector;

   public SubmitCustomGeometryEvent(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector) {
      this.poseStack = poseStack;
      this.levelRenderState = levelRenderState;
      this.submitNodeCollector = submitNodeCollector;
   }

   public PoseStack getPoseStack() {
      return this.poseStack;
   }

   public LevelRenderState getLevelRenderState() {
      return this.levelRenderState;
   }

   public SubmitNodeCollector getSubmitNodeCollector() {
      return this.submitNodeCollector;
   }
}
