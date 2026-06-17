package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;
import buildcraft.fabric.client.render.BlockOutlineRenderer;
import buildcraft.fabric.client.render.BlockOutlineRenderStore;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
//? if >= 26.1.3 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;*/
//?} else {
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//?}
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
 * Provides two hooks not available through Fabric API:
 * <ol>
 *   <li>{@code extractBlockOutline} (26.1.2) / {@code LevelExtractor.extractBlockOutline} (26.2)
 *       — fires {@link ExtractBlockOutlineRenderStateEvent} so BC can inject custom outline
 *       renderers (pipe plug previews) or cancel the vanilla outline.</li>
 *   <li>{@code renderBlockOutline} (26.1.2) / {@code submitBlockOutline} (26.2) — dispatches to
 *       those custom renderers, respecting the translucent/opaque pass distinction.</li>
 * </ol>
 *
 * Fabric API's {@code WorldRenderEvents.BLOCK_OUTLINE} fires once without a pass discriminator,
 * making it impossible to correctly split opaque/translucent rendering. Keep this mixin until
 * Fabric exposes per-pass block-outline callbacks.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
   //? if < 26.1.3 {
   @Shadow
   private ClientLevel level;
   @Shadow
   private Minecraft minecraft;

   @Inject(method = "extractBlockOutline", at = @At("RETURN"))
   private void buildcraft$afterExtractBlockOutline(Camera camera, LevelRenderState levelRenderState, CallbackInfo ci) {
      BlockOutlineRenderState outline = levelRenderState.blockOutlineRenderState;
      if (outline == null || !ExtractBlockOutlineRenderStateEvent.hasListeners()) return;
      if (!(this.minecraft.hitResult instanceof BlockHitResult blockHit) || this.level == null) return;
      BlockPos pos = blockHit.getBlockPos();
      BlockState state = this.level.getBlockState(pos);
      if (state.isAir()) return;
      ExtractBlockOutlineRenderStateEvent event = new ExtractBlockOutlineRenderStateEvent(
         (LevelRenderer)(Object)this, this.level, pos, state, blockHit, CollisionContext.of(camera.entity()), camera, levelRenderState
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
   //?}

   //? if >= 26.1.3 {
   /*@Inject(
      method = "submitBlockOutline",
      at = @At("HEAD"),
      cancellable = true
   )
   private void buildcraft$renderCustomBlockOutline(
      PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci
   ) {
      BlockOutlineRenderState outline = levelRenderState.blockOutlineRenderState;
      if (outline != null) {
         List<BlockOutlineRenderer> custom = BlockOutlineRenderStore.CUSTOM_OUTLINES.getOrDefault(outline, Collections.emptyList());
         if (!custom.isEmpty()) {
            boolean cancel = false;
            for (BlockOutlineRenderer renderer : custom) {
               cancel |= renderer.render(outline, submitNodeCollector, poseStack, levelRenderState);
            }
            if (cancel) {
               ci.cancel();
            }
         }
      }
   }*/
   //?} else {
   @Inject(
      method = "renderBlockOutline",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;isTranslucent()Z"),
      cancellable = true
   )
   private void buildcraft$renderCustomBlockOutline(
      BufferSource bufferSource, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState, CallbackInfo ci
   ) {
      BlockOutlineRenderState outline = levelRenderState.blockOutlineRenderState;
      if (outline != null) {
         List<BlockOutlineRenderer> custom = BlockOutlineRenderStore.CUSTOM_OUTLINES.getOrDefault(outline, Collections.emptyList());
         if (!custom.isEmpty()) {
            boolean cancel = false;
            for (BlockOutlineRenderer renderer : custom) {
               cancel |= renderer.render(outline, bufferSource, poseStack, translucentPass, levelRenderState);
            }
            if (cancel) {
               ci.cancel();
            }
         }
      }
   }
   //?}
}
