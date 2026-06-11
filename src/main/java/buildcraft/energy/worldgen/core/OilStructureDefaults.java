package buildcraft.energy.worldgen.core;

public final class OilStructureDefaults {
   public static final int TEMPLATE_SIZE = 91;
   public static final int TEMPLATE_CENTER = TEMPLATE_SIZE / 2;

   /** Tuned toward BC rarity_filter chance 667 (~0.15%/chunk on eligible slices). */
   public static final int NORMAL_SPACING = 26;
   public static final int NORMAL_SEPARATION = 8;
   public static final int NORMAL_SALT = 0x5046_B4_E4;

   /**
    * Rich desert sectors: ~0.7%/chunk inside a rich sector (~4× normal), grouped deposits per sector.
    */
   public static final int RICH_SPACING = 12;
   public static final int RICH_SEPARATION = 4;

   /**
    * Rich ocean sectors: ~1%/chunk inside a patch sector (~6× normal), still sparser than prior tuning.
    */
   public static final int OCEAN_PATCH_SPACING = 10;
   public static final int OCEAN_PATCH_SEPARATION = 3;
   public static final int PATCH_SALT = 0x5046_B4_E6;

   /** Rich/patch tiers form contiguous 8×8-chunk (128×128 block) sectors. */
   public static final int SLICE_SECTOR_CHUNKS = 8;

   /** Salt for sector slicing in {@link OilStructureSpawnConditions} (independent of structure-set salt). */
   public static final long SLICE_ROLL_SALT = 0x5046_B4_E7L;

   /** Template Y anchor for sphere center in well NBT (paired with {@code OilWellProjectionProcessor}). */
   public static final int SPHERE_TEMPLATE_CENTER_Y = -38;
   /** Template Y for bedrock spring marker block. */
   public static final int SPRING_TEMPLATE_Y = -57;

   private OilStructureDefaults() {
   }
}
