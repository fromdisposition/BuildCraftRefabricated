package buildcraft.energy.gen;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.gen.SpringWorldgen;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.data.Box;
import com.mojang.serialization.Codec;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class OilDepositFeature extends Feature<NoneFeatureConfiguration> {
   private static final double RICH_LARGE_RATE = 6.0E-4;
   private static final double RICH_MEDIUM_RATE = 0.0025;
   private static final double SPRING_CHANCE = 0.25;

   public OilDepositFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get()) {
         return false;
      }

      WorldGenLevel level = context.level();
      if (level.getLevel().getChunkSource().getGenerator() instanceof FlatLevelSource) {
         return false;
      }

      RandomSource random = context.random();
      BlockPos origin = context.origin();
      int x = origin.getX();
      int z = origin.getZ();
      Holder<Biome> biomeHolder = level.getBiome(origin);
      Identifier biomeId = Identifier.parse(biomeHolder.getRegisteredName());
      if (isBiomeExcluded(biomeId)) {
         return false;
      }

      boolean nether = level.getLevel().dimension() == Level.NETHER;
      if (nether && !BCEnergyConfig.enableNetherOilGeneration.get()) {
         return false;
      }

      DepositType type = nether ? rollNetherType(random) : rollType(biomeHolder, biomeId, random);
      if (type == null) {
         return false;
      }

      int groundLevel = nether ? findSolidTop(level, x, z) : findGroundLevel(level, x, z);
      if (groundLevel < level.getMinY() + 5) {
         return false;
      }

      if (surfaceDeviation(level, x, groundLevel, z) > 0.45) {
         return false;
      }

      BlockState oil = oilState();
      if (oil == null) {
         return false;
      }

      if (type == DepositType.LAKE) {
         return placeLake(level, random, x, groundLevel, z, oil);
      }

      return placeWell(level, random, x, z, groundLevel, type, biomeHolder, oil, nether);
   }

   private static DepositType rollNetherType(RandomSource random) {
      double rate = BCEnergyConfig.oilWellGenerationRate.get() * BCEnergyConfig.netherOilGenRateMultiplier.get();
      if (random.nextDouble() <= BCEnergyConfig.largeOilGenProb.get() * rate) {
         return DepositType.LARGE;
      }

      if (random.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * rate) {
         return DepositType.MEDIUM;
      }

      return null;
   }

   private static DepositType rollType(Holder<Biome> biomeHolder, Identifier biomeId, RandomSource random) {
      Set<Identifier> rich = BCEnergyConfig.getRichSurfaceDepositBiomes();
      Set<Identifier> surface = BCEnergyConfig.getSurfaceDepositBiomes();
      Set<Identifier> standard = BCEnergyConfig.getStandardSurfaceDepositBiomes();
      Set<Identifier> mountainous = BCEnergyConfig.getMountainousSurfaceDepositBiomes();
      double rate = BCEnergyConfig.oilWellGenerationRate.get();
      if (BCEnergyConfig.getForceExcessiveOilBiomes().contains(biomeId)) {
         rate *= 30.0;
      }

      boolean isRich = rich.contains(biomeId);
      boolean isOcean = biomeHolder.is(BiomeTags.IS_OCEAN);
      boolean richLand = isRich && !isOcean;

      if (richLand) {
         if (random.nextDouble() <= RICH_LARGE_RATE * rate) {
            return DepositType.LARGE;
         }

         if (random.nextDouble() <= RICH_MEDIUM_RATE * rate) {
            return DepositType.MEDIUM;
         }

         return null;
      }

      double bonus;
      if (isRich && isOcean) {
         bonus = 1.5;
      } else if (surface.contains(biomeId)) {
         bonus = 1.25;
      } else if (mountainous.contains(biomeId)) {
         bonus = 0.1;
      } else if (standard.contains(biomeId)) {
         bonus = 1.0;
      } else {
         bonus = 0.5;
      }

      double effective = bonus * rate;
      if (random.nextDouble() <= BCEnergyConfig.largeOilGenProb.get() * effective) {
         return DepositType.LARGE;
      }

      if (random.nextDouble() <= BCEnergyConfig.mediumOilGenProb.get() * effective) {
         return DepositType.MEDIUM;
      }

      if (surface.contains(biomeId) && random.nextDouble() <= BCEnergyConfig.smallOilGenProb.get() * effective) {
         return DepositType.LAKE;
      }

      return null;
   }

   private static boolean placeWell(
      WorldGenLevel level,
      RandomSource random,
      int x,
      int z,
      int groundLevel,
      DepositType type,
      Holder<Biome> biomeHolder,
      BlockState oil,
      boolean nether
   ) {
      int wellHeight = type == DepositType.LARGE ? 16 : 6;
      int maxHeight = groundLevel + wellHeight;
      if (maxHeight >= level.getMaxY() - 1) {
         return false;
      }

      int wellY = level.getMinY() + 25 + random.nextInt(10);
      int radius = type == DepositType.LARGE ? 8 + random.nextInt(9) : 4 + random.nextInt(4);
      int radiusSq = radius * radius;
      int oilCount = 0;

      for (int dx = -radius; dx <= radius; dx++) {
         for (int dy = -radius; dy <= radius; dy++) {
            for (int dz = -radius; dz <= radius; dz++) {
               int distSq = dx * dx + dy * dy + dz * dz;
               if (distSq <= radiusSq) {
                  BlockPos pos = new BlockPos(x + dx, wellY + dy, z + dz);
                  level.setBlock(pos, oil, distSq == radiusSq ? 3 : 2);
                  oilCount++;
               }
            }
         }
      }

      boolean richBiome = BCEnergyConfig.getRichSurfaceDepositBiomes().contains(Identifier.parse(biomeHolder.getRegisteredName()));
      boolean placeSpring = BCEnergyConfig.enableOilSpouts.get()
         && (nether || richBiome && type == DepositType.LARGE && random.nextDouble() <= SPRING_CHANCE);
      BlockPos springPos = null;

      if (placeSpring) {
         springPos = SpringWorldgen.placeOilSpringOnBedrock(level, x, z, oil);
         if (springPos != null) {
            oilCount += 2;
         }
      }

      int worldTopY = SpringWorldgen.findSpoutWorldTop(level, x, z, wellY);
      if (BCEnergyConfig.enableOilSpouts.get()) {
         int spoutRadius = type == DepositType.LARGE ? 1 : 0;
         int spoutMin = type == DepositType.LARGE ? BCEnergyConfig.largeSpoutMinHeight.get() : BCEnergyConfig.finiteSpoutMinHeight.get();
         int spoutMax = type == DepositType.LARGE ? BCEnergyConfig.largeSpoutMaxHeight.get() : BCEnergyConfig.finiteSpoutMaxHeight.get();
         if (spoutMax < spoutMin) {
            int swap = spoutMax;
            spoutMax = spoutMin;
            spoutMin = swap;
         }

         int stackHeight = spoutMax == spoutMin ? spoutMax : spoutMin + random.nextInt(spoutMax - spoutMin + 1);
         oilCount += SpringWorldgen.placeOilSpout(level, x, z, wellY, oil, stackHeight, spoutRadius, springPos, radius);
      }
      if (type == DepositType.LARGE) {
         int branchTop = (placeSpring ? worldTopY : maxHeight) - wellHeight / 2;
         for (int y = wellY; y <= branchTop; y++) {
            level.setBlock(new BlockPos(x + 1, y, z), oil, 3);
            level.setBlock(new BlockPos(x - 1, y, z), oil, 3);
            level.setBlock(new BlockPos(x, y, z + 1), oil, 3);
            level.setBlock(new BlockPos(x, y, z - 1), oil, 3);
            oilCount += 4;
         }
      }

      if (nether) {
         int poolRadius = type == DepositType.LARGE ? 8 + random.nextInt(5) : 5 + random.nextInt(3);
         oilCount += placeSurfacePool(level, random, x, z, poolRadius, oil);
      } else if (richBiome) {
         int poolRadius = type == DepositType.LARGE ? 8 + random.nextInt(5) : 5 + random.nextInt(3);
         oilCount += placeSurfacePool(level, random, x, z, poolRadius, oil);
      } else if (biomeHolder.is(BiomeTags.IS_OCEAN)) {
         int lakeRadius = type == DepositType.LARGE ? 4 : 2;
         int tendrilRadius = type == DepositType.LARGE ? 25 + random.nextInt(20) : 5 + random.nextInt(10);
         oilCount += placeTendrilDeposit(level, random, x, z, lakeRadius, tendrilRadius, oil);
      }

      if (springPos != null && level.getBlockEntity(springPos) instanceof TileSpringOil tile) {
         tile.totalSources = oilCount;
      }

      return oilCount > 0;
   }

   private static boolean placeLake(WorldGenLevel level, RandomSource random, int x, int groundLevel, int z, BlockState oil) {
      return placeTendrilDeposit(level, random, x, z, 2, 5 + random.nextInt(10), oil) > 0;
   }

   private static int placeTendrilDeposit(
      WorldGenLevel level, RandomSource random, int centerX, int centerZ, int lakeRadius, int tendrilRadius, BlockState oil
   ) {
      int diameter = tendrilRadius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int cx = tendrilRadius;
      int cz = tendrilRadius;

      for (int dx = -lakeRadius; dx <= lakeRadius; dx++) {
         for (int dz = -lakeRadius; dz <= lakeRadius; dz++) {
            if (dx * dx + dz * dz <= lakeRadius * lakeRadius) {
               pattern[cx + dx][cz + dz] = true;
            }
         }
      }

      for (int w = 1; w < tendrilRadius; w++) {
         float chance = (float)(tendrilRadius - w + 4) / (float)(tendrilRadius + 4);
         fillPatternIfProba(random, chance, cx, cz + w, pattern);
         fillPatternIfProba(random, chance, cx, cz - w, pattern);
         fillPatternIfProba(random, chance, cx + w, cz, pattern);
         fillPatternIfProba(random, chance, cx - w, cz, pattern);

         for (int i = 1; i <= w; i++) {
            fillPatternIfProba(random, chance, cx + i, cz + w, pattern);
            fillPatternIfProba(random, chance, cx + i, cz - w, pattern);
            fillPatternIfProba(random, chance, cx + w, cz + i, pattern);
            fillPatternIfProba(random, chance, cx - w, cz + i, pattern);
            fillPatternIfProba(random, chance, cx - i, cz + w, pattern);
            fillPatternIfProba(random, chance, cx - i, cz - w, pattern);
            fillPatternIfProba(random, chance, cx + w, cz - i, pattern);
            fillPatternIfProba(random, chance, cx - w, cz - i, pattern);
         }
      }

      return placeLakePattern(level, random, centerX - tendrilRadius, centerZ - tendrilRadius, pattern, oil);
   }

   private static int placeSurfacePool(WorldGenLevel level, RandomSource random, int centerX, int centerZ, int baseRadius, BlockState oil) {
      int maxRadius = baseRadius + 1;
      int diameter = maxRadius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int centerIdx = maxRadius;

      for (int dx = -maxRadius; dx <= maxRadius; dx++) {
         for (int dz = -maxRadius; dz <= maxRadius; dz++) {
            int noise = random.nextInt(3) - 1;
            int effectiveRadius = Math.max(0, baseRadius + noise);
            pattern[centerIdx + dx][centerIdx + dz] = dx * dx + dz * dz <= effectiveRadius * effectiveRadius;
         }
      }

      return placeSurfacePoolPattern(level, random, centerX - maxRadius, centerZ - maxRadius, pattern, oil);
   }

   private static int placeLakePattern(
      WorldGenLevel level, RandomSource random, int startX, int startZ, boolean[][] pattern, BlockState oil
   ) {
      int depth = random.nextBoolean() ? 1 : 2;
      int placed = 0;

      for (int px = 0; px < pattern.length; px++) {
         for (int pz = 0; pz < pattern[px].length; pz++) {
            if (pattern[px][pz]) {
               placed += OilLakePlacement.placeLakeColumn(level, startX + px, startZ + pz, depth, oil);
            }
         }
      }

      return placed;
   }

   private static int placeSurfacePoolPattern(
      WorldGenLevel level, RandomSource random, int startX, int startZ, boolean[][] pattern, BlockState oil
   ) {
      int depth = random.nextBoolean() ? 1 : 2;
      int placed = 0;
      int endX = startX + pattern.length - 1;
      int endZ = startZ + (pattern.length == 0 ? 0 : pattern[0].length - 1);
      Box bounds = new Box(new BlockPos(startX, level.getMinY(), startZ), new BlockPos(endX, level.getMaxY(), endZ));

      for (int px = 0; px < pattern.length; px++) {
         for (int pz = 0; pz < pattern[px].length; pz++) {
            if (pattern[px][pz]) {
               placed += OilLakePlacement.placeSurfacePoolColumn(level, startX + px, startZ + pz, depth, oil, bounds);
            }
         }
      }

      return placed;
   }

   private static void fillPatternIfProba(RandomSource random, float chance, int x, int z, boolean[][] pattern) {
      if (random.nextFloat() <= chance) {
         pattern[x][z] = isPatternSet(pattern, x, z - 1)
            | isPatternSet(pattern, x, z + 1)
            | isPatternSet(pattern, x - 1, z)
            | isPatternSet(pattern, x + 1, z);
      }
   }

   private static boolean isPatternSet(boolean[][] pattern, int x, int z) {
      if (x < 0 || x >= pattern.length) {
         return false;
      }

      return z >= 0 && z < pattern[x].length && pattern[x][z];
   }

   private static int findSolidTop(WorldGenLevel level, int x, int z) {
      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && !state.canBeReplaced()) {
            return y;
         }
      }

      return level.getMinY();
   }

   private static int findGroundLevel(WorldGenLevel level, int x, int z) {
      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         if (state.isAir()) {
            continue;
         }

         FluidState fluid = level.getFluidState(pos);
         if (!fluid.isEmpty()) {
            return y;
         }

         if (!state.canBeReplaced() && BlockUtil.blocksMotion(state)) {
            return y - 1;
         }
      }

      return level.getMinY();
   }

   private static double surfaceDeviation(WorldGenLevel level, int x, int groundY, int z) {
      double sum = 0.0;
      int radius = 8;
      int samples = 0;

      for (int dx = -radius; dx < radius; dx++) {
         for (int dz = -radius; dz < radius; dz++) {
            sum += findGroundLevel(level, x + dx, z + dz) - groundY;
            samples++;
         }
      }

      return Math.abs(sum / (groundY * samples));
   }

   private static boolean isBiomeExcluded(Identifier biomeId) {
      boolean listed = BCEnergyConfig.getExcludedBiomes().contains(biomeId);
      boolean blacklist = BCEnergyConfig.biomeListMode.get() == BCEnergyConfig.ListMode.BLACKLIST;
      return listed == blacklist;
   }

   @javax.annotation.Nullable
   private static BlockState oilState() {
      BlockState oil = BCEnergyFluidsFabric.oilSourceBlockStateForLevel(null);
      if (oil == null) {
         oil = BCEnergyFluidsFabric.OIL_COOL.still().defaultFluidState().createLegacyBlock();
      }

      return oil;
   }

   private enum DepositType {
      LARGE,
      MEDIUM,
      LAKE
   }
}
