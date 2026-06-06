package buildcraft.silicon.client.render;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.silicon.plug.PluggablePulsar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public enum PlugPulsarRenderer implements IPlugDynamicRenderer<PluggablePulsar> {
   INSTANCE;

   private static final float STAGE_MAX = 20.0F;
   private static List<MutableQuad> offBox;
   private static List<MutableQuad> onBox;
   private static List<MutableQuad> autoLeds;
   private static List<MutableQuad> manualLeds;

   public static void initCache() {
      if (offBox == null) {
         offBox = new ArrayList<>();
         onBox = new ArrayList<>();
         autoLeds = new ArrayList<>();
         manualLeds = new ArrayList<>();
         TextureAtlasSprite offSprite = SpriteUtil.getSprite("buildcraftsilicon:block/plugs/pulsar_dynamic_off");
         TextureAtlasSprite onSprite = SpriteUtil.getSprite("buildcraftsilicon:block/plugs/pulsar_dynamic_on");
         ModelUtil.UvFaceData[] offUvs = new ModelUtil.UvFaceData[]{
            ModelUtil.UvFaceData.from16(4.0, 6.0, 6.0, 10.0),
            ModelUtil.UvFaceData.from16(4.0, 6.0, 6.0, 10.0),
            ModelUtil.UvFaceData.from16(4.0, 6.0, 6.0, 10.0),
            ModelUtil.UvFaceData.from16(4.0, 6.0, 6.0, 10.0),
            ModelUtil.UvFaceData.from16(6.0, 6.0, 10.0, 10.0),
            ModelUtil.UvFaceData.from16(6.0, 6.0, 10.0, 10.0)
         };
         addBox(offBox, offSprite, 0.0F, 0.375F, 0.375F, 0.125F, 0.625F, 0.625F, offUvs);
         ModelUtil.UvFaceData[] onUvs = new ModelUtil.UvFaceData[]{
            ModelUtil.UvFaceData.from16(4.0, 6.0, 6.0, 10.0),
            ModelUtil.UvFaceData.from16(4.0, 6.0, 6.0, 10.0),
            ModelUtil.UvFaceData.from16(4.0, 6.0, 6.0, 10.0),
            ModelUtil.UvFaceData.from16(4.0, 6.0, 6.0, 10.0),
            ModelUtil.UvFaceData.from16(6.0, 6.0, 10.0, 10.0),
            ModelUtil.UvFaceData.from16(6.0, 6.0, 10.0, 10.0)
         };
         addBox(onBox, onSprite, 0.0F, 0.375F, 0.375F, 0.125F, 0.625F, 0.625F, onUvs);
         TextureAtlasSprite blankSprite = SpriteUtil.getSprite("minecraft:block/white_concrete");
         addBox(autoLeds, blankSprite, 0.15625F, 0.40625F, 0.30625F, 0.21875F, 0.46875F, 0.3125F, null);
         addBox(autoLeds, blankSprite, 0.15625F, 0.53125F, 0.6875F, 0.21875F, 0.59375F, 0.69375F, null);
         addBox(autoLeds, blankSprite, 0.15625F, 0.30625F, 0.53125F, 0.21875F, 0.3125F, 0.59375F, null);
         addBox(autoLeds, blankSprite, 0.15625F, 0.6875F, 0.40625F, 0.21875F, 0.69375F, 0.46875F, null);
         addBox(manualLeds, blankSprite, 0.15625F, 0.53125F, 0.30625F, 0.21875F, 0.59375F, 0.3125F, null);
         addBox(manualLeds, blankSprite, 0.15625F, 0.40625F, 0.6875F, 0.21875F, 0.46875F, 0.69375F, null);
         addBox(manualLeds, blankSprite, 0.15625F, 0.30625F, 0.40625F, 0.21875F, 0.3125F, 0.46875F, null);
         addBox(manualLeds, blankSprite, 0.15625F, 0.6875F, 0.53125F, 0.21875F, 0.69375F, 0.59375F, null);
      }
   }

   private static void addBox(
      List<MutableQuad> quads, TextureAtlasSprite sprite, float x0, float y0, float z0, float x1, float y1, float z1, ModelUtil.UvFaceData[] faceUvs
   ) {
      Vector3f center = new Vector3f((x0 + x1) / 2.0F, (y0 + y1) / 2.0F, (z0 + z1) / 2.0F);
      Vector3f radius = new Vector3f((x1 - x0) / 2.0F, (y1 - y0) / 2.0F, (z1 - z0) / 2.0F);

      for (Direction face : Direction.values()) {
         ModelUtil.UvFaceData uvs = faceUvs != null ? faceUvs[face.ordinal()] : new ModelUtil.UvFaceData();
         if (faceUvs == null) {
            ModelUtil.mapBoxToUvs(new AABB(x0, y0, z0, x1, y1, z1), face, uvs);
         }

         MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
         q.setSprite(sprite);
         q.vertex_0.texFromSprite(sprite);
         q.vertex_1.texFromSprite(sprite);
         q.vertex_2.texFromSprite(sprite);
         q.vertex_3.texFromSprite(sprite);
         quads.add(q);
      }
   }

   public void render(PluggablePulsar plug, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack ps) {
      initCache();
      boolean on = plug.getIsPulsingClient();
      int stage = plug.getPulseStageClient();
      float fraction = (stage + (on ? partialTicks : 0.0F)) / 20.0F;
      if (fraction > 1.0F) {
         fraction = 1.0F;
      }

      float mirroredStage = fraction > 0.5F ? 1.0F - fraction : fraction;
      float mirroredPos = 2.0F * mirroredStage;
      float posDiff = (1.0F - mirroredPos) * 2.0F - 0.001F;
      ps.pushPose();
      ps.translate(x, y, z);
      ps.translate(0.5F, 0.5F, 0.5F);
      Direction side = plug.side;
      switch (side) {
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
      ps.pushPose();
      ps.translate(posDiff / 16.0F, 0.0F, 0.0F);

      for (MutableQuad q : on ? onBox : offBox) {
         MutableQuad mq = new MutableQuad(q);
         if (on) {
            mq.lighti(15, 15);
         }

         mq.render(ps.last(), bb);
      }

      ps.popPose();
      boolean autoOn = plug.getAutoEnabledClient() && on;
      int autoR = autoOn ? 153 : 34;
      int autoG = autoOn ? 255 : 34;
      int autoB = autoOn ? 153 : 34;

      for (MutableQuad q : autoLeds) {
         MutableQuad mq = new MutableQuad(q);
         mq.multColouri(autoR, autoG, autoB, 255);
         if (autoOn) {
            mq.lighti(15, 15);
         }

         mq.render(ps.last(), bb);
      }

      boolean manOn = plug.getManuallyEnabledClient();
      int manR = manOn ? 153 : 34;
      int manG = manOn ? 255 : 34;
      int manB = manOn ? 153 : 34;

      for (MutableQuad q : manualLeds) {
         MutableQuad mq = new MutableQuad(q);
         mq.multColouri(manR, manG, manB, 255);
         if (manOn) {
            mq.lighti(15, 15);
         }

         mq.render(ps.last(), bb);
      }

      ps.popPose();
   }
}
