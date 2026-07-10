package buildcraft.energy.worldgen.template;

import buildcraft.energy.worldgen.core.OilStructureDefaults;
import buildcraft.energy.worldgen.processor.OilWellProjectionProcessor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.CachedOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Builds oil deposit NBT templates for jigsaw pools.
 *
 * <p>Well template Y convention ({@link OilWellProjectionProcessor} + gravity):
 * y&ge;0 surface/spout via gravity; deposit body fixed at world {@code -42..-12}; connector bridge
 * ({@code template 100+} &rarr; fixed world Y) then terrain shaft ({@code template -11..-1} &rarr; surface);
 * bedrock shaft {@code -56..-43} on large wells.
 * Shaft width: BC 8.0 medium radius 0 (1 block), large radius 1 (3×3).
 */
public final class OilStructureTemplateBuilder {
   private static final Identifier OIL_BLOCK_ID = Identifier.parse("buildcraftenergy:oil");
   private static final Identifier SPRING_BLOCK_ID = Identifier.parse("buildcraftcore:spring_oil");
   /** Single-block surface film at template y=0 (wells, lakes, desert and ocean). */
   private static final int SURFACE_FILM_DEPTH = 1;

   private OilStructureTemplateBuilder() {
   }

   public static void generateAll(final CachedOutput cache, final Path structuresDir, final HolderGetter<Block> blocks) throws IOException {
      writeLake(cache, structuresDir.resolve("oil_lake_patch.nbt"), blocks, 0x51AF1001L, 6, 26);
      writeLake(cache, structuresDir.resolve("oil_lake_patch_b.nbt"), blocks, 0x51AF1002L, 6, 32);
      writeLake(cache, structuresDir.resolve("oil_lake_patch_c.nbt"), blocks, 0x51AF1003L, 6, 38);
      writeLake(cache, structuresDir.resolve("oil_lake_patch_d.nbt"), blocks, 0x51AF1004L, 6, 30);
      writeLake(cache, structuresDir.resolve("oil_lake_patch_e.nbt"), blocks, 0x51AF1005L, 6, 42);

      writeWell(cache, structuresDir.resolve("oil_well_medium_s.nbt"), blocks, 2, 5, 4, 6, 0, false);
      writeWell(cache, structuresDir.resolve("oil_well_medium_alt.nbt"), blocks, 2, 9, 5, 8, 0, false);
      writeWell(cache, structuresDir.resolve("oil_well_medium.nbt"), blocks, 2, 11, 6, 10, 0, false);
      writeWell(cache, structuresDir.resolve("oil_well_medium_l.nbt"), blocks, 2, 14, 7, 12, 0, false);

      writeWell(cache, structuresDir.resolve("oil_well_large_s.nbt"), blocks, 4, 28, 10, 12, 1, true);
      writeWell(cache, structuresDir.resolve("oil_well_large_m.nbt"), blocks, 4, 31, 12, 14, 1, true);
      writeWell(cache, structuresDir.resolve("oil_well_large.nbt"), blocks, 4, 35, 14, 18, 1, true);
      writeWell(cache, structuresDir.resolve("oil_well_large_l.nbt"), blocks, 4, 42, 16, 20, 1, true);
   }

   private static BlockState blockState(final HolderGetter<Block> blocks, final Identifier id) {
      return blocks.getOrThrow(ResourceKey.create(Registries.BLOCK, id)).value().defaultBlockState();
   }

   private static void writeLake(final CachedOutput cache, final Path path, final HolderGetter<Block> blocks, final long seed, final int lakeRadius, final int tendrilRadius)
      throws IOException {
      BlockState oil = blockState(blocks, OIL_BLOCK_ID);
      List<StructureTemplateExporter.BlockEntry> entries = new ArrayList<>();
      boolean[][] pattern = bcTendrilPattern(lakeRadius, tendrilRadius, seed);
      blitSurfacePattern(entries, pattern, oil);
      StructureTemplateExporter.write(
         cache,
         path,
         blocks,
         OilStructureDefaults.TEMPLATE_SIZE,
         OilStructureDefaults.TEMPLATE_SIZE,
         0,
         4,
         entries
      );
   }

