package buildcraft.fabric.client;

//? if >= 1.21.10 {
import buildcraft.lib.fabric.mixin.client.GuiGraphicsExtractorAccessor;
//?}
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
//?}
import org.jspecify.annotations.Nullable;

public final class GuiGraphicsCompat {
   private GuiGraphicsCompat() {
   }

   public static @Nullable ScreenRectangle peekScissorStack(GuiGraphicsExtractor graphics) {
      //? if >= 1.21.10 {
      return ((GuiGraphicsExtractorAccessor)graphics).buildcraft$getScissorStack().peek();
      //?} else {
      /*return null; // 1.21.1: GUI render-state extractor absent; PiP previews disabled (cosmetic loss).
      *///?}
   }

   //? if >= 26.1 {
   public static void submitPictureInPictureRenderState(GuiGraphicsExtractor graphics, PictureInPictureRenderState state) {
      ((GuiGraphicsExtractorAccessor)graphics).buildcraft$getGuiRenderState().addPicturesInPictureState(state);
   }
   //?} else if >= 1.21.10 {
   /*public static void submitPictureInPictureRenderState(GuiGraphicsExtractor graphics, PictureInPictureRenderState state) {
      ((GuiGraphicsExtractorAccessor)graphics).buildcraft$getGuiRenderState().submitPicturesInPictureState(state);
   }
   *///?}
}
