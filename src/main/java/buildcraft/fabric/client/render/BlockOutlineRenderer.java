package buildcraft.fabric.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeCollector;
//?} else if >= 1.21.10 {
/*import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
*///?}
//? if >= 1.21.10 {
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
//?}

// Only invoked in the render pass the outline belongs to, so there is no pass flag to test. Returning true
// suppresses the vanilla outline.
public interface BlockOutlineRenderer {
   //? if >= 26.2 {
   boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState);
   //?} else if >= 1.21.10 {
   /*boolean render(BlockOutlineRenderState var1, BufferSource var2, PoseStack var3, LevelRenderState var4);
   *///?}
}
