package buildcraft.energy.worldgen.structure;

public final class OilStructureDefaults {
   public static final int TEMPLATE_SIZE = 91;
   public static final int TEMPLATE_CENTER = TEMPLATE_SIZE / 2;

   /** Tuned toward BC rarity_filter chance 667 (~0.15%/chunk on eligible slices). */
   public static final int NORMAL_SPACING = 26;
   public static final int NORMAL_SEPARATION = 8;
   public static final int NORMAL_SALT = 0x5046_B4_E4;

   /** ~1%/chunk on rich-eligible desert slices (see {@code oilDesertRichChancePercent}). */
   public static final int RICH_SPACING = 10;
   public static final int RICH_SEPARATION = 4;

   /** ~2%/chunk on patch-eligible ocean slices (see {@code oilOceanPatchChancePercent}). */
   public static final int OCEAN_PATCH_SPACING = 7;
   public static final int OCEAN_PATCH_SEPARATION = 3;
   public static final int PATCH_SALT = 0x5046_B4_E6;

   /** Salt for per-chunk desert rich / ocean patch slicing in {@link OilStructureSpawnConditions}. */
   public static final long CHUNK_ROLL_SALT = 0x5046_B4_E7L;

   /** Template Y anchor for sphere center in well NBT (paired with {@code OilWellProjectionProcessor}). */
   public static final int SPHERE_TEMPLATE_CENTER_Y = -38;
   /** Template Y for bedrock spring marker block. */
   public static final int SPRING_TEMPLATE_Y = -57;

   /** BC type_weight_large / medium / lake (20 / 60 / 20). */
   public static final int WEIGHT_LARGE = 20;
   public static final int WEIGHT_MEDIUM = 60;
   public static final int WEIGHT_LAKE = 20;

   private OilStructureDefaults() {
   }
}
