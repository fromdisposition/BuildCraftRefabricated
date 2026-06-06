package buildcraft.transport.client.render;

import buildcraft.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.transport.client.model.PipeMutableQuadCache;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public enum PipeBehaviourRendererStripes implements IPipeBehaviourRenderer<PipeBehaviourStripes> {
   INSTANCE;

   private static final MutableQuad[][] DIRECTION_QUADS = new MutableQuad[6][];

   public static void clearCaches() {
      Arrays.fill(DIRECTION_QUADS, null);
   }

   public void render(PipeBehaviourStripes stripes, double x, double y, double z, float partialTicks, VertexConsumer bb, Pose pose) {
      Direction dir = stripes.direction;
      if (dir != null && bb != null) {
         MutableQuad[] quads = getQuads(dir);
         int light = PipeRenderContext.getPackedLight();
         MutableQuad scratch = PipeMutableQuadCache.renderScratch();

         for (MutableQuad cached : quads) {
            scratch.copyFrom(cached);
            scratch.lighti(light);
            scratch.render(pose, bb);
         }
      }
   }

   private static MutableQuad[] getQuads(Direction dir) {
      int idx = dir.ordinal();
      if (DIRECTION_QUADS[idx] == null) {
         DIRECTION_QUADS[idx] = buildQuads(dir);
      }

      return DIRECTION_QUADS[idx];
   }

   private static MutableQuad[] buildQuads(Direction dir) {
      float minX = 0.0F;
      float maxX = 0.25F;
      float minY = 0.46875F;
      float maxY = 0.53125F;
      float minZ = 0.46875F;
      float maxZ = 0.53125F;
      TextureAtlas atlas = (TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
      TextureAtlasSprite sprite = atlas.getSprite(Identifier.parse("minecraft:block/gold_block"));
      float u0 = sprite.getU(0.0F);
      float u1 = sprite.getU(1.0F);
      float v0 = sprite.getV(0.0F);
      float v1 = sprite.getV(1.0F);
      MutableQuad[] quads = new MutableQuad[]{
         makeQuad(minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, 0.0F, -1.0F, 0.0F, u0, v0, u1, v1, sprite),
         makeQuad(minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, 0.0F, 1.0F, 0.0F, u0, v0, u1, v1, sprite),
         makeQuad(maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, 0.0F, 0.0F, -1.0F, u0, v0, u1, v1, sprite),
         makeQuad(minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, 0.0F, 0.0F, 1.0F, u0, v0, u1, v1, sprite),
         makeQuad(minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ, -1.0F, 0.0F, 0.0F, u0, v0, u1, v1, sprite),
         makeQuad(maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, 1.0F, 0.0F, 0.0F, u0, v0, u1, v1, sprite)
      };
      if (dir != Direction.WEST) {
         for (MutableQuad q : quads) {
            q.rotate(Direction.WEST, dir, 0.5F, 0.5F, 0.5F);
         }
      }

      return quads;
   }

   private static MutableQuad makeQuad(
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float nx,
      float ny,
      float nz,
      float u0,
      float v0,
      float u1,
      float v1,
      TextureAtlasSprite sprite
   ) {
      MutableQuad q = new MutableQuad();
      q.vertex_0.positionf(x0, y0, z0).texf(u0, v0).normalf(nx, ny, nz).colourf(1.0F, 1.0F, 1.0F, 1.0F);
      q.vertex_1.positionf(x1, y1, z1).texf(u0, v1).normalf(nx, ny, nz).colourf(1.0F, 1.0F, 1.0F, 1.0F);
      q.vertex_2.positionf(x2, y2, z2).texf(u1, v1).normalf(nx, ny, nz).colourf(1.0F, 1.0F, 1.0F, 1.0F);
      q.vertex_3.positionf(x3, y3, z3).texf(u1, v0).normalf(nx, ny, nz).colourf(1.0F, 1.0F, 1.0F, 1.0F);
      q.setShade(false);
      q.setSprite(sprite);
      return q;
   }
}
