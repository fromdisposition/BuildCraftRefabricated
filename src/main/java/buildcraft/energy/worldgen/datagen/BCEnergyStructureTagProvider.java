package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;

/** {@code #buildcraftenergy:oil} groups every oil structure so {@code /locate structure #buildcraftenergy:oil} finds the nearest of any kind. */
final class BCEnergyStructureTagProvider extends FabricTagsProvider<Structure> {
   BCEnergyStructureTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
      super(output, Registries.STRUCTURE, registriesFuture);
   }

   @Override
   protected void addTags(HolderLookup.Provider provider) {
      builder(BCEnergyStructures.OIL_TAG)
         .add(BCEnergyStructures.OIL_WELL)
         .add(BCEnergyStructures.OIL_FIELD_DESERT)
         .add(BCEnergyStructures.OIL_FIELD_OCEAN);
      builder(BCEnergyStructures.OIL_FIELD_TAG)
         .add(BCEnergyStructures.OIL_FIELD_DESERT)
         .add(BCEnergyStructures.OIL_FIELD_OCEAN);
   }
}
