package buildcraft.energy.worldgen.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import buildcraft.energy.worldgen.core.OilStructureDefaults;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionDefaults;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OilWellProjectionProcessorTest {
   private static final int CENTER = OilStructureDefaults.TEMPLATE_CENTER;
   private static final OilWellProjectionProcessor PROCESSOR = new OilWellProjectionProcessor();
   private static final StructurePlaceSettings SETTINGS = new StructurePlaceSettings();

   @BeforeAll
   static void bootstrapMinecraft() {
      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
   }

   @ParameterizedTest
   @ValueSource(ints = {20, 64, 100})
   void depositUsesFixedWorldY(int surfaceBlockY) {
      LevelReader level = new StubHeightLevel(surfaceBlockY, -64);

      for (int templateY = OilStructureDefaults.DEPOSIT_MIN_WORLD_Y; templateY <= OilStructureDefaults.DEPOSIT_MAX_WORLD_Y; templateY++) {
         int worldY = projectY(level, templateY);
         assertEquals(templateY, worldY, "deposit templateY=" + templateY + " surface=" + surfaceBlockY);
      }
   }

   @ParameterizedTest
   @ValueSource(ints = {20, 64, 100})
   void bridgeAndTerrainAreContinuous(int surfaceBlockY) {
      LevelReader level = new StubHeightLevel(surfaceBlockY, -64);
      int terrainBottom = height(level) - 1 + OilStructureDefaults.CONNECTOR_TERRAIN_MIN_TEMPLATE_Y;

      int bridgeMin = Integer.MAX_VALUE;
      int bridgeMax = Integer.MIN_VALUE;
      for (int templateY = OilStructureDefaults.CONNECTOR_BRIDGE_TEMPLATE_BASE;
         templateY < OilStructureDefaults.CONNECTOR_BRIDGE_TEMPLATE_BASE + OilStructureDefaults.CONNECTOR_BRIDGE_LAYER_COUNT;
         templateY++) {
         Integer worldY = projectYOrNull(level, templateY);
         if (worldY != null) {
            bridgeMin = Math.min(bridgeMin, worldY);
            bridgeMax = Math.max(bridgeMax, worldY);
         }
      }

      int terrainMin = Integer.MAX_VALUE;
      int terrainMax = Integer.MIN_VALUE;
      for (int templateY = OilStructureDefaults.CONNECTOR_TERRAIN_MIN_TEMPLATE_Y;
         templateY <= OilStructureDefaults.CONNECTOR_TERRAIN_MAX_TEMPLATE_Y;
         templateY++) {
         Integer worldY = projectYOrNull(level, templateY);
         if (worldY != null) {
            terrainMin = Math.min(terrainMin, worldY);
            terrainMax = Math.max(terrainMax, worldY);
         }
      }

      assertEquals(OilStructureDefaults.CONNECTOR_MIN_WORLD_Y, bridgeMin, "surface=" + surfaceBlockY);
      assertEquals(terrainBottom - 1, bridgeMax, "surface=" + surfaceBlockY);
      assertEquals(terrainBottom, terrainMin, "surface=" + surfaceBlockY);
      assertEquals(height(level) - 1 + OilStructureDefaults.CONNECTOR_TERRAIN_MAX_TEMPLATE_Y, terrainMax, "surface=" + surfaceBlockY);
      assertEquals(bridgeMax + 1, terrainMin, "surface=" + surfaceBlockY);
   }

   @ParameterizedTest
   @ValueSource(ints = {20, 64, 100})
   void springPinsToMinBuildY(int surfaceBlockY) {
      LevelReader level = new StubHeightLevel(surfaceBlockY, -64);
      assertEquals(-64, projectY(level, OilStructureDefaults.SPRING_TEMPLATE_Y));
   }

   @ParameterizedTest
   @ValueSource(ints = {20, 64, 100})
   void bedrockShaftUsesFixedWorldY(int surfaceBlockY) {
      LevelReader level = new StubHeightLevel(surfaceBlockY, -64);
      IntStream.rangeClosed(OilStructureDefaults.BEDROCK_SHAFT_MIN_WORLD_Y, OilStructureDefaults.BEDROCK_SHAFT_MAX_WORLD_Y)
         .forEach(templateY -> assertEquals(templateY, projectY(level, templateY), "surface=" + surfaceBlockY));
   }

   private static int height(LevelReader level) {
      return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, CENTER, CENTER);
   }

   private static int projectY(LevelReader level, int templateY) {
      Integer worldY = projectYOrNull(level, templateY);
      assertNotNull(worldY, "templateY=" + templateY);
      return worldY;
   }

   @Nullable
   private static Integer projectYOrNull(LevelReader level, int templateY) {
      BlockState placeholder = Blocks.STONE.defaultBlockState();
      BlockPos pos = new BlockPos(CENTER, templateY, CENTER);
      StructureTemplate.StructureBlockInfo original = new StructureTemplate.StructureBlockInfo(pos, placeholder, null);
      StructureTemplate.StructureBlockInfo processed = new StructureTemplate.StructureBlockInfo(pos, placeholder, null);
      StructureTemplate.StructureBlockInfo result = PROCESSOR.processBlock(level, pos, BlockPos.ZERO, original, processed, SETTINGS);
      return result == null ? null : result.pos().getY();
   }

   private static final class StubHeightLevel implements LevelReader, LevelHeightAccessor {
      private final int heightmapY;
      private final int minY;
      private final BiomeManager biomeManager = new BiomeManager(this, 0L);

      private StubHeightLevel(int surfaceBlockY, int minY) {
         this.heightmapY = surfaceBlockY + 1;
         this.minY = minY;
      }

      @Override
      public int getHeight(Heightmap.Types type, int x, int z) {
         return this.heightmapY;
      }

      @Override
      public int getMinY() {
         return this.minY;
      }

      @Override
      public int getHeight() {
         return DimensionDefaults.OVERWORLD_LEVEL_HEIGHT;
      }

      @Nullable
      @Override
      public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus targetStatus, boolean loadOrGenerate) {
         return null;
      }

      @Override
      public boolean hasChunk(int chunkX, int chunkZ) {
         return false;
      }

      @Override
      public int getSkyDarken() {
         return 0;
      }

      @Override
      public BiomeManager getBiomeManager() {
         return this.biomeManager;
      }

      @Override
      public Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
         return RegistryAccess.EMPTY.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
      }

      @Override
      public boolean isClientSide() {
         return false;
      }

      @Override
      public int getSeaLevel() {
         return 63;
      }

      @Override
      public DimensionType dimensionType() {
         return new DimensionType(
            false,
            true,
            false,
            false,
            1.0,
            -64,
            384,
            384,
            BlockTags.INFINIBURN_OVERWORLD,
            0.0F,
            new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
            DimensionType.Skybox.OVERWORLD,
            CardinalLighting.Type.DEFAULT,
            EnvironmentAttributeMap.EMPTY,
            HolderSet.empty(),
            Optional.empty()
         );
      }

      @Override
      public RegistryAccess registryAccess() {
         return RegistryAccess.EMPTY;
      }

      @Override
      public FeatureFlagSet enabledFeatures() {
         return FeatureFlags.VANILLA_SET;
      }

      @Override
      public EnvironmentAttributeReader environmentAttributes() {
         return EnvironmentAttributeReader.EMPTY;
      }

      @Nullable
      @Override
      public BlockEntity getBlockEntity(BlockPos pos) {
         return null;
      }

      @Override
      public List<VoxelShape> getEntityCollisions(@Nullable Entity source, AABB testArea) {
         return List.of();
      }

      @Override
      public BlockState getBlockState(BlockPos pos) {
         return Blocks.STONE.defaultBlockState();
      }

      @Override
      public FluidState getFluidState(BlockPos pos) {
         return Fluids.EMPTY.defaultFluidState();
      }

      @Override
      public LevelLightEngine getLightEngine() {
         return null;
      }

      @Override
      public WorldBorder getWorldBorder() {
         return new WorldBorder();
      }

      @Nullable
      @Override
      public LevelReader getChunkForCollisions(int chunkX, int chunkZ) {
         return null;
      }
   }
}
