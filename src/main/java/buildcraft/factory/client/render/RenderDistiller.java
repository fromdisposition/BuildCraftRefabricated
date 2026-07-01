/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileDistiller;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.fluid.BcFluidBerHelper;
import buildcraft.lib.client.fluid.BcFluidTankRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import buildcraft.lib.client.render.BCLibRenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//? if >= 26.1 {
import net.minecraft.client.resources.model.sprite.SpriteId;
//?} else {
/*import net.minecraft.client.resources.model.sprite.Material;
*///?}
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderDistiller implements BlockEntityRenderer<TileDistiller, DistillerRenderState> {
   private static final Identifier BLOCKS_ATLAS_TEXTURE = Identifier.withDefaultNamespace("textures/atlas/blocks.png");
   private static final Map<Direction, RenderDistiller.TankSizes> TANK_SIZES = new EnumMap<>(Direction.class);
   private static final Identifier[] POWER_TEXTURES = new Identifier[]{
      Identifier.parse("buildcraftfactory:block/distiller/power_sprite_a"),
      Identifier.parse("buildcraftfactory:block/distiller/power_sprite_a"),
      Identifier.parse("buildcraftfactory:block/distiller/power_sprite_b"),
      Identifier.parse("buildcraftfactory:block/distiller/power_sprite_b"),
      Identifier.parse("buildcraftfactory:block/distiller/power_sprite_c"),
      Identifier.parse("buildcraftfactory:block/distiller/power_sprite_c"),
      Identifier.parse("buildcraftfactory:block/distiller/power_sprite_d")
   };
   private static final boolean[] POWER_TOP_HALF = new boolean[]{true, false, true, false, true, false, true};
   private final TextureAtlasSprite[] powerSprites = new TextureAtlasSprite[POWER_TEXTURES.length];

   public RenderDistiller(Context context) {
      for (int i = 0; i < POWER_TEXTURES.length; i++) {
         //? if >= 26.1 {
         this.powerSprites[i] = context.sprites().get(new SpriteId(BLOCKS_ATLAS_TEXTURE, POWER_TEXTURES[i]));
         //?} else {
         /*this.powerSprites[i] = context.materials().get(new Material(BLOCKS_ATLAS_TEXTURE, POWER_TEXTURES[i]));
         *///?}
      }
   }

   public DistillerRenderState createRenderState() {
      return new DistillerRenderState();
   }

   @Override
   public void extractRenderState(TileDistiller tile, DistillerRenderState state, float partialTick, Vec3 cameraPos, @Nullable CrumblingOverlay crumblingOverlay) {
      BlockEntityRenderer.super.extractRenderState(tile, state, partialTick, cameraPos, crumblingOverlay);
      this.extract(tile, state, partialTick);
   }

   private void extract(TileDistiller tile, DistillerRenderState state, float partialTick) {
      state.partialTick = partialTick;
      BlockState blockState = tile.getBlockState();
      state.facing = (Direction)blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
      state.sizes = TANK_SIZES.get(state.facing);
      state.fluidIn = DistillerFluidSnapshot.from(tile.getSmoothIn(), partialTick);
      state.fluidGasOut = DistillerFluidSnapshot.from(tile.getSmoothGasOut(), partialTick);
      state.fluidLiquidOut = DistillerFluidSnapshot.from(tile.getSmoothLiquidOut(), partialTick);
      double prevAnim = tile.getPrevAnimState();
      double curAnim = tile.getAnimState();
      double animState = prevAnim + (curAnim - prevAnim) * partialTick;
      double powerAvg = tile.getPowerAvgVisual();
      double stMod1 = animState - Math.floor(animState);
      state.powerY1 = (float)(1.0 - Math.abs(stMod1 - 0.5) * 2.0);
      double st2 = animState <= 0.5 ? 0.0 : animState - 0.5;
      double st2Mod1 = st2 - Math.floor(st2);
      state.powerY2 = (float)(1.0 - Math.abs(st2Mod1 - 0.5) * 2.0);
      if (powerAvg <= 0.0) {
         state.powerTexIndex = 0;
      } else {
         double frac = Math.min(1.0, powerAvg / TileDistiller.MAX_MJ_PER_TICK);
         state.powerTexIndex = Math.max(1, Math.min(6, (int)Math.ceil(frac * 6.0 + 0.35)));
      }

      state.powerTopHalf = POWER_TOP_HALF[state.powerTexIndex];
   }

   public void submit(DistillerRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      RenderDistiller.TankSizes sizes = renderState.sizes;
      if (sizes == null) {
         return;
      }

      int light = renderState.lightCoords;
      poseStack.pushPose();
      renderFluidSnapshot(renderState.fluidIn, sizes.tankIn, poseStack, collector, light);
      renderFluidSnapshot(renderState.fluidGasOut, sizes.tankGasOut, poseStack, collector, light);
      renderFluidSnapshot(renderState.fluidLiquidOut, sizes.tankLiquidOut, poseStack, collector, light);
      renderPowerCubes(renderState, sizes, poseStack, collector, light);
      poseStack.popPose();
   }

   private static void renderFluidSnapshot(
      @Nullable DistillerFluidSnapshot snapshot,
      BcFluidBerHelper.TankBounds bounds,
      PoseStack poseStack,
      SubmitNodeCollector collector,
      int light
   ) {
      if (snapshot == null) {
         return;
      }

      float shrink = 0.015625F;
      float minX = bounds.minX / 16.0F + shrink;
      float minY = bounds.minY / 16.0F + shrink;
      float minZ = bounds.minZ / 16.0F + shrink;
      float maxX = bounds.maxX / 16.0F - shrink;
      float maxY = bounds.maxY / 16.0F - shrink;
      float maxZ = bounds.maxZ / 16.0F - shrink;
      RenderType renderType = BcFluidAppearanceCache.renderType(snapshot.appearance());
      collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> BcFluidTankRenderer.renderFilledBox(
         pose,
         buffer,
         snapshot.appearance().sprite(),
         minX,
         minY,
         minZ,
         maxX,
         maxY,
         maxZ,
         snapshot.fluid(),
         snapshot.amount(),
         snapshot.capacity(),
         true,
         true,
         light,
         OverlayTexture.NO_OVERLAY
      ));
   }

   private void renderPowerCubes(DistillerRenderState renderState, RenderDistiller.TankSizes sizes, PoseStack poseStack, SubmitNodeCollector collector, int light) {
      TextureAtlasSprite sprite = this.powerSprites[renderState.powerTexIndex];
      boolean topHalf = renderState.powerTopHalf;
      float r = 1.0F;
      float g = 1.0F;
      float b = 1.0F;
      float a = 1.0F;
      int overlay = OverlayTexture.NO_OVERLAY;
      collector.submitCustomGeometry(poseStack, BCLibRenderTypes.entityCutout(BLOCKS_ATLAS_TEXTURE), (pose, buffer) -> {
         renderPowerCube(pose, buffer, sprite, topHalf, sizes.powerRight, renderState.powerY1, r, g, b, a, light, overlay);
         renderPowerCube(pose, buffer, sprite, topHalf, sizes.powerLeft, renderState.powerY2, r, g, b, a, light, overlay);
      });
   }

   private static void renderPowerCube(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      boolean topHalf,
      RenderDistiller.PowerCubeBounds pcb,
      float yFraction,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      float cubeMinY = yFraction * 12.0F / 16.0F;
      float cubeMaxY = cubeMinY + 0.25F;
      float minX = pcb.minX / 16.0F;
      float maxX = (pcb.minX + pcb.sizeX) / 16.0F;
      float minZ = pcb.minZ / 16.0F;
      float maxZ = (pcb.minZ + pcb.sizeZ) / 16.0F;
      float sprU0 = sprite.getU0();
      float sprU1 = sprite.getU1();
      float sprV0 = sprite.getV0();
      float sprV1 = sprite.getV1();
      float sprURange = sprU1 - sprU0;
      float sprVRange = sprV1 - sprV0;
      float vBase = topHalf ? 0.0F : 0.5F;
      float sideV0 = sprV0 + sprVRange * (vBase + 0.25F);
      float sideV1 = sprV0 + sprVRange * (vBase + 0.5F);
      float nsU0;
      float nsU1;
      if (pcb.sizeX >= 8.0F) {
         nsU0 = sprU0 + sprURange * 0.25F;
         nsU1 = sprU0 + sprURange * 0.75F;
      } else {
         nsU0 = sprU0 + sprURange * 0.0F;
         nsU1 = sprU0 + sprURange * 0.25F;
      }

      float ewU0;
      float ewU1;
      if (pcb.sizeZ >= 8.0F) {
         ewU0 = sprU0 + sprURange * 0.25F;
         ewU1 = sprU0 + sprURange * 0.75F;
      } else {
         ewU0 = sprU0 + sprURange * 0.0F;
         ewU1 = sprU0 + sprURange * 0.25F;
      }

      float udU0 = sprU0 + sprURange * 0.25F;
      float udU1 = sprU0 + sprURange * 0.75F;
      float udV0 = sprV0 + sprVRange * (vBase + 0.0F);
      float udV1 = sprV0 + sprVRange * (vBase + 0.25F);
      quadUV(
         pose,
         buffer,
         minX,
         cubeMaxY,
         minZ,
         maxX,
         cubeMaxY,
         minZ,
         maxX,
         cubeMinY,
         minZ,
         minX,
         cubeMinY,
         minZ,
         0.0F,
         0.0F,
         -1.0F,
         r,
         g,
         b,
         a,
         light,
         overlay,
         nsU0,
         sideV0,
         nsU1,
         sideV0,
         nsU1,
         sideV1,
         nsU0,
         sideV1
      );
      quadUV(
         pose,
         buffer,
         minX,
         cubeMinY,
         maxZ,
         maxX,
         cubeMinY,
         maxZ,
         maxX,
         cubeMaxY,
         maxZ,
         minX,
         cubeMaxY,
         maxZ,
         0.0F,
         0.0F,
         1.0F,
         r,
         g,
         b,
         a,
         light,
         overlay,
         nsU0,
         sideV1,
         nsU1,
         sideV1,
         nsU1,
         sideV0,
         nsU0,
         sideV0
      );
      quadUV(
         pose,
         buffer,
         minX,
         cubeMinY,
         minZ,
         minX,
         cubeMinY,
         maxZ,
         minX,
         cubeMaxY,
         maxZ,
         minX,
         cubeMaxY,
         minZ,
         -1.0F,
         0.0F,
         0.0F,
         r,
         g,
         b,
         a,
         light,
         overlay,
         ewU0,
         sideV1,
         ewU1,
         sideV1,
         ewU1,
         sideV0,
         ewU0,
         sideV0
      );
      quadUV(
         pose,
         buffer,
         maxX,
         cubeMaxY,
         minZ,
         maxX,
         cubeMaxY,
         maxZ,
         maxX,
         cubeMinY,
         maxZ,
         maxX,
         cubeMinY,
         minZ,
         1.0F,
         0.0F,
         0.0F,
         r,
         g,
         b,
         a,
         light,
         overlay,
         ewU0,
         sideV0,
         ewU1,
         sideV0,
         ewU1,
         sideV1,
         ewU0,
         sideV1
      );
      boolean rotated = pcb.sizeX < pcb.sizeZ;
      if (!rotated) {
         quadUV(
            pose,
            buffer,
            minX,
            cubeMaxY,
            maxZ,
            maxX,
            cubeMaxY,
            maxZ,
            maxX,
            cubeMaxY,
            minZ,
            minX,
            cubeMaxY,
            minZ,
            0.0F,
            1.0F,
            0.0F,
            r,
            g,
            b,
            a,
            light,
            overlay,
            udU0,
            udV1,
            udU1,
            udV1,
            udU1,
            udV0,
            udU0,
            udV0
         );
      } else {
         quadUV(
            pose,
            buffer,
            minX,
            cubeMaxY,
            maxZ,
            maxX,
            cubeMaxY,
            maxZ,
            maxX,
            cubeMaxY,
            minZ,
            minX,
            cubeMaxY,
            minZ,
            0.0F,
            1.0F,
            0.0F,
            r,
            g,
            b,
            a,
            light,
            overlay,
            udU0,
            udV0,
            udU0,
            udV1,
            udU1,
            udV1,
            udU1,
            udV0
         );
      }

      if (!rotated) {
         quadUV(
            pose,
            buffer,
            minX,
            cubeMinY,
            minZ,
            maxX,
            cubeMinY,
            minZ,
            maxX,
            cubeMinY,
            maxZ,
            minX,
            cubeMinY,
            maxZ,
            0.0F,
            -1.0F,
            0.0F,
            r,
            g,
            b,
            a,
            light,
            overlay,
            udU0,
            udV0,
            udU1,
            udV0,
            udU1,
            udV1,
            udU0,
            udV1
         );
      } else {
         quadUV(
            pose,
            buffer,
            minX,
            cubeMinY,
            minZ,
            maxX,
            cubeMinY,
            minZ,
            maxX,
            cubeMinY,
            maxZ,
            minX,
            cubeMinY,
            maxZ,
            0.0F,
            -1.0F,
            0.0F,
            r,
            g,
            b,
            a,
            light,
            overlay,
            udU1,
            udV0,
            udU1,
            udV1,
            udU0,
            udV1,
            udU0,
            udV0
         );
      }
   }

   private static void quadUV(
      Pose pose,
      VertexConsumer builder,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float nx,
      float ny,
      float nz,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay,
      float u1,
      float v1,
      float u2,
      float v2,
      float u3,
      float v3,
      float u4,
      float v4
   ) {
      builder.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
      builder.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u2, v2).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
      builder.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u3, v3).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
      builder.addVertex(pose, x4, y4, z4).setColor(r, g, b, a).setUv(u4, v4).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
   }

   static {
      RenderDistiller.TankSizes sizes = new RenderDistiller.TankSizes(
         new BcFluidBerHelper.TankBounds(0.0F, 0.0F, 4.0F, 8.0F, 16.0F, 12.0F),
         new BcFluidBerHelper.TankBounds(8.0F, 8.0F, 0.0F, 16.0F, 16.0F, 16.0F),
         new BcFluidBerHelper.TankBounds(8.0F, 0.0F, 0.0F, 16.0F, 8.0F, 16.0F),
         new RenderDistiller.PowerCubeBounds(0.0F, 12.0F, 8.0F, 4.0F, 4.0F),
         new RenderDistiller.PowerCubeBounds(0.0F, 0.0F, 8.0F, 4.0F, 4.0F)
      );
      Direction face = Direction.WEST;

      for (int i = 0; i < 4; i++) {
         TANK_SIZES.put(face, sizes);
         face = face.getClockWise();
         sizes = sizes.rotateY();
      }
   }

   static class PowerCubeBounds {
      final float minX;
      final float minZ;
      final float sizeX;
      final float sizeY;
      final float sizeZ;

      PowerCubeBounds(float minX, float minZ, float sizeX, float sizeY, float sizeZ) {
         this.minX = minX;
         this.minZ = minZ;
         this.sizeX = sizeX;
         this.sizeY = sizeY;
         this.sizeZ = sizeZ;
      }

      RenderDistiller.PowerCubeBounds rotateY() {
         float newMinX = 16.0F - this.minZ - this.sizeZ;
         float newMinZ = this.minX;
         return new RenderDistiller.PowerCubeBounds(newMinX, newMinZ, this.sizeZ, this.sizeY, this.sizeX);
      }
   }

   static class TankSizes {
      final BcFluidBerHelper.TankBounds tankIn;
      final BcFluidBerHelper.TankBounds tankGasOut;
      final BcFluidBerHelper.TankBounds tankLiquidOut;
      final RenderDistiller.PowerCubeBounds powerRight;
      final RenderDistiller.PowerCubeBounds powerLeft;

      TankSizes(
         BcFluidBerHelper.TankBounds tankIn,
         BcFluidBerHelper.TankBounds tankGasOut,
         BcFluidBerHelper.TankBounds tankLiquidOut,
         RenderDistiller.PowerCubeBounds powerRight,
         RenderDistiller.PowerCubeBounds powerLeft
      ) {
         this.tankIn = tankIn;
         this.tankGasOut = tankGasOut;
         this.tankLiquidOut = tankLiquidOut;
         this.powerRight = powerRight;
         this.powerLeft = powerLeft;
      }

      RenderDistiller.TankSizes rotateY() {
         return new RenderDistiller.TankSizes(
            this.tankIn.rotateY(), this.tankGasOut.rotateY(), this.tankLiquidOut.rotateY(), this.powerRight.rotateY(), this.powerLeft.rotateY()
         );
      }
   }
}
