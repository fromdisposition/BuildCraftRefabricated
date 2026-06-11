package buildcraft.energy.worldgen.core;

public final class OilStructureDefaults {
   public static final int TEMPLATE_SIZE = 91;
   public static final int TEMPLATE_CENTER = TEMPLATE_SIZE / 2;

   /** Tuned toward BC rarity_filter chance 667 (~0.15%/chunk on eligible slices). */
   public static final int NORMAL_SPACING = 26;
   public static final int NORMAL_SEPARATION = 8;
   public static final int NORMAL_SALT = 0x5046_B4_E4;

   /**
    * Rich desert patch sectors ({@code oilDesertRichChancePercent}): ~4%/chunk inside a rich sector
    * ({@link #SLICE_SECTOR_CHUNKS}×{@link #SLICE_SECTOR_CHUNKS}), ~0.6 deposits per 64×64 block patch.
    */
   public static final int RICH_SPACING = 5;
   public static final int RICH_SEPARATION = 2;

   /**
    * Ocean patch sectors ({@code oilOceanPatchChancePercent}): ~6%/chunk inside a patch sector,
    * ~1 deposit per 64×64 block patch on average.
    */
   public static final int OCEAN_PATCH_SPACING = 4;
   public static final int OCEAN_PATCH_SEPARATION = 2;
   public static final int PATCH_SALT = 0x5046_B4_E6;

   /** Rich/patch tiers apply to contiguous sector blocks, not per-chunk salt (BC oil_ocean patches). */
   public static final int SLICE_SECTOR_CHUNKS = 4;

   /** Salt for sector slicing in {@link OilStructureSpawnConditions} (independent of structure-set salt). */
   public static final long SLICE_ROLL_SALT = 0x5046_B4_E7L;

   /** Template Y anchor for sphere center in well NBT (paired with {@code OilWellProjectionProcessor}). */
   public static final int SPHERE_TEMPLATE_CENTER_Y = -38;
   /** Template Y for bedrock spring marker block. */
   public static final int SPRING_TEMPLATE_Y = -57;

   private OilStructureDefaults() {
   }
}
