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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public enum PlugGateRenderer implements IPlugDynamicRenderer<PluggableGate> {
   INSTANCE;

   private static final ThreadLocal<MutableQuad> RENDER_SCRATCH = ThreadLocal.withInitial(MutableQuad::new);
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
      if (!on && plug.holder != null && plug.holder.getPipeWorld() != null) {
         Level world = plug.holder.getPipeWorld();
         BlockPos sample = plug.holder.getPipePos().relative(plug.side);
         naturalBlockLight = world.getBrightness(LightLayer.BLOCK, sample);
         naturalSkyLight = world.getBrightness(LightLayer.SKY, sample);
      }

      ps.pushPose();
      ps.translate(x, y, z);
      IPlugDynamicRenderer.rotateToSide(ps, plug.side);

      // Only the on/off indicator is drawn here; the static body comes from PlugGateBaker to avoid Z-fighting.
      MutableQuad scratch = RENDER_SCRATCH.get();

      for (MutableQuad q : on ? onBox : offBox) {
         scratch.copyFrom(q);
         if (on) {
            scratch.lighti(15, 15);
         } else {
            scratch.lighti(naturalBlockLight, naturalSkyLight);
         }

         scratch.render(ps.last(), bb);
      }

      ps.popPose();
   }
}
