package buildcraft.energy.generation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import buildcraft.lib.misc.RandUtil;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class OilWorldgenParityTest {
   private static final List<Fixture> RNG_FIXTURES = List.of(
      new Fixture(12345L, 0, 0),
      new Fixture(987654321L, -17, 42),
      new Fixture(-1L, 31, -8)
   );

   @Test
   void bcChunkRandomParity() {
      for (Fixture fixture : RNG_FIXTURES) {
         Random expected = RandUtil.createRandomForChunk(
            fixture.seed(), fixture.chunkX(), fixture.chunkZ(), BcChunkRandom.OIL_ORIGIN_CHUNK_SALT
         );
         BcChunkRandom actual = BcChunkRandom.forOilOriginChunk(fixture.seed(), fixture.chunkX(), fixture.chunkZ());
         for (int i = 0; i < 16; i++) {
            assertEquals(expected.nextInt(10_000), actual.nextInt(10_000));
            assertEquals(expected.nextDouble(), actual.nextDouble());
            assertEquals(expected.nextFloat(), actual.nextFloat());
         }
      }
   }

   @Test
   void tierResolutionFixtures() {
      OilGenSettings config = OilGenSettings.current();

      assertEquals(
         OilSpawnRoll.Tier.RICH,
         OilSpawnRoll.resolveTier(
            true, OilPatchKind.NONE, Identifier.parse("minecraft:desert"), Identifier.parse("minecraft:desert"), config
         )
      );
      assertEquals(
         OilSpawnRoll.Tier.NORMAL,
         OilSpawnRoll.resolveTier(
            false, OilPatchKind.NONE, Identifier.parse("minecraft:plains"), Identifier.parse("minecraft:plains"), config
         )
      );
      assertEquals(
         OilSpawnRoll.Tier.OIL_PATCH,
         OilSpawnRoll.resolveTier(
            false, OilPatchKind.OCEAN, Identifier.parse("minecraft:plains"), Identifier.parse("buildcraftenergy:oil_ocean"), config
         )
      );
   }

   @Test
   void tendrilStructureCountFixtures() {
      assertEquals(860, tendrilCount(12345L, 0, 0, 4, 25));
      assertEquals(367, tendrilCount(987654321L, -17, 42, 4, 25));
      assertEquals(83, tendrilCount(-1L, 31, -8, 2, 8));
   }

   private static int tendrilCount(long seed, int chunkX, int chunkZ, int lakeRadius, int radius) {
      BcChunkRandom rand = BcChunkRandom.forOilOriginChunk(seed, chunkX, chunkZ);
      OilGenStructure structure = OilGenerator.createTendril(new BlockPos(0, 62, 0), lakeRadius, radius, rand);
      return structure.countOilBlocks();
   }

   private record Fixture(long seed, int chunkX, int chunkZ) {}
}
