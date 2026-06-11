package buildcraft.energy.generation.core;

import net.minecraft.resources.Identifier;

/** Synthetic BC oil patch at a column (replaces 1.12 GenLayer oil_ocean / oil_desert). */
enum OilPatchKind {
   NONE,
   OCEAN,
   DESERT;

   boolean isPatch() {
      return this != NONE;
   }

   Identifier designBiomeId() {
      return switch (this) {
         case OCEAN -> OilBiomePatches.OIL_OCEAN;
         case DESERT -> OilBiomePatches.OIL_DESERT;
         case NONE -> throw new IllegalStateException("no design biome for NONE");
      };
   }
}
