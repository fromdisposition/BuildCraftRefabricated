package buildcraft.energy.worldgen.core;

import buildcraft.energy.BCEnergyConfig;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class WorldgenDimensionFilters {
   private WorldgenDimensionFilters() {
   }

   public static boolean isDimensionExcluded(Structure.GenerationContext context) {
      if (!(context.heightAccessor() instanceof WorldGenLevel worldGenLevel)) {
         return false;
      }
      Level level = worldGenLevel.getLevel();
      Identifier dimensionId = buildcraft.lib.misc.RegistryKeyUtil.id(level.dimension());
      Set<Identifier> excluded = BCEnergyConfig.getExcludedDimensions();
      boolean inList = excluded.contains(dimensionId);
      return BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST ? inList : !inList;
   }
}
