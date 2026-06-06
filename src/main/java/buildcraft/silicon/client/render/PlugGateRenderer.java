package buildcraft.silicon.client.render;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.plug.PluggableGate;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.joml.Vector3f;

public enum PlugGateRenderer implements IPlugDynamicRenderer<PluggableGate> {
   INSTANCE;

   private static List<MutableQuad> onBox;
   private static List<MutableQuad> offBox;
   private static final Map<GateVariant, List<MutableQuad>> staticByVariant = new ConcurrentHashMap<>();

   private static void initDynamicCache() {
      if (onBox == null) {
         onBox = new ArrayList<>();
         offBox = new ArrayList<>();
         TextureAtlasSprite onSprite = getMcSprite("buildcraftsilicon:block/gates/gate_on");
         TextureAtlasSprite offSprite = getMcSprite("minecraft:block/black_concrete");
         addDynamicBox(onBox, onSprite);
         addDynamicBox(offBox, offSprite);
      }
   }

   private static List<MutableQuad> staticQuadsFor(GateVariant variant) {
      List<MutableQuad> cached = staticByVariant.get(variant);
      if (cached != null) {
         return cached;
      }

      List<MutableQuad> list = buildStaticQuads(variant);
      staticByVariant.put(variant, list);
      return list;
   }

   private static List<MutableQuad> buildStaticQuads(GateVariant variant) {
      List<MutableQuad> list = new ArrayList<>();
      TextureAtlasSprite matSprite = getMcSprite(materialSpritePath(variant.material));
      addBox(list, 0.125F, 0.3125F, 0.3125F, 0.250625F, 0.6875F, 0.6875F, matSprite, true);
      if (variant.material != EnumGateMaterial.CLAY_BRICK) {
         TextureAtlasSprite logicSprite = getMcSprite("buildcraftsilicon:block/gates/gate_" + variant.logic.tag);
         addBox(list, 0.1125F, 0.4375F, 0.4375F, 0.2625F, 0.5625F, 0.5625F, logicSprite, true);
      }

      if (variant.modifier != EnumGateModifier.NO_MODIFIER) {
         TextureAtlasSprite modSprite = getMcSprite(modifierSpritePath(variant.modifier));
         addBox(list, 0.1125F, 0.34375F, 0.34375F, 0.2625F, 0.40625F, 0.40625F, modSprite, true);
         addBox(list, 0.1125F, 0.59375F, 0.34375F, 0.2625F, 0.65625F, 0.40625F, modSprite, true);
         addBox(list, 0.1125F, 0.34375F, 0.59375F, 0.2625F, 0.40625F, 0.65625F, modSprite, true);
         addBox(list, 0.1125F, 0.59375F, 0.59375F, 0.2625F, 0.65625F, 0.65625F, modSprite, true);
      }

      return list;
   }

   private static String materialSpritePath(EnumGateMaterial material) {
      return switch (material) {
         case CLAY_BRICK -> "minecraft:block/bricks";
         case IRON -> "minecraft:block/iron_block";
         case NETHER_BRICK -> "minecraft:block/nether_bricks";
         case GOLD -> "minecraft:block/gold_block";
      };
   }

   private static String modifierSpritePath(EnumGateModifier mod) {
      return switch (mod) {
         case LAPIS -> "minecraft:block/lapis_block";
         case QUARTZ -> "minecraft:block/quartz_block_top";
         case DIAMOND -> "minecraft:block/diamond_block";
         default -> "minecraft:block/stone";
      };
   }

   private static TextureAtlasSprite getMcSprite(String path) {
      TextureAtlas atlas = (TextureAtlas)Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
      TextureAtlasSprite sprite = atlas.getSprite(Identifier.parse(path));
      return sprite != null ? sprite : SpriteUtil.missingSprite();
   }

   private static void addBox(
      List<MutableQuad> list, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite sprite, boolean shade
   ) {
      Vector3f center = new Vector3f((minX + maxX) / 2.0F, (minY + maxY) / 2.0F, (minZ + maxZ) / 2.0F);
      Vector3f radius = new Vector3f((maxX - minX) / 2.0F, (maxY - minY) / 2.0F, (maxZ - minZ) / 2.0F);

      for (Direction face : Direction.values()) {
         ModelUtil.UvFaceData uv = makeGateUVs(face);
         MutableQuad q = ModelUtil.createFace(face, center, radius, uv);
         q.texFromSprite(sprite);
         q.setTint(-1);
         q.setShade(shade);
         list.add(q);
      }
   }

   private static void addDynamicBox(List<MutableQuad> list, TextureAtlasSprite sprite) {
      addBox(list, 0.11875F, 0.375F, 0.375F, 0.25625F, 0.625F, 0.625F, sprite, false);
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

   public static void onModelBake() {
      onBox = null;
      offBox = null;
      staticByVariant.clear();
   }

   public void render(PluggableGate plug, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack ps) {
      initDynamicCache();
      int naturalBlockLight = 0;
      int naturalSkyLight = 0;
      boolean on = plug.logic.isOn;
      if (plug.holder != null && plug.holder.getPipeWorld() != null) {
         Level world = plug.holder.getPipeWorld();
         BlockPos sample = plug.holder.getPipePos().relative(plug.side);
         naturalBlockLight = world.getBrightness(LightLayer.BLOCK, sample);
         naturalSkyLight = world.getBrightness(LightLayer.SKY, sample);
      }

      ps.pushPose();
      ps.translate(x, y, z);
      ps.translate(0.5F, 0.5F, 0.5F);
      switch (plug.side) {
         case EAST:
            ps.mulPose(Axis.YP.rotationDegrees(180.0F));
            break;
         case NORTH:
            ps.mulPose(Axis.YP.rotationDegrees(-90.0F));
            break;
         case SOUTH:
            ps.mulPose(Axis.YP.rotationDegrees(90.0F));
            break;
         case DOWN:
            ps.mulPose(Axis.ZP.rotationDegrees(90.0F));
            break;
         case UP:
            ps.mulPose(Axis.ZP.rotationDegrees(-90.0F));
         case WEST:
      }

      ps.translate(-0.5F, -0.5F, -0.5F);

      for (MutableQuad q : staticQuadsFor(plug.logic.variant)) {
         MutableQuad mq = new MutableQuad(q);
         mq.lighti(naturalBlockLight, naturalSkyLight);
         mq.render(ps.last(), bb);
      }

      for (MutableQuad q : on ? onBox : offBox) {
         MutableQuad mq = new MutableQuad(q);
         if (on) {
            mq.lighti(15, 15);
         } else {
            mq.lighti(naturalBlockLight, naturalSkyLight);
         }

         mq.render(ps.last(), bb);
      }

      ps.popPose();
   }
}
