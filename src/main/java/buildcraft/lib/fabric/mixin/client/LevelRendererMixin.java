package buildcraft.lib.fabric.mixin.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import buildcraft.fabric.client.render.BlockOutlineRenderer;
import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Unique
    private static final Map<BlockOutlineRenderState, List<BlockOutlineRenderer>> BUILDCRAFT_CUSTOM_OUTLINES =
            new WeakHashMap<>();

    @Shadow
    private ClientLevel level;

    @Shadow
    private Minecraft minecraft;

    @Inject(method = "extractBlockOutline", at = @At("RETURN"))
    private void buildcraft$afterExtractBlockOutline(Camera camera, LevelRenderState levelRenderState, CallbackInfo ci) {
        BlockOutlineRenderState outline = levelRenderState.blockOutlineRenderState;
        if (outline == null) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        var event = new ExtractBlockOutlineRenderStateEvent(
                (LevelRenderer) (Object) this,
                level,
                pos,
                state,
                blockHit,
                CollisionContext.of(camera.entity()),
                camera,
                levelRenderState);
        ExtractBlockOutlineRenderStateEvent.fire(event);
        if (event.isCanceled()) {
            levelRenderState.blockOutlineRenderState = null;
            BUILDCRAFT_CUSTOM_OUTLINES.remove(outline);
            return;
        }
        BUILDCRAFT_CUSTOM_OUTLINES.put(
                outline,
                event.getCustomRenderers() == null ? Collections.emptyList() : event.getCustomRenderers());
    }

    @Inject(
            method = "renderBlockOutline",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;isTranslucent()Z"),
            cancellable = true)
    private void buildcraft$renderCustomBlockOutline(
            MultiBufferSource.BufferSource bufferSource,
            PoseStack poseStack,
            boolean translucentPass,
            LevelRenderState levelRenderState,
            CallbackInfo ci) {
        BlockOutlineRenderState outline = levelRenderState.blockOutlineRenderState;
        if (outline == null) {
            return;
        }
        List<BlockOutlineRenderer> custom =
                BUILDCRAFT_CUSTOM_OUTLINES.getOrDefault(outline, Collections.emptyList());
        if (custom.isEmpty()) {
            return;
        }
        boolean cancel = false;
        for (BlockOutlineRenderer renderer : custom) {
            cancel |= renderer.render(outline, bufferSource, poseStack, translucentPass, levelRenderState);
        }
        if (cancel) {
            ci.cancel();
        }
    }
}


