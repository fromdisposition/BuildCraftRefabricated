package buildcraft.lib.client.fluid;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.fabric.fluid.BcFluidTags;
import buildcraft.fabric.fluid.BcFluidUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Lightmap;
//? if >= 26.1.3 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;*/
//?} else {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class FluidWorldRenderer {
   private FluidWorldRenderer() {
   }

   public static @Nullable BcFluidAppearance appearanceAtEye(Entity entity, Level level) {
      FluidState fluidState = submergedBcFluidStateAt(entity.getX(), entity.getEyeY(), entity.getZ(), level);
      return fluidState == null ? null : appearanceForBcFluid(fluidState);
   }

   public static @Nullable BcFluidAppearance appearanceAtCamera(Camera camera, ClientLevel level) {
      Entity entity = camera.entity();
      if (entity == null) {
         return null;
      }

      BlockPos cameraPos = camera.blockPosition();
      FluidState atCamera = level.getFluidState(cameraPos);
      if (BcFluidUtil.isBcFluidState(atCamera)
         && BcFluidUtil.isSubmergedInBcFluid(level, camera.position().x, camera.position().y, camera.position().z)) {
         return appearanceForBcFluid(atCamera);
      }

      return appearanceAtEye(entity, level);
   }

   public static FluidState fluidStateAtEye(Entity entity, Level level) {
      FluidState submerged = submergedBcFluidStateAt(entity.getX(), entity.getEyeY(), entity.getZ(), level);
      return submerged != null ? submerged : level.getFluidState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
   }

   public static boolean skipWaterVision(LocalPlayer player) {
      return player.level() != null && appearanceAtEye(player, player.level()) != null;
   }

   //? if >= 26.1.3 {
   /*public static void renderSubmergedOverlay(Minecraft minecraft, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      if (minecraft.player == null || minecraft.player.level() == null) {
         return;
      }

      FluidState fluidState = submergedBcFluidStateAt(
         minecraft.player.getX(),
         minecraft.player.getEyeY(),
         minecraft.player.getZ(),
         minecraft.player.level()
      );
      if (fluidState == null) {
         return;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluidState.getType());
      if (entry == null) {
         return;
      }

      BcFluidAppearance appearance = appearanceForBcFluid(fluidState);
      if (appearance == null) {
         return;
      }

      renderOverlay(
         minecraft,
         poseStack,
         submitNodeCollector,
         Identifier.fromNamespaceAndPath("buildcraftenergy", "textures/block/fluids/underwater/" + entry.name() + ".png"),
         appearance.overlayAlpha()
      );
   }

   private static void renderOverlay(Minecraft minecraft, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Identifier location, float overlayAlpha) {
      BlockPos blockPos = BlockPos.containing(minecraft.player.getX(), minecraft.player.getEyeY(), minecraft.player.getZ());
      float brightness = Lightmap.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(blockPos));
      int color = ARGB.colorFromFloat(overlayAlpha, brightness, brightness, brightness);
      float yawOffset = -minecraft.player.getYRot() / 64.0F;
      float pitchOffset = minecraft.player.getXRot() / 64.0F;
      submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.blockScreenEffect(location), (p, vc) -> {
         Matrix4f matrix4f = p.pose();
         vc.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(4.0F + yawOffset, 4.0F + pitchOffset).setColor(color);
         vc.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(0.0F + yawOffset, 4.0F + pitchOffset).setColor(color);
         vc.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(0.0F + yawOffset, 0.0F + pitchOffset).setColor(color);
         vc.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(4.0F + yawOffset, 0.0F + pitchOffset).setColor(color);
      });
   }*/
   //?} else {
   public static void renderSubmergedOverlay(Minecraft minecraft, PoseStack poseStack, MultiBufferSource bufferSource) {
      if (minecraft.player == null || minecraft.player.level() == null) {
         return;
      }

      FluidState fluidState = submergedBcFluidStateAt(
         minecraft.player.getX(),
         minecraft.player.getEyeY(),
         minecraft.player.getZ(),
         minecraft.player.level()
      );
      if (fluidState == null) {
         return;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluidState.getType());
      if (entry == null) {
         return;
      }

      BcFluidAppearance appearance = appearanceForBcFluid(fluidState);
      if (appearance == null) {
         return;
      }

      renderOverlay(
         minecraft,
         poseStack,
         bufferSource,
         Identifier.fromNamespaceAndPath("buildcraftenergy", "textures/block/fluids/underwater/" + entry.name() + ".png"),
         appearance.overlayAlpha()
      );
   }
   //?}

   private static @Nullable FluidState submergedBcFluidStateAt(double x, double sampleY, double z, Level level) {
      if (!BcFluidUtil.isSubmergedInBcFluid(level, x, sampleY, z)) {
         return null;
      }

      return level.getFluidState(BlockPos.containing(x, sampleY, z));
   }

   private static @Nullable BcFluidAppearance appearanceForBcFluid(FluidState fluidState) {
      if (!fluidState.is(BcFluidTags.BC_FLUIDS) || BCEnergyFluidsFabric.findEntry(fluidState.getType()) == null) {
         return null;
      }

      return BcFluidAppearanceCache.get(fluidState.getType());
   }

   //? if < 26.1.3 {
   private static void renderOverlay(Minecraft minecraft, PoseStack poseStack, MultiBufferSource multiBufferSource, Identifier location, float overlayAlpha) {
      BlockPos blockPos = BlockPos.containing(minecraft.player.getX(), minecraft.player.getEyeY(), minecraft.player.getZ());
      float brightness = Lightmap.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(blockPos));
      int color = ARGB.colorFromFloat(overlayAlpha, brightness, brightness, brightness);
      float yawOffset = -minecraft.player.getYRot() / 64.0F;
      float pitchOffset = minecraft.player.getXRot() / 64.0F;
      Matrix4f matrix4f = poseStack.last().pose();
      VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.blockScreenEffect(location));
      vertexConsumer.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(4.0F + yawOffset, 4.0F + pitchOffset).setColor(color);
      vertexConsumer.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(0.0F + yawOffset, 4.0F + pitchOffset).setColor(color);
      vertexConsumer.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(0.0F + yawOffset, 0.0F + pitchOffset).setColor(color);
      vertexConsumer.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(4.0F + yawOffset, 0.0F + pitchOffset).setColor(color);
   }
   //?}
}
