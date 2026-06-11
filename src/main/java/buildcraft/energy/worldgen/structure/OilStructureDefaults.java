package buildcraft.energy.worldgen.structure;

public final class OilStructureDefaults {
   public static final int TEMPLATE_SIZE = 91;
   public static final int TEMPLATE_CENTER = TEMPLATE_SIZE / 2;

   /** ~0.15%/chunk equivalent for overworld normal tier. */
   public static final int NORMAL_SPACING = 48;
   public static final int NORMAL_SEPARATION = 16;
   public static final int NORMAL_SALT = 0x5046_B4_E4;

   /** ~0.3%/chunk equivalent in rich biomes. */
   public static final int RICH_SPACING = 32;
   public static final int RICH_SEPARATION = 10;
   public static final int RICH_SALT = 0x5046_B4_E5;

   /** ~2%/chunk equivalent for patch tiers. */
   public static final int PATCH_SPACING = 12;
   public static final int PATCH_SEPARATION = 4;
   public static final int PATCH_SALT = 0x5046_B4_E6;

   public static final int WEIGHT_LARGE = 20;
   public static final int WEIGHT_MEDIUM = 60;
   public static final int WEIGHT_LAKE = 20;

   private OilStructureDefaults() {
   }
}
