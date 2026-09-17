package buildcraft.lib.fabric.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >= 1.21.10 {
import net.minecraft.client.gui.GuiGraphicsExtractor.ScissorStack;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.gen.Accessor;
//?}
import org.spongepowered.asm.mixin.Mixin;

// On 1.21.1 GuiGraphics.ScissorStack is private and the render-state extractor is absent; this accessor
// is unused there (GuiGraphicsCompat short-circuits), so it degrades to an empty mixin.
@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
   //? if >= 1.21.10 {
   @Accessor("guiRenderState")
   GuiRenderState buildcraft$getGuiRenderState();

   @Accessor("scissorStack")
   ScissorStack buildcraft$getScissorStack();
   //?}
}
