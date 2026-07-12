package buildcraft.energy.worldgen.processor;

import buildcraft.energy.worldgen.core.WaterSpringDefaults;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
//? if >= 26.2 {
//?} else {
/*import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
*///?}
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Projects the template spring marker onto the top bedrock in {@code [minY, minY + 4]}. */
//? if >= 26.2 {
public final class WaterSpringBedrockProcessor implements StructureProcessor {
//?} else {
/*public final class WaterSpringBedrockProcessor extends StructureProcessor {
*///?}
   public static final MapCodec<WaterSpringBedrockProcessor> CODEC = MapCodec.unit(WaterSpringBedrockProcessor::new);

   //? if >= 26.2 {
   @Override
   public MapCodec<WaterSpringBedrockProcessor> codec() {
      return CODEC;
   }

   @Override
   public StructureTemplate.StructureBlockInfo processBlock(
      LevelReader level,
      BlockPos targetPosition,
      BlockPos referencePos,
      BlockPos originalPos,
      StructureTemplate.StructureBlockInfo processedBlockInfo,
      StructurePlaceSettings settings
   ) {
      if (originalPos.getY() != WaterSpringDefaults.SPRING_TEMPLATE_Y) {
   //?} else {
   /*@Override
   public StructureTemplate.StructureBlockInfo processBlock(
      LevelReader level,
      BlockPos targetPosition,
      BlockPos referencePos,
      StructureTemplate.StructureBlockInfo originalBlockInfo,
      StructureTemplate.StructureBlockInfo processedBlockInfo,
      StructurePlaceSettings settings
   ) {
      if (originalBlockInfo.pos().getY() != WaterSpringDefaults.SPRING_TEMPLATE_Y) {
   *///?}
         return processedBlockInfo;
      }

      BlockPos pos = processedBlockInfo.pos();
      BlockPos bedrock = findBedrock(level, pos.getX(), pos.getZ());
      if (bedrock == null) {
         return new StructureTemplate.StructureBlockInfo(pos, Blocks.AIR.defaultBlockState(), null);
      }

      return new StructureTemplate.StructureBlockInfo(bedrock, processedBlockInfo.state(), processedBlockInfo.nbt());
   }

   @Nullable
   public static BlockPos findBedrock(LevelReader level, int x, int z) {
      int minY = minBuildY(level);
      for (int y = minY + 4; y >= minY; y--) {
         BlockPos pos = new BlockPos(x, y, z);
         if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
            return pos;
         }
      }
      return null;
   }

   private static int minBuildY(LevelReader level) {
      if (level instanceof LevelHeightAccessor heightAccessor) {
         return heightAccessor.getMinY();
      }
      return -64;
   }

   //? if >= 26.2 {
   //?} else {
   /*@Override
   protected StructureProcessorType<?> getType() {
      return BCEnergyStructureProcessorTypes.WATER_SPRING_BEDROCK;
   }
   *///?}
}
