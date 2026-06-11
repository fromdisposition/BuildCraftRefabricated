package buildcraft.energy.generation.datagen;

import buildcraft.energy.BCEnergyFeatures;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class BCEnergyWorldgenProvider implements DataProvider {
   private final FabricPackOutput output;
   private final CompletableFuture<HolderLookup.Provider> registriesFuture;

   public BCEnergyWorldgenProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
      this.output = output;
      this.registriesFuture = registriesFuture;
   }

   @Override
   public CompletableFuture<?> run(CachedOutput cache) {
      return this.registriesFuture.thenCompose(registry -> {
         Path dataRoot = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("buildcraftenergy");

         ConfiguredFeature<?, ?> configured = registry.lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .getOrThrow(BCEnergyWorldgenConfiguredFeatures.OIL_DEPOSIT)
            .value();
         PlacedFeature placed = registry.lookupOrThrow(Registries.PLACED_FEATURE)
            .getOrThrow(BCEnergyFeatures.OIL_DEPOSIT_PLACED)
            .value();

         CompletableFuture<?> configuredSave = DataProvider.saveStable(
            cache,
            registry,
            ConfiguredFeature.DIRECT_CODEC,
            configured,
            dataRoot.resolve("worldgen/configured_feature/oil_deposit.json")
         );
         CompletableFuture<?> placedSave = DataProvider.saveStable(
            cache,
            registry,
            PlacedFeature.DIRECT_CODEC,
            placed,
            dataRoot.resolve("worldgen/placed_feature/oil_deposit.json")
         );
         return CompletableFuture.allOf(configuredSave, placedSave);
      });
   }

   @Override
   public String getName() {
      return "BuildCraft Energy Worldgen";
   }
}
