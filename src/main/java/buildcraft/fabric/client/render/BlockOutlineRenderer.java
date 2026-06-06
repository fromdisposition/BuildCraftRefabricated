package buildcraft.fabric.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

public interface BlockOutlineRenderer {
   boolean render(BlockOutlineRenderState var1, BufferSource var2, PoseStack var3, boolean var4, LevelRenderState var5);
}
