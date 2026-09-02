package buildcraft.lib.fabric.mixin.client;

import buildcraft.lib.client.fluid.FluidWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererBcFluidMixin {
   //? if < 26.3-pre-1 {
   /*@Final
   @Shadow
   private Minecraft minecraft;
   *///?}

   // ScreenEffectRenderer.renderScreenEffect differs by version:
   //   1.21.11 : renderScreenEffect(boolean sleeping, float partialTicks, SubmitNodeCollector) — 3-arg
   //   26.1.2  : renderScreenEffect(boolean isFirstPerson, boolean isSleeping, float, SubmitNodeCollector, boolean hideGui) — 5-arg
   //   26.2    : same 5 args but the method was renamed to submit(...)
   //   26.3-pre: submit(float, SubmitNodeCollector, PlayerRenderState, CameraRenderState, boolean) — TAIL + fresh identity PoseStack
   //? if >= 26.3-pre-1 {
   @Inject(method = "submit", at = @At("TAIL"))
   private void buildcraft$renderBcFluidOverlay(
      float partialTicks,
      SubmitNodeCollector submitNodeCollector,
      net.minecraft.client.renderer.state.level.PlayerRenderState playerRenderState,
      net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState,
      boolean hideGui,
      CallbackInfo ci
   ) {
      FluidWorldRenderer.renderSubmergedOverlay(Minecraft.getInstance(), new PoseStack(), submitNodeCollector);
   }
   //?} else if >= 26.2 {
   /*@Inject(
      method = "submit",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"),
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void buildcraft$renderBcFluidOverlay(
      boolean isFirstPerson,
      boolean isSleeping,
      float partialTicks,
      SubmitNodeCollector submitNodeCollector,
      boolean hideGui,
      CallbackInfo ci,
      PoseStack poseStack
   ) {
      FluidWorldRenderer.renderSubmergedOverlay(this.minecraft, poseStack, submitNodeCollector);
   }
   *///?} else if >= 26.1 {
   /*@Inject(
      method = "renderScreenEffect",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"),
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void buildcraft$renderBcFluidOverlay(
      boolean isFirstPerson,
      boolean isSleeping,
      float partialTicks,
      SubmitNodeCollector submitNodeCollector,
      boolean hideGui,
      CallbackInfo ci,
      PoseStack poseStack
   ) {
      FluidWorldRenderer.renderSubmergedOverlay(this.minecraft, poseStack, submitNodeCollector);
   }
   *///?} else {
   /*@Inject(
      method = "renderScreenEffect",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z"),
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void buildcraft$renderBcFluidOverlay(
      boolean sleeping,
      float partialTicks,
      SubmitNodeCollector submitNodeCollector,
      CallbackInfo ci,
      PoseStack poseStack
   ) {
      FluidWorldRenderer.renderSubmergedOverlay(this.minecraft, poseStack, submitNodeCollector);
   }
   *///?}
}