   private static void writeWell(
      final CachedOutput cache,
      final Path path,
      final HolderGetter<Block> blocks,
      final int lakeRadius,
      final int tendrilRadius,
      final int sphereRadius,
      final int surfaceSpoutHeight,
      final int spoutRadius,
      final boolean withSpring
   ) throws IOException {
      BlockState oil = blockState(blocks, OIL_BLOCK_ID);
      BlockState spring = blockState(blocks, SPRING_BLOCK_ID);
      List<StructureTemplateExporter.BlockEntry> entries = new ArrayList<>();
      long seed = tendrilRadius * 31L + lakeRadius * 17L + sphereRadius * 13L;
      boolean[][] pattern = bcTendrilPattern(lakeRadius, tendrilRadius, seed);
      blitSurfacePattern(entries, pattern, oil);

      int center = OilStructureDefaults.TEMPLATE_CENTER;
      int sphereCenterY = OilStructureDefaults.SPHERE_TEMPLATE_CENTER_Y;
      int depositMinY = OilStructureDefaults.DEPOSIT_MIN_WORLD_Y;
      int depositMaxY = OilStructureDefaults.DEPOSIT_MAX_WORLD_Y;

      for (int dx = -sphereRadius; dx <= sphereRadius; dx++) {
         for (int dy = -sphereRadius; dy <= sphereRadius; dy++) {
            for (int dz = -sphereRadius; dz <= sphereRadius; dz++) {
               if (dx * dx + dy * dy + dz * dz > sphereRadius * sphereRadius) {
                  continue;
               }
               int y = sphereCenterY + dy;
               if (y < depositMinY || y > depositMaxY) {
                  continue;
               }
               entries.add(new StructureTemplateExporter.BlockEntry(center + dx, y, center + dz, oil));
            }
         }
      }

      int sphereTop = Math.min(sphereCenterY + sphereRadius, depositMaxY);
      if (sphereTop + 1 < OilStructureDefaults.CONNECTOR_MIN_WORLD_Y) {
         blitShaftColumn(entries, center, sphereTop + 1, OilStructureDefaults.CONNECTOR_MIN_WORLD_Y - 1, spoutRadius, oil);
      }

      blitShaftColumn(
         entries,
         center,
         OilStructureDefaults.CONNECTOR_BRIDGE_TEMPLATE_BASE,
         OilStructureDefaults.CONNECTOR_BRIDGE_TEMPLATE_BASE + OilStructureDefaults.CONNECTOR_BRIDGE_LAYER_COUNT - 1,
         spoutRadius,
         oil
      );
      blitShaftColumn(
         entries,
         center,
         OilStructureDefaults.CONNECTOR_TERRAIN_MIN_TEMPLATE_Y,
         OilStructureDefaults.CONNECTOR_TERRAIN_MAX_TEMPLATE_Y,
         spoutRadius,
         oil
      );

      if (withSpring) {
         blitShaftColumn(
            entries,
            center,
            OilStructureDefaults.BEDROCK_SHAFT_MIN_WORLD_Y,
            OilStructureDefaults.BEDROCK_SHAFT_MAX_WORLD_Y,
            spoutRadius,
            oil
         );
         entries.add(new StructureTemplateExporter.BlockEntry(center, OilStructureDefaults.SPRING_TEMPLATE_Y, center, spring));
      }

      if (surfaceSpoutHeight > 0) {
         blitSurfaceSpout(entries, center, surfaceSpoutHeight, spoutRadius, oil);
      }

      StructureTemplateExporter.write(
         cache,
         path,
         blocks,
         OilStructureDefaults.TEMPLATE_SIZE,
         OilStructureDefaults.TEMPLATE_SIZE,
         OilStructureDefaults.WELL_TEMPLATE_Y_OFFSET,
         64,
         entries
      );
   }

   private static void blitShaftColumn(
      final List<StructureTemplateExporter.BlockEntry> entries,
      final int center,
      final int yFrom,
      final int yTo,
      final int shaftRadius,
      final BlockState oil
   ) {
      if (yFrom > yTo) {
         return;
      }
      for (int y = yFrom; y <= yTo; y++) {
         writeCylinderY(entries, center, y, center, shaftRadius, oil);
      }
   }

