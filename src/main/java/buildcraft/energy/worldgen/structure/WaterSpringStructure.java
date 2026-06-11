package buildcraft.energy.worldgen.structure;

import buildcraft.energy.worldgen.core.WaterSpringDefaults;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public final class WaterSpringStructure extends Structure {
   public static final MapCodec<WaterSpringStructure> CODEC = RecordCodecBuilder.<WaterSpringStructure>mapCodec(
      instance -> instance.group(
         settingsCodec(instance),
         StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(WaterSpringStructure::startPool),
         Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(WaterSpringStructure::startJigsawName),
         Codec.intRange(0, 20).fieldOf("size").forGetter(WaterSpringStructure::maxDepth),
         HeightProvider.CODEC.fieldOf("start_height").forGetter(WaterSpringStructure::startHeight),
         Codec.BOOL.fieldOf("use_expansion_hack").forGetter(WaterSpringStructure::useExpansionHack),
         Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(WaterSpringStructure::projectStartToHeightmap),
         JigsawStructure.MaxDistance.CODEC.fieldOf("max_distance_from_center").forGetter(WaterSpringStructure::maxDistanceFromCenter),
         Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(WaterSpringStructure::poolAliases),
         DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DimensionPadding.ZERO).forGetter(WaterSpringStructure::dimensionPadding),
         LiquidSettings.CODEC.optionalFieldOf("liquid_settings", LiquidSettings.APPLY_WATERLOGGING).forGetter(WaterSpringStructure::liquidSettings)
      ).apply(instance, WaterSpringStructure::new)
   );

   private final Holder<StructureTemplatePool> startPool;
   private final Optional<Identifier> startJigsawName;
   private final int maxDepth;
   private final HeightProvider startHeight;
   private final boolean useExpansionHack;
   private final Optional<Heightmap.Types> projectStartToHeightmap;
   private final JigsawStructure.MaxDistance maxDistanceFromCenter;
   private final List<PoolAliasBinding> poolAliases;
   private final DimensionPadding dimensionPadding;
   private final LiquidSettings liquidSettings;

   public WaterSpringStructure(
      StructureSettings settings,
      Holder<StructureTemplatePool> startPool,
      Optional<Identifier> startJigsawName,
      int maxDepth,
      HeightProvider startHeight,
      boolean useExpansionHack,
      Optional<Heightmap.Types> projectStartToHeightmap,
      JigsawStructure.MaxDistance maxDistanceFromCenter,
      List<PoolAliasBinding> poolAliases,
      DimensionPadding dimensionPadding,
      LiquidSettings liquidSettings
   ) {
      super(settings);
      this.startPool = startPool;
      this.startJigsawName = startJigsawName;
      this.maxDepth = maxDepth;
      this.startHeight = startHeight;
      this.useExpansionHack = useExpansionHack;
      this.projectStartToHeightmap = projectStartToHeightmap;
      this.maxDistanceFromCenter = maxDistanceFromCenter;
      this.poolAliases = poolAliases;
      this.dimensionPadding = dimensionPadding;
      this.liquidSettings = liquidSettings;
   }

   public Holder<StructureTemplatePool> startPool() {
      return this.startPool;
   }

   public Optional<Identifier> startJigsawName() {
      return this.startJigsawName;
   }

   public int maxDepth() {
      return this.maxDepth;
   }

   public HeightProvider startHeight() {
      return this.startHeight;
   }

   public boolean useExpansionHack() {
      return this.useExpansionHack;
   }

   public Optional<Heightmap.Types> projectStartToHeightmap() {
      return this.projectStartToHeightmap;
   }

   public JigsawStructure.MaxDistance maxDistanceFromCenter() {
      return this.maxDistanceFromCenter;
   }

   public List<PoolAliasBinding> poolAliases() {
      return this.poolAliases;
   }

   public DimensionPadding dimensionPadding() {
      return this.dimensionPadding;
   }

   public LiquidSettings liquidSettings() {
      return this.liquidSettings;
   }

   @Override
   public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
      if (!WaterSpringSpawnConditions.canSpawn(context)) {
         return Optional.empty();
      }

      ChunkPos chunkPos = context.chunkPos();
      int x = chunkPos.getMinBlockX() + context.random().nextInt(16);
      int z = chunkPos.getMinBlockZ() + context.random().nextInt(16);
      BlockPos startPos = new BlockPos(x, WaterSpringDefaults.SPRING_TEMPLATE_Y, z);
      return JigsawPlacement.addPieces(
         context,
         this.startPool,
         this.startJigsawName,
         this.maxDepth,
         startPos,
         this.useExpansionHack,
         this.projectStartToHeightmap,
         this.maxDistanceFromCenter,
         PoolAliasLookup.create(this.poolAliases, startPos, context.seed()),
         this.dimensionPadding,
         this.liquidSettings
      );
   }

   @Override
   public StructureType<?> type() {
      return BCEnergyStructures.WATER_SPRING_TYPE;
   }
}
