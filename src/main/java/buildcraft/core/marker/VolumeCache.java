package buildcraft.core.marker;

import buildcraft.lib.marker.MarkerCache;
import net.minecraft.world.level.Level;

public class VolumeCache extends MarkerCache<VolumeSubCache> {
   public static final VolumeCache INSTANCE = new VolumeCache();

   private VolumeCache() {
      super("volume");
   }

   protected VolumeSubCache createSubCache(Level world) {
      return new VolumeSubCache(world);
   }
}
