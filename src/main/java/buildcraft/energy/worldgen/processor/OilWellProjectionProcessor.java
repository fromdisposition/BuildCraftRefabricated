package buildcraft.energy.worldgen.processor;

import buildcraft.energy.worldgen.core.OilStructureDefaults;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Runs after vanilla {@link net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor}
 * and pins the bedrock spring marker to {@code minY}. All other blocks keep gravity projection
 * ({@code heightmap - 1 + templateY}), so sphere, shaft, connector, and surface share one anchor.
 */
public final class OilWellProjectionProcessor extends StructureProcessor {
   public static final MapCodec<OilWellProjectionProcessor> CODEC = MapCodec.unit(OilWellProjectionProcessor::new);

   @Override
   public StructureTemplate.StructureBlockInfo processBlock(
      final LevelReader level,
      final BlockPos targetPosition,
      final BlockPos referencePos,
      final StructureTemplate.StructureBlockInfo originalBlockInfo,
      final StructureTemplate.StructureBlockInfo processedBlockInfo,
      final StructurePlaceSettings settings
   ) {
      if (originalBlockInfo.pos().getY() != OilStructureDefaults.SPRING_TEMPLATE_Y) {
         return processedBlockInfo;
      }

      BlockPos pos = processedBlockInfo.pos();
      return new StructureTemplate.StructureBlockInfo(
         new BlockPos(pos.getX(), minBuildY(level), pos.getZ()), processedBlockInfo.state(), processedBlockInfo.nbt()
      );
   }

   private static int minBuildY(final LevelReader level) {
      if (level instanceof LevelHeightAccessor heightAccessor) {
         return heightAccessor.getMinY();
      }
      return -64;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return BCEnergyStructureProcessorTypes.OIL_WELL_PROJECTION;
   }
}
