package buildcraft.fabric.client;

import buildcraft.lib.fabric.mixin.client.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jspecify.annotations.Nullable;

public final class GuiGraphicsCompat {
   private GuiGraphicsCompat() {
   }

   public static @Nullable ScreenRectangle peekScissorStack(GuiGraphicsExtractor graphics) {
      return ((GuiGraphicsExtractorAccessor)graphics).buildcraft$getScissorStack().peek();
   }

   public static void submitPictureInPictureRenderState(GuiGraphicsExtractor graphics, PictureInPictureRenderState state) {
      ((GuiGraphicsExtractorAccessor)graphics).buildcraft$getGuiRenderState().addPicturesInPictureState(state);
   }
}
