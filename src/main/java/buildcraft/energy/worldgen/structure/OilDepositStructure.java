package buildcraft.energy.worldgen.structure;

import buildcraft.energy.worldgen.core.OilStructureDefaults;
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
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public final class OilDepositStructure extends Structure {
   public static final MapCodec<OilDepositStructure> CODEC = RecordCodecBuilder.<OilDepositStructure>mapCodec(
      instance -> instance.group(
         settingsCodec(instance),
         StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(OilDepositStructure::startPool),
         Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(OilDepositStructure::startJigsawName),
         Codec.intRange(0, 20).fieldOf("size").forGetter(OilDepositStructure::maxDepth),
         HeightProvider.CODEC.fieldOf("start_height").forGetter(OilDepositStructure::startHeight),
         Codec.BOOL.fieldOf("use_expansion_hack").forGetter(OilDepositStructure::useExpansionHack),
         Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(OilDepositStructure::projectStartToHeightmap),
         JigsawStructure.MaxDistance.CODEC.fieldOf("max_distance_from_center").forGetter(OilDepositStructure::maxDistanceFromCenter),
         Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(OilDepositStructure::poolAliases),
         DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DimensionPadding.ZERO).forGetter(OilDepositStructure::dimensionPadding),
         LiquidSettings.CODEC.optionalFieldOf("liquid_settings", LiquidSettings.APPLY_WATERLOGGING).forGetter(OilDepositStructure::liquidSettings),
         OilStructureSpawnConditions.Tier.CODEC.fieldOf("tier").forGetter(OilDepositStructure::tier)
      ).apply(instance, OilDepositStructure::new)
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
   private final OilStructureSpawnConditions.Tier tier;

   public OilDepositStructure(
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
      LiquidSettings liquidSettings,
      OilStructureSpawnConditions.Tier tier
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
      this.tier = tier;
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

   public OilStructureSpawnConditions.Tier tier() {
      return this.tier;
   }

   @Override
   public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
      if (!OilStructureSpawnConditions.canSpawn(this.tier, context)) {
         return Optional.empty();
      }
      ChunkPos chunkPos = context.chunkPos();
      // Skip steep sites (cliffs, mountain edges): reject if the surface spread over a 3x3 sample is too large.
      int cx = chunkPos.getMiddleBlockX();
      int cz = chunkPos.getMiddleBlockZ();
      int r = OilStructureDefaults.FLATNESS_SAMPLE_RADIUS;
      int minH = Integer.MAX_VALUE;
      int maxH = Integer.MIN_VALUE;
      for (int dx = -r; dx <= r; dx += r) {
         for (int dz = -r; dz <= r; dz += r) {
            int h = context.chunkGenerator().getBaseHeight(cx + dx, cz + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            minH = Math.min(minH, h);
            maxH = Math.max(maxH, h);
         }
      }
      if (maxH - minH > OilStructureDefaults.MAX_SURFACE_SLOPE) {
         return Optional.empty();
      }
      int height = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
      // With start_jigsaw_name the placer aligns the template's centre anchor onto this pos, so the start
      // IS the oil column position: the chunk middle, matching locate_offset (8,0,8).
      BlockPos startPos = new BlockPos(chunkPos.getMiddleBlockX(), height, chunkPos.getMiddleBlockZ());
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
      return BCEnergyStructures.OIL_DEPOSIT_TYPE;
   }
}
