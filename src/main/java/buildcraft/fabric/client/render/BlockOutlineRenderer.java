package buildcraft.fabric.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
//? if >= 26.2 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
*///?} else {
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//?}
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

public interface BlockOutlineRenderer {
   //? if >= 26.2 {
   /*boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState);
   *///?} else {
   boolean render(BlockOutlineRenderState var1, BufferSource var2, PoseStack var3, boolean var4, LevelRenderState var5);
   //?}
}
