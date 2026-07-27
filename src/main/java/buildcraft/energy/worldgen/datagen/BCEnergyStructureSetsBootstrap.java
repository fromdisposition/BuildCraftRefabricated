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

      registerOilSet(context, structures, BCEnergyStructures.OIL_WELL_SET, BCEnergyStructures.OIL_WELL, OilStructureDefaults.PlacementSet.NORMAL);
      registerOilSet(
         context, structures, BCEnergyStructures.OIL_FIELD_DESERT_SET, BCEnergyStructures.OIL_FIELD_DESERT, OilStructureDefaults.PlacementSet.FIELD_DESERT
      );
      registerOilSet(
         context, structures, BCEnergyStructures.OIL_FIELD_OCEAN_SET, BCEnergyStructures.OIL_FIELD_OCEAN, OilStructureDefaults.PlacementSet.FIELD_OCEAN
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
      // locate_offset (8,0,8): /locate reports the structure chunk's MIN corner; the well template centres
      // on the chunk middle, so shift the report there — vanilla's own field for exactly this.
      context.register(
         setKey,
         new StructureSet(
            structures.getOrThrow(structureKey),
            new RandomSpreadStructurePlacement(
               new net.minecraft.core.Vec3i(8, 0, 8),
               net.minecraft.world.level.levelgen.structure.placement.StructurePlacement.FrequencyReductionMethod.DEFAULT,
               1.0F,
               placement.salt(),
               java.util.Optional.empty(),
               placement.spacing(),
               placement.separation(),
               RandomSpreadType.LINEAR
            )
         )
      );
   }
}
