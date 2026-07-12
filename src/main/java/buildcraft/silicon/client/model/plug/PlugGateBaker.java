/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.plug;

import buildcraft.lib.client.texture.BcTextureAtlases;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.gate.GateVariant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;

public class PlugGateBaker implements IPluggableStaticBaker<KeyPlugGate> {
   public static final PlugGateBaker INSTANCE = new PlugGateBaker();
   private static final Map<KeyPlugGate, List<BakedQuad>> cached = new ConcurrentHashMap<>();

   public static void onModelBake() {
      cached.clear();
   }

   private TextureAtlasSprite getSprite(String path) {
      return BcTextureAtlases.getBlockSprite(Identifier.parse(path));
   }

   public List<BakedQuad> bake(KeyPlugGate key) {
      return cached.computeIfAbsent(key, this::bakeUncached);
   }

   private List<BakedQuad> bakeUncached(KeyPlugGate key) {
      // Static gate body only. The animated on/off indicator box is drawn live by PlugGateRenderer (the dynamic
      // renderer); baking it here as well drew the whole gate twice at identical coordinates, which is what caused
      // the Z-fighting on gates placed on pipes.
      List<BakedQuad> quads = new ArrayList<>();
      GateQuadGeometry.appendStaticBaked(quads, key.variant, key.side, this::getSprite, true, 0);
      return quads;
   }

   public List<MutableQuad> bakeForItem(GateVariant variant) {
      List<MutableQuad> quads = new ArrayList<>();
      GateQuadGeometry.appendItemNorthFacing(quads, variant, this::getSprite, true);
      return quads;
   }
}
