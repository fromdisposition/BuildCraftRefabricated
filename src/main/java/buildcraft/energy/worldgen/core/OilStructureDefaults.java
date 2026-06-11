package buildcraft.energy.worldgen.core;

public final class OilStructureDefaults {
   public static final int TEMPLATE_SIZE = 91;
   public static final int TEMPLATE_CENTER = TEMPLATE_SIZE / 2;

   /** Rich/patch tiers form contiguous 8×8-chunk (128×128 block) sectors. */
   public static final int SLICE_SECTOR_CHUNKS = 8;

   /** Surface tendril/spout template Y ({@code GravityProcessor}: {@code heightmap - 1 + templateY}). */
   public static final int SURFACE_TEMPLATE_Y = 0;

   /** Fixed overworld Y for the underground deposit body (template Y equals world Y). */
   public static final int DEPOSIT_MIN_WORLD_Y = -42;
   public static final int DEPOSIT_MAX_WORLD_Y = -12;
   public static final int SPHERE_TEMPLATE_CENTER_Y = (DEPOSIT_MIN_WORLD_Y + DEPOSIT_MAX_WORLD_Y) / 2;

   /** Connector from deposit top ({@code DEPOSIT_MAX_WORLD_Y + 1}) up to just below the surface film. */
   public static final int CONNECTOR_MIN_WORLD_Y = DEPOSIT_MAX_WORLD_Y + 1;
   /** Upper template bound; processor clips each column at {@code heightmap - 2}. */
   public static final int CONNECTOR_MAX_WORLD_Y = 319;

   /** Bedrock spring marker (pinned to {@code minY} after gravity). */
   public static final int SPRING_TEMPLATE_Y = -57;
   public static final int BEDROCK_SHAFT_MIN_WORLD_Y = SPRING_TEMPLATE_Y + 1;
   public static final int BEDROCK_SHAFT_MAX_WORLD_Y = DEPOSIT_MIN_WORLD_Y - 1;

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
