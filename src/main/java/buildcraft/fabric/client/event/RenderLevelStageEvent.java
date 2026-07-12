package buildcraft.fabric.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.state.level.LevelRenderState;
//?}

/**
 * Version-neutral "after translucent blocks" level-render stage. On 1.21.5+ it carries the LevelRenderState
 * (camera comes from cameraRenderState); on 1.21.1 there is no LevelRenderState, so the camera position is
 * passed directly. Consumers use only {@link AfterTranslucentBlocks#getPoseStack()} and
 * {@link AfterTranslucentBlocks#getCameraPos()}.
 */
public class RenderLevelStageEvent {
   public static final class AfterTranslucentBlocks {
      private final PoseStack poseStack;
      //? if >= 1.21.10 {
      private final LevelRenderState levelRenderState;

      public AfterTranslucentBlocks(PoseStack poseStack, LevelRenderState levelRenderState) {
         this.poseStack = poseStack;
         this.levelRenderState = levelRenderState;
      }

      public Vec3 getCameraPos() {
         return this.levelRenderState.cameraRenderState.pos;
      }
      //?}
      //? if < 1.21.10 {
      /*private final Vec3 cameraPos;

      public AfterTranslucentBlocks(PoseStack poseStack, Vec3 cameraPos) {
         this.poseStack = poseStack;
         this.cameraPos = cameraPos;
      }

      public Vec3 getCameraPos() {
         return this.cameraPos;
      }
      *///?}

      public PoseStack getPoseStack() {
         return this.poseStack;
      }
   }
}
