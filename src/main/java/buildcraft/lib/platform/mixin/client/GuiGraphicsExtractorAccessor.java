package buildcraft.lib.platform.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >= 26 {
import net.minecraft.client.gui.GuiGraphicsExtractor.ScissorStack;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
   @Accessor("guiRenderState")
   GuiRenderState buildcraft$getGuiRenderState();

   @Accessor("scissorStack")
   ScissorStack buildcraft$getScissorStack();
}
//?} else {
/*import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
   // 1.21.x: GuiRenderState doesn't exist; accessor is a no-op stub
}
*///?}
