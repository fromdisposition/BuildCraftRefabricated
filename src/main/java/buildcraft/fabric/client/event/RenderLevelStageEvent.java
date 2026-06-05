package buildcraft.fabric.client.event;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.state.level.LevelRenderState;

public class RenderLevelStageEvent {
    public static final class AfterTranslucentBlocks {
        private final PoseStack poseStack;
        private final LevelRenderState levelRenderState;

        public AfterTranslucentBlocks(PoseStack poseStack, LevelRenderState levelRenderState) {
            this.poseStack = poseStack;
            this.levelRenderState = levelRenderState;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public LevelRenderState getLevelRenderState() {
            return levelRenderState;
        }
    }
}

