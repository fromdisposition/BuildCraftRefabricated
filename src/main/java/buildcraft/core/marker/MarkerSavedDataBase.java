/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker;

import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;

public abstract class MarkerSavedDataBase<C extends MarkerConnection<C>> extends SavedData {
   public List<BlockPos> markerPositions = new ArrayList<>();
   public List<List<BlockPos>> markerConnections = new ArrayList<>();
   private MarkerSubCache<C> subCache;

   protected MarkerSavedDataBase() {
   }

   public void setSubCache(MarkerSubCache<C> subCache) {
      this.subCache = subCache;
   }

   protected void syncFromSubCache() {
      if (this.subCache != null) {
         this.markerPositions = new ArrayList<>(this.subCache.getAllMarkers());
         this.markerConnections = new ArrayList<>();

         for (C connection : this.subCache.getConnections()) {
            this.markerConnections.add(new ArrayList<>(connection.getMarkerPositions()));
         }
      }
   }

   protected static <T extends MarkerSavedDataBase<?>> Codec<T> codec(Supplier<T> factory) {
      return RecordCodecBuilder.create(
         instance -> instance.group(
               BlockPos.CODEC.listOf().optionalFieldOf("markers", List.of()).forGetter(d -> {
                  d.syncFromSubCache();
                  return d.markerPositions;
               }),
               BlockPos.CODEC.listOf().listOf().optionalFieldOf("connections", List.of()).forGetter(d -> d.markerConnections)
            )
            .apply(instance, (positions, connections) -> {
               T data = factory.get();
               data.markerPositions = new ArrayList<>(positions);
               data.markerConnections = new ArrayList<>();

               for (List<BlockPos> conn : connections) {
                  data.markerConnections.add(new ArrayList<>(conn));
               }

               return data;
            })
      );
   }

}
