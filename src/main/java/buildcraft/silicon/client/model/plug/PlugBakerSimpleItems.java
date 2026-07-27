/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.silicon.client.model.key.KeyPlugSimple;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public enum PlugBakerSimpleItems implements IPluggableStaticBaker<KeyPlugSimple> {
   INSTANCE;

   private static final Map<KeyPlugSimple, List<BakedQuad>> cached = new ConcurrentHashMap<>();

   public static void onModelBake() {
      cached.clear();
   }

   public List<BakedQuad> bake(KeyPlugSimple key) {
      if (!cached.containsKey(key)) {
         List<MutableQuad> rawQuads = new ArrayList<>();
         String layerName = key.layer != null ? key.layer.toString().toLowerCase() : "";
         if (layerName.contains("cutout")) {
            String texturePath = "block/plugs/" + key.identifier;
            if (key.identifier.equals("pulsar")) {
               texturePath = "block/plugs/pulsar_static";
            }

            TextureAtlasSprite sprite = SpriteUtil.getSprite(Identifier.fromNamespaceAndPath("buildcraftsilicon", texturePath));
            if (sprite == null) {
               sprite = SpriteUtil.missingSprite();
            }

            ModelUtil.UvFaceData[] explicitUvs;
            if (key.identifier.equals("pulsar")) {
               explicitUvs = new ModelUtil.UvFaceData[]{
                  ModelUtil.UvFaceData.from16(2, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(2, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(2, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(2, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(5, 5, 11, 11),
                  ModelUtil.UvFaceData.from16(5, 5, 11, 11)
               };
            } else {
               explicitUvs = new ModelUtil.UvFaceData[]{
                  ModelUtil.UvFaceData.from16(3, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(3, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(3, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(3, 5, 5, 11),
                  ModelUtil.UvFaceData.from16(5, 5, 11, 11),
                  ModelUtil.UvFaceData.from16(5, 5, 11, 11)
               };
            }

            ModelUtil.addSpriteBox(rawQuads, sprite, 0.125F, 0.3125F, 0.3125F, 0.25F, 0.6875F, 0.6875F, explicitUvs);
         }

         List<BakedQuad> baked = new ArrayList<>();

         for (MutableQuad q : rawQuads) {
            q.rotate(Direction.WEST, key.side, 0.5F, 0.5F, 0.5F);
            q.multShade();
            baked.add(q.toBakedBlock());
         }

         cached.put(key, baked);
      }

      return cached.get(key);
   }
}
