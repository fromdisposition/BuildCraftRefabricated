package buildcraft.energy.worldgen.structure;

import buildcraft.fabric.BCRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

public final class BCEnergyStructures {
   public static final ResourceKey<Structure> OIL_DEPOSIT_NORMAL = ResourceKey.create(
      Registries.STRUCTURE, BCRegistries.id("buildcraftenergy", "oil_deposit_normal")
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
   public static final ResourceKey<StructureSet> OIL_DEPOSIT_PATCH_DESERT_SET = ResourceKey.create(
      Registries.STRUCTURE_SET, BCRegistries.id("buildcraftenergy", "oil_deposit_patch_desert")
   );
   public static final ResourceKey<StructureSet> OIL_DEPOSIT_PATCH_OCEAN_SET = ResourceKey.create(
      Registries.STRUCTURE_SET, BCRegistries.id("buildcraftenergy", "oil_deposit_patch_ocean")
   );

   public static final ResourceKey<Structure> WATER_SPRING = ResourceKey.create(
      Registries.STRUCTURE, BCRegistries.id("buildcraftenergy", "water_spring")
   );
   public static final ResourceKey<StructureSet> WATER_SPRING_SET = ResourceKey.create(
      Registries.STRUCTURE_SET, BCRegistries.id("buildcraftenergy", "water_spring")
   );

   public static StructureType<OilDepositStructure> OIL_DEPOSIT_TYPE;
   public static StructurePoolElementType<OilDepositPoolElement> OIL_DEPOSIT_POOL_ELEMENT;
   public static StructureType<WaterSpringStructure> WATER_SPRING_TYPE;
   public static StructurePoolElementType<WaterSpringPoolElement> WATER_SPRING_POOL_ELEMENT;

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

      if (OIL_DEPOSIT_POOL_ELEMENT == null) {
         OIL_DEPOSIT_POOL_ELEMENT = Registry.register(
            BuiltInRegistries.STRUCTURE_POOL_ELEMENT,
            BCRegistries.id("buildcraftenergy", "oil_deposit"),
            () -> OilDepositPoolElement.CODEC
         );
      }

      if (WATER_SPRING_TYPE == null) {
         WATER_SPRING_TYPE = Registry.register(
            BuiltInRegistries.STRUCTURE_TYPE,
            BCRegistries.id("buildcraftenergy", "water_spring"),
            () -> WaterSpringStructure.CODEC
         );
      }

      if (WATER_SPRING_POOL_ELEMENT == null) {
         WATER_SPRING_POOL_ELEMENT = Registry.register(
            BuiltInRegistries.STRUCTURE_POOL_ELEMENT,
            BCRegistries.id("buildcraftenergy", "water_spring"),
            () -> WaterSpringPoolElement.CODEC
         );
      }
   }
}
