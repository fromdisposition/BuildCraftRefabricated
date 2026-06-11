package buildcraft.energy.worldgen.processor;

import buildcraft.energy.worldgen.core.OilStructureDefaults;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Runs after {@link net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor}.
 * Surface tendrils/spouts keep gravity; the deposit and bedrock shaft use fixed world Y; the connector
 * uses literal template Y up to the surface column cap.
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
      int templateY = originalBlockInfo.pos().getY();
      BlockPos pos = processedBlockInfo.pos();
      int x = pos.getX();
      int z = pos.getZ();

      if (templateY == OilStructureDefaults.SPRING_TEMPLATE_Y) {
         return new StructureTemplate.StructureBlockInfo(
            new BlockPos(x, minBuildY(level), z), processedBlockInfo.state(), processedBlockInfo.nbt()
         );
      }

      if (templateY >= OilStructureDefaults.SURFACE_TEMPLATE_Y) {
         return processedBlockInfo;
      }

      if (isDepositWorldY(templateY)) {
         return fixedY(processedBlockInfo, templateY);
      }

      if (isBedrockShaftWorldY(templateY)) {
         return fixedY(processedBlockInfo, templateY);
      }

      if (isConnectorWorldY(templateY)) {
         int connectorTop = surfaceY(level, x, z, -1);
         if (templateY > connectorTop) {
            return null;
         }
         return fixedY(processedBlockInfo, templateY);
      }

      return processedBlockInfo;
   }

   private static StructureTemplate.StructureBlockInfo fixedY(
      final StructureTemplate.StructureBlockInfo processedBlockInfo,
      final int worldY
   ) {
      BlockPos pos = processedBlockInfo.pos();
      return new StructureTemplate.StructureBlockInfo(
         new BlockPos(pos.getX(), worldY, pos.getZ()), processedBlockInfo.state(), processedBlockInfo.nbt()
      );
   }

   static boolean isDepositWorldY(final int templateY) {
      return templateY >= OilStructureDefaults.DEPOSIT_MIN_WORLD_Y && templateY <= OilStructureDefaults.DEPOSIT_MAX_WORLD_Y;
   }

   static boolean isBedrockShaftWorldY(final int templateY) {
      return templateY >= OilStructureDefaults.BEDROCK_SHAFT_MIN_WORLD_Y
         && templateY <= OilStructureDefaults.BEDROCK_SHAFT_MAX_WORLD_Y;
   }

   static boolean isConnectorWorldY(final int templateY) {
      return templateY >= OilStructureDefaults.CONNECTOR_MIN_WORLD_Y
         && templateY <= OilStructureDefaults.CONNECTOR_MAX_WORLD_Y;
   }

   private static int surfaceY(final LevelReader level, final int x, final int z, final int templateY) {
      Heightmap.Types heightmap = Heightmap.Types.WORLD_SURFACE_WG;
      if (level instanceof ServerLevel) {
         heightmap = Heightmap.Types.WORLD_SURFACE;
      }
      return level.getHeight(heightmap, x, z) - 1 + templateY;
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
