package buildcraft.energy.worldgen.structure;

import buildcraft.fabric.BCRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class BCEnergyStructures {
   public static final ResourceKey<Structure> OIL_DEPOSIT_NORMAL = ResourceKey.create(
      Registries.STRUCTURE, BCRegistries.id("buildcraftenergy", "oil_deposit_normal")
   );
   public static final ResourceKey<Structure> OIL_DEPOSIT_RICH = ResourceKey.create(
      Registries.STRUCTURE, BCRegistries.id("buildcraftenergy", "oil_deposit_rich")
   );
   public static final ResourceKey<Structure> OIL_DEPOSIT_PATCH_DESERT = ResourceKey.create(
      Registries.STRUCTURE, BCRegistries.id("buildcraftenergy", "oil_deposit_patch_desert")
   );
   public static final ResourceKey<Structure> OIL_DEPOSIT_PATCH_OCEAN = ResourceKey.create(
      Registries.STRUCTURE, BCRegistries.id("buildcraftenergy", "oil_deposit_patch_ocean")
   );

   public static final ResourceKey<StructureSet> OIL_DEPOSIT_NORMAL_SET = ResourceKey.create(
      Registries.STRUCTURE_SET, BCRegistries.id("buildcraftenergy", "oil_deposit_normal")
   );
   public static final ResourceKey<StructureSet> OIL_DEPOSIT_RICH_SET = ResourceKey.create(
      Registries.STRUCTURE_SET, BCRegistries.id("buildcraftenergy", "oil_deposit_rich")
   );
   public static final ResourceKey<StructureSet> OIL_DEPOSIT_PATCH_DESERT_SET = ResourceKey.create(
      Registries.STRUCTURE_SET, BCRegistries.id("buildcraftenergy", "oil_deposit_patch_desert")
   );
   public static final ResourceKey<StructureSet> OIL_DEPOSIT_PATCH_OCEAN_SET = ResourceKey.create(
      Registries.STRUCTURE_SET, BCRegistries.id("buildcraftenergy", "oil_deposit_patch_ocean")
   );

   public static StructureType<OilDepositStructure> OIL_DEPOSIT_TYPE;

   private BCEnergyStructures() {
   }

   public static void registerStructureType() {
      if (OIL_DEPOSIT_TYPE == null) {
         OIL_DEPOSIT_TYPE = Registry.register(
            BuiltInRegistries.STRUCTURE_TYPE,
            BCRegistries.id("buildcraftenergy", "oil_deposit"),
            () -> OilDepositStructure.CODEC
         );
      }
   }
}
