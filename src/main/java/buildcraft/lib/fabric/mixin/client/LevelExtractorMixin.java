package buildcraft.lib.fabric.mixin.client;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * In 26.2, block-outline state extraction moved from {@code LevelRenderer} to
 * {@code LevelExtractor}. This mixin hooks {@code extractBlockOutline} there and fires
 * {@link ExtractBlockOutlineRenderStateEvent}, replacing the 26.1.2 hook in
 * {@link LevelRendererMixin}.
 *
 * Uses {@code targets} string form so the mixin is silently ignored in 26.1.2 where
 * {@code LevelExtractor} does not exist.
 */
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
      ExtractBlockOutlineRenderStateEvent event = new ExtractBlockOutlineRenderStateEvent(
         this.levelRenderer, this.level, pos, state, blockHit, CollisionContext.of(camera.entity()), camera, levelRenderState
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
