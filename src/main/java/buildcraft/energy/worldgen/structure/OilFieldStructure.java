package buildcraft.energy.worldgen.structure;

import buildcraft.energy.worldgen.core.OilStructureDefaults;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * An oil field: ONE structure that scatters several wells/lakes around its centre, mineshaft-style
 * (many pieces, one structure). {@code /locate} therefore points at the middle of a real cluster,
 * and rarity is plain vanilla structure_set spacing — no custom sector hashing.
 */
public final class OilFieldStructure extends Structure {
   public static final MapCodec<OilFieldStructure> CODEC = RecordCodecBuilder.<OilFieldStructure>mapCodec(
      instance -> instance.group(
         settingsCodec(instance),
         StructureTemplatePool.CODEC.fieldOf("pool").forGetter(s -> s.pool),
         net.minecraft.util.valueproviders.IntProviders.codec(1, 32).fieldOf("count").forGetter(s -> s.count),
         // Cap: spot radius + template half-width (45) must stay within the vanilla 8-chunk (128-block)
         // structure-reference scan, or the farthest pieces silently never place.
         Codec.intRange(16, 83).fieldOf("spread_radius").forGetter(s -> s.spreadRadius),
         OilStructureSpawnConditions.Tier.CODEC.fieldOf("tier").forGetter(s -> s.tier)
      ).apply(instance, OilFieldStructure::new)
   );

   /** Cluster spots are kept at least this far apart so wells stand clearly separated, not one smear. */
   private static final int MIN_SPOT_SPACING = 40;

   private final Holder<StructureTemplatePool> pool;
   private final IntProvider count;
   private final int spreadRadius;
   private final OilStructureSpawnConditions.Tier tier;

   public OilFieldStructure(
      StructureSettings settings,
      Holder<StructureTemplatePool> pool,
      IntProvider count,
      int spreadRadius,
      OilStructureSpawnConditions.Tier tier
   ) {
      super(settings);
      this.pool = pool;
      this.count = count;
      this.spreadRadius = spreadRadius;
      this.tier = tier;
   }

   @Override
   public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
      if (!OilStructureSpawnConditions.canSpawn(this.tier, context)) {
         return Optional.empty();
      }
      ChunkPos chunkPos = context.chunkPos();
      BlockPos center = new BlockPos(chunkPos.getMiddleBlockX(), 0, chunkPos.getMiddleBlockZ());
      return Optional.of(new GenerationStub(center, builder -> this.generatePieces(builder, context, center)));
   }

   private void generatePieces(StructurePiecesBuilder builder, GenerationContext context, BlockPos center) {
      RandomSource random = context.random();
      StructureTemplateManager templateManager = context.structureTemplateManager();
      int pieces = this.count.sample(random);

      int placed = 0;
      int[] xs = new int[pieces];
      int[] zs = new int[pieces];
      for (int attempt = 0; attempt < pieces * 16 && placed < pieces; attempt++) {
         // Uniform disc spot; the first piece sits on the exact centre so /locate lands on something.
         int dx = 0;
         int dz = 0;
         if (placed > 0) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(random.nextDouble()) * this.spreadRadius;
            dx = (int)Math.round(Math.cos(angle) * dist);
            dz = (int)Math.round(Math.sin(angle) * dist);
            boolean tooClose = false;
            for (int i = 0; i < placed; i++) {
               int ddx = xs[i] - dx;
               int ddz = zs[i] - dz;
               if (ddx * ddx + ddz * ddz < MIN_SPOT_SPACING * MIN_SPOT_SPACING) {
                  tooClose = true;
                  break;
               }
            }
            if (tooClose) {
               continue;
            }
         }

         // Per-spot flatness (same 3x3 sample as lone wells): rugged spots are simply re-rolled, so the
         // field flows around dunes and cliffs instead of being rejected or draped over them. The sample
         // uses WORLD_SURFACE_WG, which counts the water surface — ocean floors pass untouched.
         if (!isFlatEnough(context, center.getX() + dx, center.getZ() + dz)) {
            continue;
         }

         StructurePoolElement element = this.pool.value().getRandomTemplate(random);
         if (element == StructurePoolElement.empty()) {
            continue;
         }

         xs[placed] = dx;
         zs[placed] = dz;
         placed++;

         // Rotate around the template centre: anchor the rotated centre-offset onto the spot.
         Rotation rotation = Rotation.getRandom(random);
         int c = OilStructureDefaults.TEMPLATE_CENTER;
         BlockPos spot = center.offset(dx, 0, dz);
         BlockPos anchor = switch (rotation) {
            case NONE -> spot.offset(-c, 0, -c);
            case CLOCKWISE_90 -> spot.offset(c, 0, -c);
            case CLOCKWISE_180 -> spot.offset(c, 0, c);
            case COUNTERCLOCKWISE_90 -> spot.offset(-c, 0, c);
         };
         builder.addPiece(
            new PoolElementStructurePiece(
               templateManager,
               element,
               anchor,
               element.getGroundLevelDelta(),
               rotation,
               element.getBoundingBox(templateManager, anchor, rotation),
               JigsawStructure.DEFAULT_LIQUID_SETTINGS
            )
         );
      }
   }

   private static boolean isFlatEnough(GenerationContext context, int x, int z) {
      int r = OilStructureDefaults.FLATNESS_SAMPLE_RADIUS;
      int minH = Integer.MAX_VALUE;
      int maxH = Integer.MIN_VALUE;
      for (int dx = -r; dx <= r; dx += r) {
         for (int dz = -r; dz <= r; dz += r) {
            int h = context.chunkGenerator()
               .getBaseHeight(x + dx, z + dz, net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            minH = Math.min(minH, h);
            maxH = Math.max(maxH, h);
         }
      }
      return maxH - minH <= OilStructureDefaults.MAX_SURFACE_SLOPE;
   }

   @Override
   public StructureType<?> type() {
      return BCEnergyStructures.OIL_FIELD_TYPE;
   }
}
