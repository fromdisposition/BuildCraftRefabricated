package buildcraft.lib.platform.mixin.client;

import buildcraft.lib.client.fluid.FluidWorldRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
//? if >= 26.1.3 {
//?} else {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
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
   @Final
   @Shadow
   private Minecraft minecraft;

   //? if >= 26.1.3 {
   //?} else {
   @Final
   @Shadow
   private MultiBufferSource bufferSource;
   //?}

   @Inject(
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
      //? if >= 26.1.3 {
      /*FluidWorldRenderer.renderSubmergedOverlay(this.minecraft, poseStack, submitNodeCollector);*/
      //?} else {
      FluidWorldRenderer.renderSubmergedOverlay(this.minecraft, poseStack, this.bufferSource);
      //?}
   }
}
