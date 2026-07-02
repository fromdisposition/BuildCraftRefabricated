/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.render;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.silicon.client.model.plug.GateQuadGeometry;
import buildcraft.silicon.plug.PluggableGate;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public enum PlugGateRenderer implements IPlugDynamicRenderer<PluggableGate> {
   INSTANCE;

   private static List<MutableQuad> onBox;
   private static List<MutableQuad> offBox;

   private static void initDynamicCache() {
      if (onBox == null) {
         onBox = new ArrayList<>();
         offBox = new ArrayList<>();
         TextureAtlasSprite onSprite = getMcSprite("buildcraftsilicon:block/gates/gate_on");
         TextureAtlasSprite offSprite = getMcSprite("minecraft:block/black_concrete");
         GateQuadGeometry.addWestFacingBox(onBox, 0.11875F, 0.375F, 0.375F, 0.25625F, 0.625F, 0.625F, onSprite, false);
         GateQuadGeometry.addWestFacingBox(offBox, 0.11875F, 0.375F, 0.375F, 0.25625F, 0.625F, 0.625F, offSprite, false);
      }
   }

   private static TextureAtlasSprite getMcSprite(String path) {
      TextureAtlasSprite sprite = BcTextureAtlases.getBlockSprite(Identifier.parse(path));
      return sprite != null ? sprite : SpriteUtil.missingSprite();
   }

   public static void onModelBake() {
      onBox = null;
      offBox = null;
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

      // Only the animated on/off indicator box is drawn here; the static gate body comes from the baked cutout
      // path (PlugGateBaker). Drawing both here and in the baker was the Z-fighting cause, now removed.
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
