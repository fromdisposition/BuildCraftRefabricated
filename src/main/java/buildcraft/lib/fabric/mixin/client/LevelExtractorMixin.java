package buildcraft.lib.fabric.mixin.client;

//? if >= 1.21.10 {
import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;
import buildcraft.fabric.client.render.BlockOutlineRenderStore;
import java.util.Collections;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}
import org.spongepowered.asm.mixin.Mixin;

/**
 * In 26.2, block-outline state extraction moved from {@code LevelRenderer} to {@code LevelExtractor}.
 * This mixin hooks {@code extractBlockOutline} there and fires {@code ExtractBlockOutlineRenderStateEvent}.
 * On 1.21.1 the level render-state extraction does not exist, so this degrades to an empty no-op mixin
 * (custom block outlines / pipe-placement preview highlight are a cosmetic loss there).
 */
//? if >= 1.21.10 {
@Mixin(targets = "net.minecraft.client.renderer.extract.LevelExtractor", remap = false)
public abstract class LevelExtractorMixin {
   @Shadow(remap = false)
   private Minecraft minecraft;
   @Shadow(remap = false)
   private ClientLevel level;
   @Shadow(remap = false)
   private LevelRenderer levelRenderer;

   @Inject(method = "extractBlockOutline", at = @At("RETURN"), remap = false, require = 0)
   private void buildcraft$afterExtractBlockOutline(Camera camera, LevelRenderState levelRenderState, CallbackInfo ci) {
      BlockOutlineRenderState outline = levelRenderState.blockOutlineRenderState;
      if (outline == null || !ExtractBlockOutlineRenderStateEvent.hasListeners()) return;
      if (!(this.minecraft.hitResult instanceof BlockHitResult blockHit) || this.level == null) return;
      BlockPos pos = blockHit.getBlockPos();
      BlockState state = this.level.getBlockState(pos);
      if (state.isAir()) return;
      var cameraEntity = camera.entity() != null ? camera.entity() : this.minecraft.player;
      ExtractBlockOutlineRenderStateEvent event = new ExtractBlockOutlineRenderStateEvent(
         this.levelRenderer, this.level, pos, state, blockHit, CollisionContext.of(cameraEntity), camera, levelRenderState
      );
      ExtractBlockOutlineRenderStateEvent.fire(event);
      if (event.isCanceled()) {
         levelRenderState.blockOutlineRenderState = null;
         BlockOutlineRenderStore.CUSTOM_OUTLINES.remove(outline);
      } else {
         BlockOutlineRenderStore.CUSTOM_OUTLINES.put(
            outline, event.getCustomRenderers().isEmpty() ? Collections.emptyList() : event.getCustomRenderers()
         );
      }
   }
}
//?} else {
/*@Mixin(net.minecraft.client.Minecraft.class)
public abstract class LevelExtractorMixin {
}
*///?}
