package buildcraft.energy.worldgen.structure;

import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.worldgen.processor.WaterSpringBedrockProcessor;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Places a bedrock water spring NBT template and fills the BC water column above it. */
public final class WaterSpringPoolElement extends SinglePoolElement {
   public static final MapCodec<WaterSpringPoolElement> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec())
         .apply(instance, WaterSpringPoolElement::new)
   );

   private WaterSpringPoolElement(
      Either<Identifier, StructureTemplate> template,
      Holder<StructureProcessorList> processors,
      StructureTemplatePool.Projection projection,
      Optional<LiquidSettings> overrideLiquidSettings
   ) {
      super(template, processors, projection, overrideLiquidSettings);
   }

   public static Function<StructureTemplatePool.Projection, WaterSpringPoolElement> of(
      String location,
      Holder<StructureProcessorList> processors
   ) {
      return projection -> new WaterSpringPoolElement(Either.left(Identifier.parse(location)), processors, projection, Optional.empty());
   }

   @Override
   public boolean place(
      StructureTemplateManager structureTemplateManager,
      WorldGenLevel level,
      StructureManager structureManager,
      ChunkGenerator generator,
      BlockPos position,
      BlockPos referencePos,
      Rotation rotation,
      BoundingBox chunkBB,
      RandomSource random,
      LiquidSettings liquidSettings,
      boolean keepJigsaws
   ) {
      int columnX = position.getX();
      int columnZ = position.getZ();
      BlockPos bedrock = WaterSpringBedrockProcessor.findBedrock(level, columnX, columnZ);
      if (bedrock == null) {
         return false;
      }

      level.setBlock(bedrock, BCCoreBlocks.SPRING_WATER.defaultBlockState(), 2);
      fillWaterColumn(level, bedrock);
      level.getChunk(bedrock).markPosForPostprocessing(bedrock);
      return true;
   }

   private static void fillWaterColumn(WorldGenLevel level, BlockPos springPos) {
      int x = springPos.getX();
      int z = springPos.getZ();
      int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
      int maxY = Math.min(getWaterColumnMaxY(level, x, surfaceY, z), level.getMaxY() - 1);
      for (int y = springPos.getY() + 1; y <= maxY; y++) {
         BlockPos at = new BlockPos(x, y, z);
         level.setBlock(at, Blocks.WATER.defaultBlockState(), 2);
         level.getChunk(at).markPosForPostprocessing(at);
      }
   }

   private static int getWaterColumnMaxY(WorldGenLevel level, int x, int surfaceY, int z) {
      BlockPos topSurface = new BlockPos(x, surfaceY - 1, z);
      if (level.getFluidState(topSurface).is(FluidTags.WATER)) {
         return surfaceY - 1;
      }

      return surfaceY;
   }

   @Override
   public StructurePoolElementType<?> getType() {
      return BCEnergyStructures.WATER_SPRING_POOL_ELEMENT;
   }
}
