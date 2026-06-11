package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public final class BuildCraftWorldgenDataGenerator implements DataGeneratorEntrypoint {
   @Override
   public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
      BCEnergyStructures.registerStructureType();
      FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
      pack.addProvider(BCEnergyStructureProvider::new);
      pack.addProvider(BCEnergyBiomeTagProvider::new);
   }

   @Override
   public void buildRegistry(RegistrySetBuilder registryBuilder) {
      registryBuilder.add(Registries.PROCESSOR_LIST, BCEnergyProcessorListsBootstrap::bootstrap);
      registryBuilder.add(Registries.TEMPLATE_POOL, BCEnergyTemplatePoolsBootstrap::bootstrap);
      registryBuilder.add(Registries.STRUCTURE, BCEnergyStructuresBootstrap::bootstrap);
      registryBuilder.add(Registries.STRUCTURE_SET, BCEnergyStructureSetsBootstrap::bootstrap);
   }
}
