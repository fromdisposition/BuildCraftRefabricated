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
//? if >= 26.1.3 {
//?} else {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Runs after {@link net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor}.
 * Surface tendrils/spouts keep gravity; the deposit and bedrock shaft use fixed world Y; the connector
 * is a fixed bridge ({@code template 100+}) from Y {@code -11} up to the terrain-relative shaft ({@code template -11..-1}).
 * Bridge must be handled before the surface gravity pass-through ({@code templateY >= 0}).
 */
//? if >= 26.1.3 {
/*public final class OilWellProjectionProcessor implements StructureProcessor {*/
//?} else {
public final class OilWellProjectionProcessor extends StructureProcessor {
//?}
   public static final MapCodec<OilWellProjectionProcessor> CODEC = MapCodec.unit(OilWellProjectionProcessor::new);

   //? if >= 26.1.3 {
   /*@Override
   public MapCodec<OilWellProjectionProcessor> codec() {
      return CODEC;
   }

   @Override
   public StructureTemplate.StructureBlockInfo processBlock(
      final LevelReader level,
      final BlockPos targetPosition,
      final BlockPos referencePos,
      final BlockPos originalPos,
      final StructureTemplate.StructureBlockInfo processedBlockInfo,
      final StructurePlaceSettings settings
   ) {
      int templateY = originalPos.getY();*/
   //?} else {
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
   //?}
      BlockPos pos = processedBlockInfo.pos();
      int x = pos.getX();
      int z = pos.getZ();

      if (templateY == OilStructureDefaults.SPRING_TEMPLATE_Y) {
         return new StructureTemplate.StructureBlockInfo(
            new BlockPos(x, minBuildY(level), z), processedBlockInfo.state(), processedBlockInfo.nbt()
         );
      }

      if (isConnectorBridgeTemplateY(templateY)) {
         return projectConnectorBridge(level, x, z, processedBlockInfo, templateY);
      }

      if (isDepositWorldY(templateY)) {
         return fixedY(processedBlockInfo, templateY);
      }

      if (isBedrockShaftWorldY(templateY)) {
         return fixedY(processedBlockInfo, templateY);
      }

      if (isConnectorTerrainTemplateY(templateY)) {
         return projectConnectorTerrain(level, x, z, processedBlockInfo, templateY);
      }

      if (templateY >= OilStructureDefaults.SURFACE_TEMPLATE_Y) {
         return processedBlockInfo;
      }

      return processedBlockInfo;
   }

   private static StructureTemplate.StructureBlockInfo projectConnectorBridge(
      final LevelReader level,
      final int x,
      final int z,
      final StructureTemplate.StructureBlockInfo processedBlockInfo,
      final int templateY
   ) {
      int worldY = OilStructureDefaults.CONNECTOR_MIN_WORLD_Y + templateY - OilStructureDefaults.CONNECTOR_BRIDGE_TEMPLATE_BASE;
      int terrainBottom = surfaceY(level, x, z, OilStructureDefaults.CONNECTOR_TERRAIN_MIN_TEMPLATE_Y);
      if (worldY >= terrainBottom) {
         return null;
      }
      return fixedY(processedBlockInfo, worldY);
   }

   private static StructureTemplate.StructureBlockInfo projectConnectorTerrain(
      final LevelReader level,
      final int x,
      final int z,
      final StructureTemplate.StructureBlockInfo processedBlockInfo,
      final int templateY
   ) {
      int connectorTop = surfaceY(level, x, z, OilStructureDefaults.CONNECTOR_TERRAIN_MAX_TEMPLATE_Y);
      int worldY = surfaceY(level, x, z, templateY);
      if (worldY > connectorTop) {
         return null;
      }
      return fixedY(processedBlockInfo, worldY);
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

   static boolean isConnectorBridgeTemplateY(final int templateY) {
      return templateY >= OilStructureDefaults.CONNECTOR_BRIDGE_TEMPLATE_BASE
         && templateY < OilStructureDefaults.CONNECTOR_BRIDGE_TEMPLATE_BASE + OilStructureDefaults.CONNECTOR_BRIDGE_LAYER_COUNT;
   }

   static boolean isConnectorTerrainTemplateY(final int templateY) {
      return templateY >= OilStructureDefaults.CONNECTOR_TERRAIN_MIN_TEMPLATE_Y
         && templateY <= OilStructureDefaults.CONNECTOR_TERRAIN_MAX_TEMPLATE_Y;
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

   //? if >= 26.1.3 {
   //?} else {
   @Override
   protected StructureProcessorType<?> getType() {
      return BCEnergyStructureProcessorTypes.OIL_WELL_PROJECTION;
   }
   //?}
}
