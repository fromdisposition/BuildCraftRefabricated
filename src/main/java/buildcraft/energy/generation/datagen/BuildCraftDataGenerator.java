package buildcraft.energy.generation.datagen;

import buildcraft.energy.BCEnergyFeatures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public final class BuildCraftDataGenerator implements DataGeneratorEntrypoint {
   @Override
   public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
      BCEnergyFeatures.registerFeatureType();
      FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
      pack.addProvider(BCEnergyWorldgenProvider::new);
      pack.addProvider(BCEnergyBiomeTagProvider::new);
   }

   @Override
   public void buildRegistry(RegistrySetBuilder registryBuilder) {
      registryBuilder.add(Registries.CONFIGURED_FEATURE, BCEnergyWorldgenConfiguredFeatures::bootstrap);
      registryBuilder.add(Registries.PLACED_FEATURE, BCEnergyWorldgenPlacedFeatures::bootstrap);
   }
}
