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

         ConfiguredFeature<?, ?> configuredNormal = registry.lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .getOrThrow(BCEnergyWorldgenConfiguredFeatures.OIL_DEPOSIT_NORMAL)
            .value();
         ConfiguredFeature<?, ?> configuredRich = registry.lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .getOrThrow(BCEnergyWorldgenConfiguredFeatures.OIL_DEPOSIT_RICH)
            .value();
         ConfiguredFeature<?, ?> configuredPatch = registry.lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .getOrThrow(BCEnergyWorldgenConfiguredFeatures.OIL_DEPOSIT_PATCH)
            .value();
         PlacedFeature placedNormal = registry.lookupOrThrow(Registries.PLACED_FEATURE)
            .getOrThrow(BCEnergyFeatures.OIL_DEPOSIT_NORMAL_PLACED)
            .value();
         PlacedFeature placedRich = registry.lookupOrThrow(Registries.PLACED_FEATURE)
            .getOrThrow(BCEnergyFeatures.OIL_DEPOSIT_RICH_PLACED)
            .value();
         PlacedFeature placedPatch = registry.lookupOrThrow(Registries.PLACED_FEATURE)
            .getOrThrow(BCEnergyFeatures.OIL_DEPOSIT_PLACED)
            .value();

         CompletableFuture<?> configuredNormalSave = DataProvider.saveStable(
            cache,
            registry,
            ConfiguredFeature.DIRECT_CODEC,
            configuredNormal,
            dataRoot.resolve("worldgen/configured_feature/oil_deposit_normal.json")
         );
         CompletableFuture<?> configuredRichSave = DataProvider.saveStable(
            cache,
            registry,
            ConfiguredFeature.DIRECT_CODEC,
            configuredRich,
            dataRoot.resolve("worldgen/configured_feature/oil_deposit_rich.json")
         );
         CompletableFuture<?> configuredPatchSave = DataProvider.saveStable(
            cache,
            registry,
            ConfiguredFeature.DIRECT_CODEC,
            configuredPatch,
            dataRoot.resolve("worldgen/configured_feature/oil_deposit_patch.json")
         );
         CompletableFuture<?> placedNormalSave = DataProvider.saveStable(
            cache,
            registry,
            PlacedFeature.DIRECT_CODEC,
            placedNormal,
            dataRoot.resolve("worldgen/placed_feature/oil_deposit_normal.json")
         );
         CompletableFuture<?> placedRichSave = DataProvider.saveStable(
            cache,
            registry,
            PlacedFeature.DIRECT_CODEC,
            placedRich,
            dataRoot.resolve("worldgen/placed_feature/oil_deposit_rich.json")
         );
         CompletableFuture<?> placedPatchSave = DataProvider.saveStable(
            cache,
            registry,
            PlacedFeature.DIRECT_CODEC,
            placedPatch,
            dataRoot.resolve("worldgen/placed_feature/oil_deposit_patch.json")
         );
         return CompletableFuture.allOf(
            configuredNormalSave,
            configuredRichSave,
            configuredPatchSave,
            placedNormalSave,
            placedRichSave,
            placedPatchSave
         );
      });
   }

   @Override
   public String getName() {
      return "BuildCraft Energy Worldgen";
   }
}
