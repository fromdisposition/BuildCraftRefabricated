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
import net.minecraft.client.renderer.MultiBufferSource;
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
      FluidState fluidState = fluidStateAtEye(entity, level);
      return fluidState.isEmpty() ? null : BcFluidAppearanceCache.get(fluidState.getType());
   }

   public static @Nullable BcFluidAppearance appearanceAtCamera(Camera camera, ClientLevel level) {
      FluidState fluidState = level.getFluidState(camera.blockPosition());
      if (camera.position().y > camera.blockPosition().getY() + fluidState.getHeight(level, camera.blockPosition())) {
         fluidState = fluidStateAtEye(camera.entity(), level);
      }

      return fluidState.isEmpty() ? null : BcFluidAppearanceCache.get(fluidState.getType());
   }

   public static FluidState fluidStateAtEye(Entity entity, Level level) {
      return level.getFluidState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
   }

   public static boolean skipWaterVision(LocalPlayer player) {
      if (player.level() == null) {
         return false;
      }

      FluidState fluidState = fluidStateAtEye(player, player.level());
      return !fluidState.isEmpty() && fluidState.getType().is(BcFluidTags.BC_FLUIDS);
   }

   public static void renderSubmergedOverlay(Minecraft minecraft, PoseStack poseStack, MultiBufferSource bufferSource) {
      if (minecraft.player == null || !BcFluidUtil.isEyeInBcFluid(minecraft.player)) {
         return;
      }

      FluidState fluidState = fluidStateAtEye(minecraft.player, minecraft.player.level());
      if (fluidState.isEmpty() || !fluidState.getType().is(BcFluidTags.BC_FLUIDS)) {
         return;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluidState.getType());
      if (entry == null) {
         return;
      }

      BcFluidAppearance appearance = BcFluidAppearanceCache.get(fluidState.getType());
      if (appearance == null) {
         return;
      }

      renderOverlay(
         minecraft,
         poseStack,
         bufferSource,
         Identifier.fromNamespaceAndPath("buildcraftenergy", "textures/block/fluids/" + entry.name() + "_underwater.png"),
         appearance.overlayAlpha()
      );
   }

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
}
