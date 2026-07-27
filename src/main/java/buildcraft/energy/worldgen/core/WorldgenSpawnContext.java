package buildcraft.energy.worldgen.core;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Distinguishes real chunk decoration from structure probes such as {@code /locate}. */
public final class WorldgenSpawnContext {
   private WorldgenSpawnContext() {
   }

   public static boolean isChunkDecoration(Structure.GenerationContext context) {
      return context.heightAccessor() instanceof WorldGenRegion;
   }
}
