/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportSprites;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import org.joml.Vector3f;

public final class PipeItemColourQuads {
   private static final Map<DyeColor, MutableQuad[]> BY_COLOUR = new EnumMap<>(DyeColor.class);

   private PipeItemColourQuads() {
   }

   public static MutableQuad[] get(DyeColor colour) {
      return BY_COLOUR.computeIfAbsent(colour, PipeItemColourQuads::bake);
   }

   private static MutableQuad[] bake(DyeColor colour) {
      MutableQuad[] quads = new MutableQuad[6];
      Vector3f center = new Vector3f();
      Vector3f radius = new Vector3f(0.2F, 0.2F, 0.2F);
      SpriteHolderRegistry.SpriteHolder sprite = BCTransportSprites.COLOUR_ITEM_BOX;
      ModelUtil.UvFaceData uvs = new ModelUtil.UvFaceData();
      uvs.minU = (float)sprite.getInterpU(0.0);
      uvs.maxU = (float)sprite.getInterpU(1.0);
      uvs.minV = (float)sprite.getInterpV(0.0);
      uvs.maxV = (float)sprite.getInterpV(1.0);
      int col = ColourUtil.getLightHex(colour);
      int r = col >> 16 & 0xFF;
      int g = col >> 8 & 0xFF;
      int b = col & 0xFF;

      for (Direction face : Direction.values()) {
         MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
         quad.setCalculatedDiffuse();
         quad.multColouri(r, g, b, 255);
         quads[face.ordinal()] = quad;
      }

      return quads;
   }
}
