package buildcraft.fabric.fluid;

import java.util.ArrayList;
import java.util.List;

public record BcFluidWorldProperties(
   String baseName,
   int heat,
   int baseDensity,
   int baseViscosity,
   int boilPoint,
   int baseSpread,
   int density,
   int viscosity,
   int quanta,
   boolean gaseous,
   boolean sticky,
   boolean flammable,
   boolean denseFluid,
   boolean displacesWater,
   boolean floatsOnWater,
   int tickDelay,
   int slopeFindDistance,
   int dropOff
) {
   public static final int WATER_DENSITY = 1000;
   public static final int WATER_VISCOSITY = 1000;
   public static final int[][] FLUID_DATA = new int[][]{
      {900, 2000, 3, 6, 5263440, 328965, 1, 1},
      {1200, 4000, 3, 4, 1052432, 4329538, 1, 0},
      {850, 1800, 3, 6, 10522399, 4338976, 1, 1},
      {950, 1600, 3, 5, 8875639, 4334628, 1, 1},
      {750, 1400, 2, 8, 14987128, 11828992, 0, 1},
      {600, 800, 2, 7, 16756543, 14712576, 0, 1},
      {700, 1000, 2, 7, 15902464, 12879616, 0, 1},
      {400, 600, 1, 8, 16777008, 14995200, 0, 1},
      {650, 900, 1, 9, 16176896, 12891904, 0, 1},
      {300, 500, 0, 10, 16447024, 14735616, 0, 1}
   };
   public static final String[] FLUID_NAMES = new String[]{
      "oil", "oil_residue", "oil_heavy", "oil_dense", "oil_distilled", "fuel_dense", "fuel_mixed_heavy", "fuel_light", "fuel_mixed_light", "fuel_gaseous"
   };

   public static String regName(String baseName, int heat) {
      return baseName + (heat == 0 ? "" : "_heat_" + heat);
   }

   public String regName() {
      return regName(this.baseName, this.heat);
   }

   public static BcFluidWorldProperties compute(
      String baseName,
      int heat,
      int baseDensity,
      int baseViscosity,
      int boilPoint,
      int baseSpread,
      int texLight,
      int texDark,
      boolean stickyEnabled,
      int stickyFlag,
      boolean flammableEnabled,
      int flammableFlag
   ) {
      int viscosity = baseViscosity * (4 - heat) / 4;
      int density = baseDensity * (heat >= boilPoint ? -1 : 1);
      boolean gaseous = density < 0;
      boolean sticky = stickyEnabled && stickyFlag == 1;
      boolean flammable = flammableEnabled && flammableFlag == 1;
      boolean denseFluid = baseName.contains("oil_heavy") || baseName.contains("oil_dense") || baseName.contains("oil_residue");
      boolean displacesWater = !gaseous && density > WATER_DENSITY;
      boolean floatsOnWater = !gaseous && density > 0 && density < WATER_DENSITY;
      int quanta = baseSpread + (baseSpread > 6 ? heat : heat / 2);
      int tickDelay = Math.max(1, viscosity / 200);
      int slopeFindDistance;
      int dropOff;
      if (quanta <= 5) {
         slopeFindDistance = 2;
         dropOff = 2;
      } else if (quanta <= 6) {
         slopeFindDistance = 3;
         dropOff = 1;
      } else {
         slopeFindDistance = 4;
         dropOff = 1;
      }

      return new BcFluidWorldProperties(
         baseName,
         heat,
         baseDensity,
         baseViscosity,
         boilPoint,
         baseSpread,
         density,
         viscosity,
         quanta,
         gaseous,
         sticky,
         flammable,
         denseFluid,
         displacesWater,
         floatsOnWater,
         tickDelay,
         slopeFindDistance,
         dropOff
      );
   }
}
