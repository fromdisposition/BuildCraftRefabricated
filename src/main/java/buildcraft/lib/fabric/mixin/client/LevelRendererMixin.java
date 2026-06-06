package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;
import buildcraft.fabric.client.render.BlockOutlineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
   @Unique
   private static final Map<BlockOutlineRenderState, List<BlockOutlineRenderer>> BUILDCRAFT_CUSTOM_OUTLINES = new WeakHashMap<>();
   @Shadow
   private ClientLevel level;
   @Shadow
   private Minecraft minecraft;

   @Inject(method = "extractBlockOutline", at = @At("RETURN"))
   private void buildcraft$afterExtractBlockOutline(Camera camera, LevelRenderState levelRenderState, CallbackInfo ci) {
      BlockOutlineRenderState outline = levelRenderState.blockOutlineRenderState;
      if (outline != null) {
         if (this.minecraft.hitResult instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = this.level.getBlockState(pos);
            if (!state.isAir()) {
               ExtractBlockOutlineRenderStateEvent event = new ExtractBlockOutlineRenderStateEvent(
                  (LevelRenderer)(Object)this, this.level, pos, state, blockHit, CollisionContext.of(camera.entity()), camera, levelRenderState
               );
               ExtractBlockOutlineRenderStateEvent.fire(event);
               if (event.isCanceled()) {
                  levelRenderState.blockOutlineRenderState = null;
                  BUILDCRAFT_CUSTOM_OUTLINES.remove(outline);
               } else {
                  BUILDCRAFT_CUSTOM_OUTLINES.put(outline, event.getCustomRenderers() == null ? Collections.emptyList() : event.getCustomRenderers());
               }
            }
         }
      }
   }

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
         List<BlockOutlineRenderer> custom = BUILDCRAFT_CUSTOM_OUTLINES.getOrDefault(outline, Collections.emptyList());
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
}
