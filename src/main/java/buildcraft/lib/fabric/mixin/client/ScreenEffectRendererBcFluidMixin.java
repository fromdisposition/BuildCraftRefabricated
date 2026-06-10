package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.fabric.fluid.BcFluidClientAppearance;
import buildcraft.fabric.fluid.BcFluidTags;
import buildcraft.fabric.fluid.BcFluidUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.Fluid;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererBcFluidMixin {
   @Final
   @Shadow
   private Minecraft minecraft;

   @Final
   @Shadow
   private MultiBufferSource bufferSource;

   @Redirect(
      method = "renderScreenEffect",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
   )
   private boolean buildcraft$skipVanillaWaterOverlay(LocalPlayer player, TagKey<Fluid> tag) {
      if (tag == FluidTags.WATER && player.isEyeInFluid(BcFluidTags.BC_FLUIDS)) {
         return false;
      }

      return player.isEyeInFluid(tag);
   }

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
      if (this.minecraft.player == null || !this.minecraft.player.isEyeInFluid(BcFluidTags.BC_FLUIDS)) {
         return;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(
         this.minecraft.player.level()
            .getFluidState(BlockPos.containing(this.minecraft.player.getX(), this.minecraft.player.getEyeY(), this.minecraft.player.getZ()))
            .getType()
      );
      if (entry == null) {
         return;
      }

      BcFluidClientAppearance appearance = BcFluidUtil.clientAppearance(
         this.minecraft.player.level()
            .getFluidState(BlockPos.containing(this.minecraft.player.getX(), this.minecraft.player.getEyeY(), this.minecraft.player.getZ()))
      );
      if (appearance == null) {
         return;
      }

      renderOverlay(
         this.minecraft,
         poseStack,
         this.bufferSource,
         Identifier.fromNamespaceAndPath("buildcraftenergy", "textures/block/fluids/" + entry.name() + "_underwater.png"),
         appearance.overlayAlpha()
      );
   }

   private static void renderOverlay(Minecraft minecraft, PoseStack poseStack, MultiBufferSource multiBufferSource, Identifier location, float overlayAlpha) {
      BlockPos blockPos = BlockPos.containing(minecraft.player.getX(), minecraft.player.getEyeY(), minecraft.player.getZ());
      float f = Lightmap.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(blockPos));
      int i = ARGB.colorFromFloat(overlayAlpha, f, f, f);
      float n = -minecraft.player.getYRot() / 64.0F;
      float o = minecraft.player.getXRot() / 64.0F;
      Matrix4f matrix4f = poseStack.last().pose();
      VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.blockScreenEffect(location));
      vertexConsumer.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(4.0F + n, 4.0F + o).setColor(i);
      vertexConsumer.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(0.0F + n, 4.0F + o).setColor(i);
      vertexConsumer.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(0.0F + n, 0.0F + o).setColor(i);
      vertexConsumer.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(4.0F + n, 0.0F + o).setColor(i);
   }
}
