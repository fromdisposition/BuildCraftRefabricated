package buildcraft.energy.worldgen.structure;

import buildcraft.fabric.BCRegistries;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Jigsaw pool element that marks placed {@code buildcraftenergy:oil} for vanilla fluid post-processing. */
public final class OilDepositPoolElement extends SinglePoolElement {
   private static final Identifier OIL_BLOCK_ID = BCRegistries.id("buildcraftenergy", "oil");

   public static final MapCodec<OilDepositPoolElement> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec())
         .apply(instance, OilDepositPoolElement::new)
   );

   private OilDepositPoolElement(
      Either<Identifier, StructureTemplate> template,
      Holder<StructureProcessorList> processors,
      StructureTemplatePool.Projection projection,
      Optional<LiquidSettings> overrideLiquidSettings
   ) {
      super(template, processors, projection, overrideLiquidSettings);
   }

   public static Function<StructureTemplatePool.Projection, OilDepositPoolElement> of(
      String location,
      Holder<StructureProcessorList> processors
   ) {
      return projection -> new OilDepositPoolElement(Either.left(Identifier.parse(location)), processors, projection, Optional.empty());
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
      if (!super.place(structureTemplateManager, level, structureManager, generator, position, referencePos, rotation, chunkBB, random, liquidSettings, keepJigsaws)) {
         return false;
      }

      StructureTemplate template = this.template.map(structureTemplateManager::getOrCreate, Function.identity());
      StructurePlaceSettings settings = this.getSettings(rotation, chunkBB, liquidSettings, keepJigsaws);
      List<StructureTemplate.StructureBlockInfo> blocks = StructureTemplate.processBlockInfos(
         level, position, referencePos, settings, template.filterBlocks(BlockPos.ZERO, settings, Blocks.AIR)
      );
      for (StructureTemplate.StructureBlockInfo block : blocks) {
         BlockPos pos = block.pos();
         if (!chunkBB.isInside(pos)) {
            continue;
         }
         if (OIL_BLOCK_ID.equals(BuiltInRegistries.BLOCK.getKey(block.state().getBlock()))) {
            level.getChunk(pos).markPosForPostprocessing(pos);
         }
      }

      return true;
   }

   @Override
   public StructurePoolElementType<?> getType() {
      return BCEnergyStructures.OIL_DEPOSIT_POOL_ELEMENT;
   }
}
