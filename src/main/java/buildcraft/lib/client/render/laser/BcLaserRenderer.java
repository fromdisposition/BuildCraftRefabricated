package buildcraft.lib.client.render.laser;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.LightUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public final class BcLaserRenderer {
   private static final Map<LaserData_BC8.LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new HashMap<>();

   private BcLaserRenderer() {
   }

   public static void clearModels() {
      COMPILED_LASER_TYPES.clear();
   }

   private static CompiledLaserType compileType(LaserData_BC8.LaserType laserType) {
      return COMPILED_LASER_TYPES.computeIfAbsent(laserType, CompiledLaserType::new);
   }

   public static void renderLaser(PoseStack poseStack, VertexConsumer consumer, LaserData_BC8 data, Vec3 cameraPos) {
      ILaserRenderer vertexWriter = (x, y, z, u, v, lmap, nx, ny, nz, colour) -> {
         float rx = (float)(x - cameraPos.x);
         float ry = (float)(y - cameraPos.y);
         float rz = (float)(z - cameraPos.z);
         int r = (int)(colour * 255.0F);
         int g = (int)(colour * 255.0F);
         int b = (int)(colour * 255.0F);
         int a = 255;
         consumer.addVertex(poseStack.last().pose(), rx, ry, rz)
            .setColor(r, g, b, a)
            .setUv((float)u, (float)v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(lmap)
            .setNormal(poseStack.last(), nx, ny, nz);
      };
      LaserContext ctx = new LaserContext(vertexWriter, data, data.enableDiffuse, data.doubleFace);
      CompiledLaserType type = compileType(data.laserType);
      type.bakeFor(ctx);
   }

   public static void renderLaserStatic(PoseStack poseStack, LaserData_BC8 data, Vec3 cameraPos) {
      renderLasersBatched(poseStack, List.of(data), cameraPos);
   }

   public static void renderLasersBatched(PoseStack poseStack, List<LaserData_BC8> lasers, Vec3 cameraPos) {
      if (!lasers.isEmpty()) {
         BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
         VertexConsumer consumer = bufferSource.getBuffer(BCLibRenderTypes.entityTranslucent(BcTextureAtlases.BLOCKS_TEXTURE));

         for (LaserData_BC8 data : lasers) {
            renderLaser(poseStack, consumer, data, cameraPos);
         }

         if (!LaserBatch.isActive()) {
            bufferSource.endBatch();
         }
      }
   }

   public static int computeLightmap(double x, double y, double z, int minBlockLight) {
      Level level = Minecraft.getInstance().level;
      if (level == null) {
         return 15728880;
      }

      BlockPos pos = BlockPos.containing(x, y, z);
      int blockLight = minBlockLight >= 15 ? 15 : Math.max(minBlockLight, level.getBrightness(LightLayer.BLOCK, pos));
      int skyLight = level.getBrightness(LightLayer.SKY, pos);
      return LightUtil.pack(blockLight, skyLight);
   }
}
