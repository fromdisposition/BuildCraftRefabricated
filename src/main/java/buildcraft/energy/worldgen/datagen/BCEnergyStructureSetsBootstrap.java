package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import buildcraft.energy.worldgen.structure.OilStructureDefaults;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

final class BCEnergyStructureSetsBootstrap {
   private BCEnergyStructureSetsBootstrap() {
   }

   static void bootstrap(BootstrapContext<StructureSet> context) {
      HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);

      context.register(
         BCEnergyStructures.OIL_DEPOSIT_NORMAL_SET,
         new StructureSet(
            structures.getOrThrow(BCEnergyStructures.OIL_DEPOSIT_NORMAL),
            new RandomSpreadStructurePlacement(
               OilStructureDefaults.NORMAL_SPACING,
               OilStructureDefaults.NORMAL_SEPARATION,
               RandomSpreadType.LINEAR,
               OilStructureDefaults.NORMAL_SALT
            )
         )
      );
      context.register(
         BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT_SET,
         new StructureSet(
            structures.getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT),
            new RandomSpreadStructurePlacement(
               OilStructureDefaults.RICH_SPACING,
               OilStructureDefaults.RICH_SEPARATION,
               RandomSpreadType.LINEAR,
               OilStructureDefaults.PATCH_SALT + 1
            )
         )
      );
      context.register(
         BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN_SET,
         new StructureSet(
            structures.getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN),
            new RandomSpreadStructurePlacement(
               OilStructureDefaults.OCEAN_PATCH_SPACING,
               OilStructureDefaults.OCEAN_PATCH_SEPARATION,
               RandomSpreadType.LINEAR,
               OilStructureDefaults.PATCH_SALT + 2
            )
         )
      );
   }
}
