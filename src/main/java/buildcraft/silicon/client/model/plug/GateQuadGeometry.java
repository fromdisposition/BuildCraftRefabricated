package buildcraft.silicon.client.model.plug;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class GateQuadGeometry {
   private GateQuadGeometry() {
   }

   public static String materialSpritePath(EnumGateMaterial material) {
      return switch (material) {
         case CLAY_BRICK -> "minecraft:block/bricks";
         case IRON -> "minecraft:block/iron_block";
         case NETHER_BRICK -> "minecraft:block/nether_bricks";
         case GOLD -> "minecraft:block/gold_block";
      };
   }

   public static String modifierSpritePath(EnumGateModifier mod) {
      return switch (mod) {
         case LAPIS -> "minecraft:block/lapis_block";
         case QUARTZ -> "minecraft:block/quartz_block_top";
         case DIAMOND -> "minecraft:block/diamond_block";
         default -> "minecraft:block/stone";
      };
   }

   public static void appendStaticWestFacing(List<MutableQuad> list, GateVariant variant, Function<String, TextureAtlasSprite> sprites, boolean shade) {
      TextureAtlasSprite matSprite = sprites.apply(materialSpritePath(variant.material));
      addWestFacingBox(list, 0.125F, 0.3125F, 0.3125F, 0.250625F, 0.6875F, 0.6875F, matSprite, shade);
      if (variant.material != EnumGateMaterial.CLAY_BRICK) {
         TextureAtlasSprite logicSprite = sprites.apply("buildcraftsilicon:block/gates/gate_" + variant.logic.tag);
         addWestFacingBox(list, 0.1125F, 0.4375F, 0.4375F, 0.2625F, 0.5625F, 0.5625F, logicSprite, shade);
      }

      if (variant.modifier != EnumGateModifier.NO_MODIFIER) {
         TextureAtlasSprite modSprite = sprites.apply(modifierSpritePath(variant.modifier));
         addWestFacingBox(list, 0.1125F, 0.34375F, 0.34375F, 0.2625F, 0.40625F, 0.40625F, modSprite, shade);
         addWestFacingBox(list, 0.1125F, 0.59375F, 0.34375F, 0.2625F, 0.65625F, 0.40625F, modSprite, shade);
         addWestFacingBox(list, 0.1125F, 0.34375F, 0.59375F, 0.2625F, 0.40625F, 0.65625F, modSprite, shade);
         addWestFacingBox(list, 0.1125F, 0.59375F, 0.59375F, 0.2625F, 0.65625F, 0.65625F, modSprite, shade);
      }
   }

   public static void appendStaticBaked(
      List<BakedQuad> list, GateVariant variant, Direction side, Function<String, TextureAtlasSprite> sprites, boolean shade, int light
   ) {
      TextureAtlasSprite matSprite = sprites.apply(materialSpritePath(variant.material));
      addRotatedBakedBox(list, 0.125F, 0.3125F, 0.3125F, 0.250625F, 0.6875F, 0.6875F, matSprite, side, shade, light);
      if (variant.material != EnumGateMaterial.CLAY_BRICK) {
         TextureAtlasSprite logicSprite = sprites.apply("buildcraftsilicon:block/gates/gate_" + variant.logic.tag);
         addRotatedBakedBox(list, 0.1125F, 0.4375F, 0.4375F, 0.2625F, 0.5625F, 0.5625F, logicSprite, side, shade, light);
      }

      if (variant.modifier != EnumGateModifier.NO_MODIFIER) {
         TextureAtlasSprite modSprite = sprites.apply(modifierSpritePath(variant.modifier));
         addRotatedBakedBox(list, 0.1125F, 0.34375F, 0.34375F, 0.2625F, 0.40625F, 0.40625F, modSprite, side, shade, light);
         addRotatedBakedBox(list, 0.1125F, 0.59375F, 0.34375F, 0.2625F, 0.65625F, 0.40625F, modSprite, side, shade, light);
         addRotatedBakedBox(list, 0.1125F, 0.34375F, 0.59375F, 0.2625F, 0.40625F, 0.65625F, modSprite, side, shade, light);
         addRotatedBakedBox(list, 0.1125F, 0.59375F, 0.59375F, 0.2625F, 0.65625F, 0.65625F, modSprite, side, shade, light);
      }
   }

   public static void appendItemNorthFacing(List<MutableQuad> list, GateVariant variant, Function<String, TextureAtlasSprite> sprites, boolean shade) {
      float baseZMin = 0.125F;
      float baseZMax = 0.250625F;
      float baseYMin = 0.3125F;
      float baseYMax = 0.6875F;
      float baseXMin = 0.3125F;
      float baseXMax = 0.6875F;
      TextureAtlasSprite matSprite = sprites.apply(materialSpritePath(variant.material));
      addItemNorthBox(list, baseXMin, baseYMin, baseZMin, baseXMax, baseYMax, baseZMax, matSprite, shade);
      if (variant.material != EnumGateMaterial.CLAY_BRICK) {
         TextureAtlasSprite logicSprite = sprites.apply("buildcraftsilicon:block/gates/gate_" + variant.logic.tag);
         addItemNorthBox(list, 0.4375F, 0.4375F, 0.1125F, 0.5625F, 0.5625F, 0.2625F, logicSprite, false);
      }

      TextureAtlasSprite dynSprite = sprites.apply("buildcraftsilicon:block/gates/gate_off");
      addItemNorthBox(list, 0.375F, 0.375F, 0.11875F, 0.625F, 0.625F, 0.25625F, dynSprite, false);
      if (variant.modifier != EnumGateModifier.NO_MODIFIER) {
         TextureAtlasSprite modSprite = sprites.apply(modifierSpritePath(variant.modifier));
         addItemNorthBox(list, 0.34375F, 0.34375F, 0.1125F, 0.40625F, 0.40625F, 0.2625F, modSprite, false);
         addItemNorthBox(list, 0.34375F, 0.59375F, 0.1125F, 0.40625F, 0.65625F, 0.2625F, modSprite, false);
         addItemNorthBox(list, 0.59375F, 0.34375F, 0.1125F, 0.65625F, 0.40625F, 0.2625F, modSprite, false);
         addItemNorthBox(list, 0.59375F, 0.59375F, 0.1125F, 0.65625F, 0.65625F, 0.2625F, modSprite, false);
      }
   }

   public static void addWestFacingBox(
      List<MutableQuad> list, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite sprite, boolean shade
   ) {
      Vector3f center = new Vector3f((minX + maxX) / 2.0F, (minY + maxY) / 2.0F, (minZ + maxZ) / 2.0F);
      Vector3f radius = new Vector3f((maxX - minX) / 2.0F, (maxY - minY) / 2.0F, (maxZ - minZ) / 2.0F);

      for (Direction face : Direction.values()) {
         ModelUtil.UvFaceData uv = makeGateBlockUVs(face);
         MutableQuad q = ModelUtil.createFace(face, center, radius, uv);
         q.texFromSprite(sprite);
         q.setTint(-1);
         q.setShade(shade);
         list.add(q);
      }
   }

   public static void addRotatedBakedBox(
      List<BakedQuad> list,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      TextureAtlasSprite sprite,
      Direction targetSide,
      boolean shade,
      int light
   ) {
      Vector3f center = new Vector3f((minX + maxX) / 2.0F, (minY + maxY) / 2.0F, (minZ + maxZ) / 2.0F);
      Vector3f radius = new Vector3f(maxX - center.x, maxY - center.y, maxZ - center.z);

      for (Direction face : Direction.values()) {
         if (face != Direction.EAST) {
            ModelUtil.UvFaceData uv = makeGateBlockUVs(face);
            MutableQuad q = ModelUtil.createFace(face, center, radius, uv);
            q.texFromSprite(sprite);
            q.setTint(-1);
            q.setShade(shade);
            if (light > 0) {
               q.setLightEmission(light);
            }

            q.rotate(Direction.WEST, targetSide, 0.5F, 0.5F, 0.5F);
            q.setCalculatedNormal();
            q.setCalculatedDiffuse();
            list.add(q.toBakedBlock());
         }
      }
   }

   private static void addItemNorthBox(
      List<MutableQuad> list, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite sprite, boolean shade
   ) {
      Vector3f center = new Vector3f((minX + maxX) / 2.0F, (minY + maxY) / 2.0F, (minZ + maxZ) / 2.0F);
      Vector3f radius = new Vector3f(maxX - center.x, maxY - center.y, maxZ - center.z);

      for (Direction face : Direction.values()) {
         ModelUtil.UvFaceData uv = makeGateItemUVs(face);
         MutableQuad q = ModelUtil.createFace(face, center, radius, uv);
         q.texFromSprite(sprite);
         q.setTint(-1);
         q.setShade(shade);
         q.setCalculatedNormal();
         list.add(q);
      }
   }

   public static ModelUtil.UvFaceData makeGateBlockUVs(Direction face) {
      ModelUtil.UvFaceData uv = new ModelUtil.UvFaceData();
      if (face != Direction.WEST && face != Direction.EAST) {
         uv.minU = 0.125F;
         uv.maxU = 0.25F;
         uv.minV = 0.3125F;
         uv.maxV = 0.6875F;
      } else {
         uv.minU = 0.3125F;
         uv.maxU = 0.6875F;
         uv.minV = 0.3125F;
         uv.maxV = 0.6875F;
      }

      return uv;
   }

   private static ModelUtil.UvFaceData makeGateItemUVs(Direction face) {
      ModelUtil.UvFaceData uv = new ModelUtil.UvFaceData();
      if (face == Direction.NORTH || face == Direction.SOUTH) {
         uv.minU = 0.3125F;
         uv.maxU = 0.6875F;
         uv.minV = 0.3125F;
         uv.maxV = 0.6875F;
      } else if (face != Direction.WEST && face != Direction.EAST) {
         uv.minU = 0.3125F;
         uv.maxU = 0.6875F;
         uv.minV = 0.125F;
         uv.maxV = 0.25F;
      } else {
         uv.minU = 0.125F;
         uv.maxU = 0.25F;
         uv.minV = 0.3125F;
         uv.maxV = 0.6875F;
      }

      return uv;
   }
}
