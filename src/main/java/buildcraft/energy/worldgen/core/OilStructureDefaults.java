package buildcraft.energy.worldgen.core;

public final class OilStructureDefaults {
   public static final int TEMPLATE_SIZE = 91;
   public static final int TEMPLATE_CENTER = TEMPLATE_SIZE / 2;

   /** Rich/patch tiers form contiguous 8×8-chunk (128×128 block) sectors. */
   public static final int SLICE_SECTOR_CHUNKS = 8;

   /** Template Y anchor for sphere center in well NBT (paired with {@code OilWellProjectionProcessor}). */
   public static final int SPHERE_TEMPLATE_CENTER_Y = -38;
   /** Template Y for bedrock spring marker block. */
   public static final int SPRING_TEMPLATE_Y = -57;

   /** Base seed; each {@link PlacementSet} offsets by ordinal so placement grids stay independent. */
   private static final int BASE_SEED = 0x5046_B4_E4;

   public enum PlacementSet {
      /** BC rarity_filter 667 (~0.15%/chunk on eligible slices). */
      NORMAL(26, 8),
      /** ~40% fewer patch placements vs 7/5 (spacing² scales grid density). */
      PATCH_DESERT(8, 2),
      PATCH_DESERT_DENSE(7, 2),
      PATCH_OCEAN(8, 2),
      PATCH_OCEAN_DENSE(5, 2);

      private final int spacing;
      private final int separation;

      PlacementSet(int spacing, int separation) {
         this.spacing = spacing;
         this.separation = separation;
      }

      public int spacing() {
         return this.spacing;
      }

      public int separation() {
         return this.separation;
      }

      public int salt() {
         return BASE_SEED + this.ordinal();
      }
   }

   /** Salt for rich-sector slicing — separate offset so sector rolls do not track structure grids. */
   public static long sectorRollSalt() {
      return BASE_SEED + 0x20L;
   }

   private OilStructureDefaults() {
   }
}
