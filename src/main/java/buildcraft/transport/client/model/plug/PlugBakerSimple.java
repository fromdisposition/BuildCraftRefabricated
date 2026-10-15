/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model.plug;

import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.client.model.MutableQuad;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

public class PlugBakerSimple<K extends PluggableModelKey> implements IPluggableStaticBaker<K> {
   private final PlugBakerSimple.IQuadProvider provider;
   private final Map<Direction, List<BakedQuad>> cached = new EnumMap<>(Direction.class);
   private MutableQuad[] lastSeen;

   public PlugBakerSimple(PlugBakerSimple.IQuadProvider provider) {
      this.provider = provider;
   }

   @Override
   public List<BakedQuad> bake(K key) {
      MutableQuad[] quads = this.provider.getCutoutQuads();
      if (quads != this.lastSeen) {
         this.cached.clear();
         MutableQuad copy = new MutableQuad();

         for (Direction to : Direction.values()) {
            List<BakedQuad> list = new ArrayList<>();

            for (MutableQuad q : quads) {
               copy.copyFrom(q);
               copy.rotate(Direction.WEST, to, 0.5F, 0.5F, 0.5F);
               copy.multShade();
               list.add(copy.toBakedBlock());
            }

            this.cached.put(to, list);
         }

         this.lastSeen = quads;
      }

      return this.cached.get(key.side);
   }

   public interface IQuadProvider {
      MutableQuad[] getCutoutQuads();
   }
}
