package buildcraft.builders.snapshot;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Lightweight per-dimension index of construction markers that currently have a blueprint awaiting construction. Builder
 * robots query this set instead of scanning chunks, so marker discovery stays O(markers).
 */
public final class ConstructionMarkerRegistry {
   private static final Map<ResourceKey<Level>, Set<BlockPos>> MARKERS = new ConcurrentHashMap<>();

   private ConstructionMarkerRegistry() {
   }

   public static void register(Level level, BlockPos pos) {
      if (level != null && !level.isClientSide()) {
         MARKERS.computeIfAbsent(level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
      }
   }

   public static void unregister(Level level, BlockPos pos) {
      if (level != null && !level.isClientSide()) {
         Set<BlockPos> set = MARKERS.get(level.dimension());
         if (set != null) {
            set.remove(pos);
         }
      }
   }

   public static Set<BlockPos> getMarkers(Level level) {
      if (level == null) {
         return Collections.emptySet();
      }

      Set<BlockPos> set = MARKERS.get(level.dimension());
      return set == null ? Collections.emptySet() : set;
   }
}
