package buildcraft.energy.worldgen.core;

public final class OilStructureDefaults {
   public static final int TEMPLATE_SIZE = 91;
   public static final int TEMPLATE_CENTER = TEMPLATE_SIZE / 2;

   /** Flatness gate: sample the surface height on a 3x3 ring at this radius and skip the deposit if the spread
    *  exceeds {@link #MAX_SURFACE_SLOPE}. Bigger radius / smaller slope = flatter sites but rarer oil. */
   public static final int FLATNESS_SAMPLE_RADIUS = 12;
   public static final int MAX_SURFACE_SLOPE = 8;

   /** Surface tendril/spout template Y ({@code GravityProcessor}: {@code heightmap - 1 + templateY}). */
   public static final int SURFACE_TEMPLATE_Y = 0;

   /** Extra single-block spire above a large well's spout so the tip is clearly visible (blocks). */
   public static final int LARGE_SPOUT_TIP_HEIGHT = 6;

   /**
    * Every deposit sphere sits with its BOTTOM on this Y — one block above the first bedrock (the bedrock
    * gradient starts at -60, and the jigsaw replaces whatever the template covers, bedrock included, so the
    * sphere must not reach into it). The sphere centre is radius-dependent: {@code DEPOSIT_BOTTOM_WORLD_Y + r}.
    */
   public static final int DEPOSIT_BOTTOM_WORLD_Y = -59;
   /** Largest sphere radius any well template uses (giant). */
   public static final int MAX_SPHERE_RADIUS = 15;

   /** Fixed overworld Y band for the underground deposit body (template Y equals world Y). */
   public static final int DEPOSIT_MIN_WORLD_Y = DEPOSIT_BOTTOM_WORLD_Y;
   public static final int DEPOSIT_MAX_WORLD_Y = DEPOSIT_BOTTOM_WORLD_Y + 2 * MAX_SPHERE_RADIUS;

   /** Connector from deposit top ({@code DEPOSIT_MAX_WORLD_Y + 1}) up to just below the surface film. */
   public static final int CONNECTOR_MIN_WORLD_Y = DEPOSIT_MAX_WORLD_Y + 1;
   /**
    * Fixed bridge template band ({@code CONNECTOR_MIN_WORLD_Y + (templateY - base)}). Ends where the
    * terrain-relative shaft begins ({@code heightmap - 1 + CONNECTOR_TERRAIN_MIN_TEMPLATE_Y}).
    */
   public static final int CONNECTOR_BRIDGE_TEMPLATE_BASE = 100;
   public static final int CONNECTOR_BRIDGE_LAYER_COUNT = 320;
   /** Terrain-relative shaft template band ({@code heightmap - 1 + templateY}), BC-style {@code -11..-1}. */
   public static final int CONNECTOR_TERRAIN_MIN_TEMPLATE_Y = -11;
   public static final int CONNECTOR_TERRAIN_MAX_TEMPLATE_Y = -1;

   /**
    * Spring marker, placed at this fixed world Y — directly under the sphere bottom so the tile's
    * advancement check ({@code springPos.above()} is the last pumped source) can actually pass.
    * Must stay OUTSIDE the deposit band or the projection decoder would read it as deposit oil.
    */
   public static final int SPRING_TEMPLATE_Y = DEPOSIT_BOTTOM_WORLD_Y - 1;

   /**
    * Well templates use negative template Y as placement markers (deposit/shaft/spring), but Minecraft's structure
    * NBT format requires every stored block position to be non-negative -- Structure Blocks and NBT tooling reject
    * negatives, and the jigsaw placer drops out-of-bounds blocks so the deposit never spawns. Every well block's Y is
    * shifted up by this constant on export to keep all positions {@code >= 0}, and
    * {@link buildcraft.energy.worldgen.processor.OilWellProjectionProcessor} subtracts it back to recover the semantic
    * template Y. Equals {@code -SPRING_TEMPLATE_Y}, the lowest template Y any well uses.
    */
   public static final int WELL_TEMPLATE_Y_OFFSET = -SPRING_TEMPLATE_Y;

   /** Base seed; each {@link PlacementSet} offsets by ordinal so placement grids stay independent. */
   private static final int BASE_SEED = 0x5046_B4_E4;

   public enum PlacementSet {
      /** BC rarity_filter 667 (~0.15%/chunk on eligible slices). */
      NORMAL(26, 8),
      /** One field per ~32x32 desert/ocean chunks; separation 16 keeps 250-block-wide fields from touching. */
      FIELD_DESERT(32, 16),
      FIELD_OCEAN(32, 16);

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

   private OilStructureDefaults() {
   }
}
