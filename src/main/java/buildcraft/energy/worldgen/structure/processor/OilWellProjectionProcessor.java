package buildcraft.energy.worldgen.structure.processor;

import buildcraft.energy.worldgen.structure.BCEnergyStructureProcessorTypes;
import buildcraft.energy.worldgen.structure.OilStructureDefaults;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
 * BC 8.0 well projection: surface tendril/spout/connector follow terrain; sphere and bedrock shaft use
 * {@code minY + sphere_center_offset} (BC {@code wellY = minY + 20 + rand}).
 */
public final class OilWellProjectionProcessor extends StructureProcessor {
   public static final MapCodec<OilWellProjectionProcessor> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
         Codec.INT.fieldOf("sphere_center_offset").forGetter(OilWellProjectionProcessor::sphereCenterOffset),
         Codec.INT.fieldOf("sphere_radius").forGetter(OilWellProjectionProcessor::sphereRadius),
         Codec.INT
            .optionalFieldOf("sphere_center_template_y", OilStructureDefaults.SPHERE_TEMPLATE_CENTER_Y)
            .forGetter(OilWellProjectionProcessor::sphereCenterTemplateY)
      ).apply(instance, OilWellProjectionProcessor::new)
   );

   private final int sphereCenterOffset;
   private final int sphereRadius;
   private final int sphereCenterTemplateY;

   public OilWellProjectionProcessor(final int sphereCenterOffset, final int sphereRadius, final int sphereCenterTemplateY) {
      this.sphereCenterOffset = sphereCenterOffset;
      this.sphereRadius = sphereRadius;
      this.sphereCenterTemplateY = sphereCenterTemplateY;
   }

   public int sphereCenterOffset() {
      return this.sphereCenterOffset;
   }

   public int sphereRadius() {
      return this.sphereRadius;
   }

   public int sphereCenterTemplateY() {
      return this.sphereCenterTemplateY;
   }

   private int sphereTopTemplateY() {
      return this.sphereCenterTemplateY + this.sphereRadius;
   }

   private static int minBuildY(final LevelReader level) {
      if (level instanceof LevelHeightAccessor heightAccessor) {
         return heightAccessor.getMinY();
      }
      return -64;
   }

   private static int surfaceY(final LevelReader level, final int x, final int z, final int templateY) {
      Heightmap.Types heightmap = Heightmap.Types.WORLD_SURFACE_WG;
      if (level instanceof ServerLevel) {
         heightmap = Heightmap.Types.WORLD_SURFACE;
      }
      return level.getHeight(heightmap, x, z) - 1 + templateY;
   }

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
         return new StructureTemplate.StructureBlockInfo(new BlockPos(x, minBuildY(level), z), processedBlockInfo.state(), processedBlockInfo.nbt());
      }

      if (usesTerrainProjection(templateY)) {
         return new StructureTemplate.StructureBlockInfo(
            new BlockPos(x, surfaceY(level, x, z, templateY), z), processedBlockInfo.state(), processedBlockInfo.nbt()
         );
      }

      int worldY = minBuildY(level) + this.sphereCenterOffset + (templateY - this.sphereCenterTemplateY);
      return new StructureTemplate.StructureBlockInfo(new BlockPos(x, worldY, z), processedBlockInfo.state(), processedBlockInfo.nbt());
   }

   private boolean usesTerrainProjection(final int templateY) {
      if (templateY >= 0) {
         return true;
      }
      int connectorTop = this.sphereTopTemplateY() + 1;
      return templateY >= connectorTop && templateY <= -1;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return BCEnergyStructureProcessorTypes.OIL_WELL_PROJECTION;
   }
}
