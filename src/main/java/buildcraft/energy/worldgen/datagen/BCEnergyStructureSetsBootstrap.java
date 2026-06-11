package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import buildcraft.energy.worldgen.core.OilStructureDefaults;
import buildcraft.energy.worldgen.core.WaterSpringDefaults;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

final class BCEnergyStructureSetsBootstrap {
   private BCEnergyStructureSetsBootstrap() {
   }

   static void bootstrap(BootstrapContext<StructureSet> context) {
      HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);

      registerOilSet(context, structures, BCEnergyStructures.OIL_DEPOSIT_NORMAL_SET, BCEnergyStructures.OIL_DEPOSIT_NORMAL, OilStructureDefaults.PlacementSet.NORMAL);
      registerOilSet(
         context, structures, BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT_SET, BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT, OilStructureDefaults.PlacementSet.PATCH_DESERT
      );
      registerOilSet(
         context, structures, BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN_SET, BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN, OilStructureDefaults.PlacementSet.PATCH_OCEAN
      );
      registerOilSet(
         context,
         structures,
         BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT_DENSE_SET,
         BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT,
         OilStructureDefaults.PlacementSet.PATCH_DESERT_DENSE
      );
      registerOilSet(
         context,
         structures,
         BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN_DENSE_SET,
         BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN,
         OilStructureDefaults.PlacementSet.PATCH_OCEAN_DENSE
      );

      context.register(
         BCEnergyStructures.WATER_SPRING_SET,
         new StructureSet(
            structures.getOrThrow(BCEnergyStructures.WATER_SPRING),
            new RandomSpreadStructurePlacement(
               WaterSpringDefaults.SPACING,
               WaterSpringDefaults.SEPARATION,
               RandomSpreadType.LINEAR,
               WaterSpringDefaults.SALT
            )
         )
      );
   }

   private static void registerOilSet(
      BootstrapContext<StructureSet> context,
      HolderGetter<Structure> structures,
      ResourceKey<StructureSet> setKey,
      ResourceKey<Structure> structureKey,
      OilStructureDefaults.PlacementSet placement
   ) {
      context.register(
         setKey,
         new StructureSet(
            structures.getOrThrow(structureKey),
            new RandomSpreadStructurePlacement(placement.spacing(), placement.separation(), RandomSpreadType.LINEAR, placement.salt())
         )
      );
   }
}
