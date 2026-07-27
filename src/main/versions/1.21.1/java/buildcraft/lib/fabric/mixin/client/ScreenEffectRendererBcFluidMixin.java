/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.mixin.client;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.client.fluid.FluidWorldRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.1 implementation (versions/1.21.1). The shared mixin draws BuildCraft's submerged screen overlay via the
 * 1.21.5 submit pipeline (FluidWorldRenderer.renderSubmergedOverlay + SubmitNodeCollector), which does not exist
 * on 1.21.1. Here the overlay is drawn immediately, mirroring vanilla 1.21.1 {@code ScreenEffectRenderer.renderTex}:
 * a single textured quad in the {@code POSITION_TEX_COLOR} format (NOT a block/entity render type, which would
 * demand UV1/UV2/Normal and crash with "Missing elements in vertex"), via Tesselator + BufferUploader with the
 * position_tex_color shader. UVs drift with the look direction; colour = overlay alpha × local brightness.
 */
@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererBcFluidMixin {
   @Inject(
      method = "renderScreenEffect(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
      at = @At("HEAD")
   )
   private static void buildcraft$renderBcFluidOverlay(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
      LocalPlayer player = minecraft.player;
      if (player == null || player.level() == null) {
         return;
      }
      Level level = player.level();
      BcFluidAppearance appearance = FluidWorldRenderer.appearanceAtEye(player, level);
      if (appearance == null) {
         return;
      }
      FluidState fluidState = FluidWorldRenderer.fluidStateAtEye(player, level);
      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluidState.getType());
      if (entry == null) {
         return;
      }

      Identifier texture = Identifier.fromNamespaceAndPath(
         "buildcraftenergy", "textures/block/fluids/underwater/" + entry.name() + ".png"
      );
      BlockPos pos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
      float brightness = LightCoordsUtil.getBrightness(level.dimensionType(), level.getMaxLocalRawBrightness(pos));
      int color = FastColor.ARGB32.colorFromFloat(appearance.overlayAlpha(), brightness, brightness, brightness);
      float yawOffset = -player.getYRot() / 64.0F;
      float pitchOffset = player.getXRot() / 64.0F;

      // Immediate draw matching vanilla renderTex: POSITION_TEX_COLOR (position + uv + colour only).
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, texture);
      Matrix4f matrix = poseStack.last().pose();
      BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      buffer.addVertex(matrix, -1.0F, -1.0F, -0.5F).setUv(4.0F + yawOffset, 4.0F + pitchOffset).setColor(color);
      buffer.addVertex(matrix, 1.0F, -1.0F, -0.5F).setUv(0.0F + yawOffset, 4.0F + pitchOffset).setColor(color);
      buffer.addVertex(matrix, 1.0F, 1.0F, -0.5F).setUv(0.0F + yawOffset, 0.0F + pitchOffset).setColor(color);
      buffer.addVertex(matrix, -1.0F, 1.0F, -0.5F).setUv(4.0F + yawOffset, 0.0F + pitchOffset).setColor(color);
      MeshData mesh = buffer.buildOrThrow();
      BufferUploader.drawWithShader(mesh);
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
   }
}
