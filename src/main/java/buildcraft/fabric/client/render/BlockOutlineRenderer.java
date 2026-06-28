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

// On 1.21.1 the level render-state extraction (and thus custom block outlines) does not exist; this
// degrades to an empty marker interface there (pipe-placement preview highlight is a cosmetic loss).
public interface BlockOutlineRenderer {
   //? if >= 26.2 {
   boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState);
   //?} else if >= 1.21.10 {
   /*boolean render(BlockOutlineRenderState var1, BufferSource var2, PoseStack var3, boolean var4, LevelRenderState var5);
   *///?}
}
