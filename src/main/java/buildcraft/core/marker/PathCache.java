package buildcraft.core.marker;

import buildcraft.lib.marker.MarkerCache;
import net.minecraft.world.level.Level;

public class PathCache extends MarkerCache<PathSubCache> {
   public static final PathCache INSTANCE = new PathCache();

   public PathCache() {
      super("path");
   }

   protected PathSubCache createSubCache(Level world) {
      return new PathSubCache(world);
   }
}
