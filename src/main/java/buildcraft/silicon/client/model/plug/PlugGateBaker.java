package buildcraft.silicon.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class PlugGateBaker implements IPluggableStaticBaker<KeyPlugGate> {
   public static final PlugGateBaker INSTANCE = new PlugGateBaker();
   private static final Map<KeyPlugGate, List<BakedQuad>> cached = new ConcurrentHashMap<>();

   public static void onModelBake() {
      cached.clear();
   }

   private TextureAtlasSprite getSprite(String path) {
      TextureAtlas atlas = (TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
      return atlas.getSprite(Identifier.parse(path));
   }

   private String getMaterialSpritePath(EnumGateMaterial material) {
      switch (material) {
         case CLAY_BRICK:
            return "minecraft:block/bricks";
         case IRON:
            return "minecraft:block/iron_block";
         case NETHER_BRICK:
            return "minecraft:block/nether_bricks";
         case GOLD:
            return "minecraft:block/gold_block";
         default:
            return "minecraft:block/bricks";
      }
   }

   private String getModifierSpritePath(EnumGateModifier mod) {
      switch (mod) {
         case LAPIS:
            return "minecraft:block/lapis_block";
         case QUARTZ:
            return "minecraft:block/quartz_block_top";
         case DIAMOND:
            return "minecraft:block/diamond_block";
         default:
            return "minecraft:block/stone";
      }
   }

   public List<BakedQuad> bake(KeyPlugGate key) {
      return Collections.emptyList();
   }

   public List<MutableQuad> bakeForItem(GateVariant variant) {
      List<MutableQuad> quads = new ArrayList<>();
      float baseZMin = 0.125F;
      float baseZMax = 0.250625F;
      float baseYMin = 0.3125F;
      float baseYMax = 0.6875F;
      float baseXMin = 0.3125F;
      float baseXMax = 0.6875F;
      TextureAtlasSprite matSprite = this.getSprite(this.getMaterialSpritePath(variant.material));
      this.buildBoxMutableNorth(quads, baseXMin, baseYMin, baseZMin, baseXMax, baseYMax, baseZMax, matSprite, true);
      if (variant.material != EnumGateMaterial.CLAY_BRICK) {
         TextureAtlasSprite logicSprite = this.getSprite("buildcraftsilicon:block/gates/gate_" + variant.logic.tag);
         this.buildBoxMutableNorth(quads, 0.4375F, 0.4375F, 0.1125F, 0.5625F, 0.5625F, 0.2625F, logicSprite, false);
      }

      TextureAtlasSprite dynSprite = this.getSprite("buildcraftsilicon:block/gates/gate_off");
      this.buildBoxMutableNorth(quads, 0.375F, 0.375F, 0.11875F, 0.625F, 0.625F, 0.25625F, dynSprite, false);
      if (variant.modifier != EnumGateModifier.NO_MODIFIER) {
         TextureAtlasSprite modSprite = this.getSprite(this.getModifierSpritePath(variant.modifier));
         this.buildBoxMutableNorth(quads, 0.34375F, 0.34375F, 0.1125F, 0.40625F, 0.40625F, 0.2625F, modSprite, false);
         this.buildBoxMutableNorth(quads, 0.34375F, 0.59375F, 0.1125F, 0.40625F, 0.65625F, 0.2625F, modSprite, false);
         this.buildBoxMutableNorth(quads, 0.59375F, 0.34375F, 0.1125F, 0.65625F, 0.40625F, 0.2625F, modSprite, false);
         this.buildBoxMutableNorth(quads, 0.59375F, 0.59375F, 0.1125F, 0.65625F, 0.65625F, 0.2625F, modSprite, false);
      }

      return quads;
   }

   private void buildBox(
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
      new AABB(minX, minY, minZ, maxX, maxY, maxZ);
      Vector3f center = new Vector3f((minX + maxX) / 2.0F, (minY + maxY) / 2.0F, (minZ + maxZ) / 2.0F);
      Vector3f radius = new Vector3f(maxX - center.x, maxY - center.y, maxZ - center.z);

      for (Direction face : Direction.values()) {
         if (face != Direction.EAST) {
            ModelUtil.UvFaceData uv = makeGateUVs(face);
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

   private void buildBoxMutableNorth(
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

   private static ModelUtil.UvFaceData makeGateUVs(Direction face) {
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
