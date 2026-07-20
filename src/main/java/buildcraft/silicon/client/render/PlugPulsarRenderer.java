/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.render;

import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.silicon.plug.PluggablePulsar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public enum PlugPulsarRenderer implements IPlugDynamicRenderer<PluggablePulsar> {
   INSTANCE;

   private static final float STAGE_MAX = PluggablePulsar.PULSE_STAGE;
   private static final int LED_ON_R = 153;
   private static final int LED_ON_G = 255;
   private static final int LED_ON_B = 153;
   private static final int LED_OFF = 34;
   private static List<MutableQuad> offBox;
   private static List<MutableQuad> onBox;
   private static List<MutableQuad> autoLeds;
   private static List<MutableQuad> manualLeds;

   /** Drops the sprite-mapped geometry so the next render rebuilds it against the freshly stitched atlas. */
   public static void onModelBake() {
      offBox = null;
      onBox = null;
      autoLeds = null;
      manualLeds = null;
   }

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
      ModelUtil.addSpriteBox(quads, sprite, x0, y0, z0, x1, y1, z1, faceUvs);
   }

   public void render(PluggablePulsar plug, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack ps) {
      initCache();
      boolean on = plug.getIsPulsingClient();
      int stage = plug.getPulseStageClient();
      float fraction = (stage + (on ? partialTicks : 0.0F)) / STAGE_MAX;
      if (fraction > 1.0F) {
         fraction = 1.0F;
      }

      float mirroredStage = fraction > 0.5F ? 1.0F - fraction : fraction;
      float mirroredPos = 2.0F * mirroredStage;
      float posDiff = (1.0F - mirroredPos) * 2.0F - 0.001F;
      ps.pushPose();
      ps.translate(x, y, z);
      IPlugDynamicRenderer.rotateToSide(ps, plug.side);
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
      renderLeds(autoLeds, plug.getAutoEnabledClient() && on, ps, bb);
      renderLeds(manualLeds, plug.getManuallyEnabledClient(), ps, bb);
      ps.popPose();
   }

   private static void renderLeds(List<MutableQuad> leds, boolean lit, PoseStack ps, VertexConsumer bb) {
      int r = lit ? LED_ON_R : LED_OFF;
      int g = lit ? LED_ON_G : LED_OFF;
      int b = lit ? LED_ON_B : LED_OFF;

      for (MutableQuad q : leds) {
         MutableQuad mq = new MutableQuad(q);
         mq.multColouri(r, g, b, 255);
         if (lit) {
            mq.lighti(15, 15);
         }

         mq.render(ps.last(), bb);
      }
   }
}