   private static void blitSurfaceSpout(
      final List<StructureTemplateExporter.BlockEntry> entries,
      final int center,
      final int height,
      final int maxRadius,
      final BlockState oil
   ) {
      for (int h = 1; h <= height; h++) {
         int radius = h >= height - 1 ? 0 : maxRadius;
         writeCylinderY(entries, center, h, center, radius, oil);
      }
   }

   private static void blitSurfacePattern(
      final List<StructureTemplateExporter.BlockEntry> entries,
      final boolean[][] pattern,
      final BlockState oil
   ) {
      int center = OilStructureDefaults.TEMPLATE_CENTER;
      int half = pattern.length / 2;
      for (int x = 0; x < pattern.length; x++) {
         for (int z = 0; z < pattern[x].length; z++) {
            if (!pattern[x][z]) {
               continue;
            }
            int worldX = center - half + x;
            int worldZ = center - half + z;
            for (int d = 0; d < SURFACE_FILM_DEPTH; d++) {
               entries.add(new StructureTemplateExporter.BlockEntry(worldX, OilStructureDefaults.SURFACE_TEMPLATE_Y - d, worldZ, oil));
            }
         }
      }
   }

   /** Port of BC 8.0 {@code OilGenerator.createTendril}. */
   static boolean[][] bcTendrilPattern(final int lakeRadius, final int tendrilRadius, final long seed) {
      int diameter = tendrilRadius * 2 + 1;
      boolean[][] pattern = new boolean[diameter][diameter];
      int x = tendrilRadius;
      int z = tendrilRadius;
      Random rand = new Random(seed);

      for (int dx = -lakeRadius; dx <= lakeRadius; dx++) {
         for (int dz = -lakeRadius; dz <= lakeRadius; dz++) {
            pattern[x + dx][z + dz] = dx * dx + dz * dz <= lakeRadius * lakeRadius;
         }
      }

      for (int w = 1; w < tendrilRadius; w++) {
         float proba = (float)(tendrilRadius - w + 4) / (tendrilRadius + 4);
         fillPatternIfProba(rand, proba, x, z + w, pattern);
         fillPatternIfProba(rand, proba, x, z - w, pattern);
         fillPatternIfProba(rand, proba, x + w, z, pattern);
         fillPatternIfProba(rand, proba, x - w, z, pattern);
         for (int i = 1; i <= w; i++) {
            fillPatternIfProba(rand, proba, x + i, z + w, pattern);
            fillPatternIfProba(rand, proba, x + i, z - w, pattern);
            fillPatternIfProba(rand, proba, x + w, z + i, pattern);
            fillPatternIfProba(rand, proba, x - w, z + i, pattern);
            fillPatternIfProba(rand, proba, x - i, z + w, pattern);
            fillPatternIfProba(rand, proba, x - i, z - w, pattern);
            fillPatternIfProba(rand, proba, x + w, z - i, pattern);
            fillPatternIfProba(rand, proba, x - w, z - i, pattern);
         }
      }
      return pattern;
   }

   private static void fillPatternIfProba(final Random rand, final float proba, final int x, final int z, final boolean[][] pattern) {
      if (x < 0 || x >= pattern.length || z < 0 || z >= pattern[x].length) {
         return;
      }
      if (rand.nextFloat() <= proba) {
         pattern[x][z] = isSet(pattern, x, z - 1)
            | isSet(pattern, x, z + 1)
            | isSet(pattern, x - 1, z)
            | isSet(pattern, x + 1, z);
      }
   }

   private static boolean isSet(final boolean[][] pattern, final int x, final int z) {
      if (x < 0 || x >= pattern.length || z < 0 || z >= pattern[x].length) {
         return false;
      }
      return pattern[x][z];
   }

   private static void writeCylinderY(
      final List<StructureTemplateExporter.BlockEntry> entries,
      final int centerX,
      final int y,
      final int centerZ,
      final int radius,
      final BlockState oil
   ) {
      if (radius <= 0) {
         entries.add(new StructureTemplateExporter.BlockEntry(centerX, y, centerZ, oil));
         return;
      }
      int radiusSq = radius * radius;
      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            if (dx * dx + dz * dz <= radiusSq) {
               entries.add(new StructureTemplateExporter.BlockEntry(centerX + dx, y, centerZ + dz, oil));
            }
         }
      }
   }
}
