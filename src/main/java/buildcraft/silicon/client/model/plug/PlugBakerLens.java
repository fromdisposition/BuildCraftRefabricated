package buildcraft.silicon.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.silicon.client.model.key.KeyPlugLens;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import org.joml.Vector3f;

public enum PlugBakerLens implements IPluggableStaticBaker<KeyPlugLens> {
   INSTANCE;

   private static final Map<KeyPlugLens, List<BakedQuad>> cached = new HashMap<>();

   public static void onModelBake() {
      cached.clear();
   }

   private static MutableQuad makeFace(Direction face, float x0, float y0, float z0, float x1, float y1, float z1, float[] uvArr, TextureAtlasSprite sprite) {
      Vector3f center = new Vector3f((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, (z0 + z1) / 2.0F);
      Vector3f radius = new Vector3f((x1 - x0) / 2.0F, (y1 - y0) / 2.0F, (z1 - z0) / 2.0F);
      ModelUtil.UvFaceData uvs = ModelUtil.UvFaceData.from16(uvArr[0], uvArr[1], uvArr[2], uvArr[3]);
      MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
      q.setSprite(sprite);
      q.texFromSprite(sprite);
      if (uvArr.length >= 5) {
         int rotation = (int)uvArr[4];
         if (rotation != 0) {
            q.rotateTextureUp(rotation);
         }
      }

      return q;
   }

   private static void addBox(
      List<MutableQuad> quads,
      TextureAtlasSprite sprite,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float[] uvDown,
      float[] uvUp,
      float[] uvNorth,
      float[] uvSouth,
      float[] uvWest,
      float[] uvEast
   ) {
      float bx0 = x0 / 16.0F;
      float by0 = y0 / 16.0F;
      float bz0 = z0 / 16.0F;
      float bx1 = x1 / 16.0F;
      float by1 = y1 / 16.0F;
      float bz1 = z1 / 16.0F;
      quads.add(makeFace(Direction.DOWN, bx0, by0, bz0, bx1, by1, bz1, uvDown, sprite));
      quads.add(makeFace(Direction.UP, bx0, by0, bz0, bx1, by1, bz1, uvUp, sprite));
      quads.add(makeFace(Direction.NORTH, bx0, by0, bz0, bx1, by1, bz1, uvNorth, sprite));
      quads.add(makeFace(Direction.SOUTH, bx0, by0, bz0, bx1, by1, bz1, uvSouth, sprite));
      quads.add(makeFace(Direction.WEST, bx0, by0, bz0, bx1, by1, bz1, uvWest, sprite));
      quads.add(makeFace(Direction.EAST, bx0, by0, bz0, bx1, by1, bz1, uvEast, sprite));
   }

   private static void bakeCutoutQuads(List<MutableQuad> rawQuads, boolean isFilter) {
      TextureAtlasSprite sprite = SpriteUtil.getSprite(
         Identifier.fromNamespaceAndPath("buildcraftsilicon", isFilter ? "block/plugs/filter" : "block/plugs/lens")
      );
      if (sprite == null) {
         sprite = SpriteUtil.missingSprite();
      }

      addBox(
         rawQuads,
         sprite,
         0.0F,
         3.0F,
         3.0F,
         2.0F,
         4.01F,
         13.0F,
         new float[]{2.0F, 3.0F, 4.0F, 13.0F},
         new float[]{2.0F, 3.0F, 4.0F, 13.0F},
         new float[]{2.0F, 12.0F, 4.0F, 13.0F},
         new float[]{2.0F, 3.0F, 4.0F, 4.0F},
         new float[]{2.0F, 3.0F, 3.0F, 13.0F, 1.0F},
         new float[]{3.0F, 3.0F, 4.0F, 13.0F, 3.0F}
      );
      addBox(
         rawQuads,
         sprite,
         0.0F,
         11.99F,
         3.0F,
         2.0F,
         13.0F,
         13.0F,
         new float[]{12.0F, 3.0F, 14.0F, 13.0F},
         new float[]{12.0F, 3.0F, 14.0F, 13.0F},
         new float[]{12.0F, 12.0F, 14.0F, 13.0F},
         new float[]{12.0F, 3.0F, 14.0F, 4.0F},
         new float[]{12.0F, 3.0F, 13.0F, 13.0F, 1.0F},
         new float[]{13.0F, 3.0F, 14.0F, 13.0F, 3.0F}
      );
      addBox(
         rawQuads,
         sprite,
         0.0F,
         4.01F,
         3.0F,
         2.0F,
         11.99F,
         4.01F,
         new float[]{12.0F, 2.0F, 13.0F, 4.0F, 1.0F},
         new float[]{3.0F, 2.0F, 4.0F, 4.0F, 1.0F},
         new float[]{3.0F, 2.0F, 13.0F, 4.0F, 1.0F},
         new float[]{3.0F, 2.0F, 13.0F, 4.0F, 3.0F},
         new float[]{3.0F, 2.0F, 13.0F, 3.0F, 3.0F},
         new float[]{3.0F, 3.0F, 13.0F, 4.0F, 1.0F}
      );
      addBox(
         rawQuads,
         sprite,
         0.0F,
         4.01F,
         11.99F,
         2.0F,
         11.99F,
         13.0F,
         new float[]{3.0F, 12.0F, 4.0F, 14.0F, 1.0F},
         new float[]{12.0F, 12.0F, 13.0F, 14.0F, 1.0F},
         new float[]{3.0F, 12.0F, 13.0F, 14.0F, 1.0F},
         new float[]{3.0F, 12.0F, 13.0F, 14.0F, 3.0F},
         new float[]{3.0F, 12.0F, 13.0F, 13.0F, 3.0F},
         new float[]{3.0F, 13.0F, 13.0F, 14.0F, 1.0F}
      );
      if (isFilter) {
         addBox(
            rawQuads,
            sprite,
            0.0F,
            4.01F,
            6.0F,
            2.0F,
            11.99F,
            7.01F,
            new float[]{12.0F, 12.0F, 13.0F, 14.0F, 1.0F},
            new float[]{3.0F, 12.0F, 4.0F, 14.0F, 1.0F},
            new float[]{3.0F, 12.0F, 13.0F, 14.0F, 1.0F},
            new float[]{3.0F, 12.0F, 13.0F, 14.0F, 3.0F},
            new float[]{3.0F, 12.0F, 13.0F, 13.0F, 3.0F},
            new float[]{3.0F, 13.0F, 13.0F, 14.0F, 1.0F}
         );
         addBox(
            rawQuads,
            sprite,
            0.0F,
            4.01F,
            9.0F,
            2.0F,
            11.99F,
            10.01F,
            new float[]{12.0F, 2.0F, 13.0F, 4.0F, 1.0F},
            new float[]{3.0F, 2.0F, 4.0F, 4.0F, 1.0F},
            new float[]{3.0F, 2.0F, 13.0F, 4.0F, 1.0F},
            new float[]{3.0F, 2.0F, 13.0F, 4.0F, 3.0F},
            new float[]{3.0F, 2.0F, 13.0F, 3.0F, 3.0F},
            new float[]{3.0F, 3.0F, 13.0F, 4.0F, 1.0F}
         );
      }
   }

   private static void bakeTranslucentQuads(List<MutableQuad> rawQuads, @Nullable DyeColor colour, boolean isFilter) {
      if (!isFilter || colour != null) {
         TextureAtlasSprite sprite;
         if (colour != null) {
            sprite = SpriteUtil.getSprite(Identifier.fromNamespaceAndPath("buildcraftsilicon", "block/plugs/overlay_lens"));
         } else {
            sprite = SpriteUtil.getSprite(Identifier.fromNamespaceAndPath("minecraft", "block/water_flow"));
         }

         if (sprite == null) {
            sprite = SpriteUtil.missingSprite();
         }

         float bx0 = 0.03125F;
         float by0 = 0.25F;
         float bz0 = 0.25F;
         float bx1 = 0.09375F;
         float by1 = 0.75F;
         float bz1 = 0.75F;
         rawQuads.add(makeFace(Direction.EAST, bx0, by0, bz0, bx1, by1, bz1, new float[]{6.0F, 6.0F, 10.0F, 10.0F}, sprite));
         rawQuads.add(makeFace(Direction.WEST, bx0, by0, bz0, bx1, by1, bz1, new float[]{6.0F, 6.0F, 10.0F, 10.0F}, sprite));
         if (colour != null) {
            int argb = colour.getTextureDiffuseColor();
            int r = argb >> 16 & 0xFF;
            int g = argb >> 8 & 0xFF;
            int b = argb & 0xFF;

            for (MutableQuad q : rawQuads) {
               q.colouri(r, g, b, 255);
            }
         } else {
            for (MutableQuad q : rawQuads) {
               q.colouri(63, 118, 228, 255);
            }
         }
      }
   }

   public static List<MutableQuad> bakeForItem(@Nullable DyeColor colour, boolean isFilter, boolean cutout) {
      List<MutableQuad> rawQuads = new ArrayList<>();
      if (cutout) {
         bakeCutoutQuads(rawQuads, isFilter);
      } else {
         bakeTranslucentQuads(rawQuads, colour, isFilter);
      }

      return rawQuads;
   }

   public List<BakedQuad> bake(KeyPlugLens key) {
      if (!cached.containsKey(key)) {
         List<MutableQuad> rawQuads = new ArrayList<>();
         String layerName = key.layer != null ? key.layer.toString().toLowerCase() : "";
         List<BakedQuad> baked = new ArrayList<>();
         if (layerName.contains("cutout")) {
            bakeCutoutQuads(rawQuads, key.isFilter);

            for (MutableQuad q : rawQuads) {
               q.rotate(Direction.WEST, key.side, 0.5F, 0.5F, 0.5F);
               q.multShade();
               baked.add(q.toBakedBlock());
            }
         } else if (layerName.contains("translucent")) {
            bakeTranslucentQuads(rawQuads, key.colour, key.isFilter);

            for (MutableQuad q : rawQuads) {
               q.rotate(Direction.WEST, key.side, 0.5F, 0.5F, 0.5F);
               baked.add(q.toBakedTranslucent());
            }
         }

         cached.put(key, baked);
      }

      return cached.get(key);
   }
}
