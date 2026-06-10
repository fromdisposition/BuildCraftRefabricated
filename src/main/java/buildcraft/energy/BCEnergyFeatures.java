package buildcraft.energy;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreConfig;
import buildcraft.energy.gen.OilDepositFeature;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.misc.AdvancementUtil;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class BCEnergyFeatures {
   private static final Identifier ADVANCEMENT_FINE_RICHES = Identifier.parse("buildcraftenergy:fine_riches");
   public static final ResourceKey<PlacedFeature> OIL_DEPOSIT_PLACED = ResourceKey.create(
      Registries.PLACED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit")
   );
   public static Feature<NoneFeatureConfiguration> OIL_DEPOSIT;

   private BCEnergyFeatures() {
   }

   public static void register() {
      OIL_DEPOSIT = Registry.register(
         BuiltInRegistries.FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit"), new OilDepositFeature(NoneFeatureConfiguration.CODEC)
      );

      if (BCCoreConfig.worldGen.get() && BCEnergyConfig.enableOilGeneration.get()) {
         BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), Decoration.UNDERGROUND_DECORATION, OIL_DEPOSIT_PLACED);
         if (BCEnergyConfig.enableNetherOilGeneration.get()) {
            BiomeModifications.addFeature(BiomeSelectors.foundInTheNether(), Decoration.UNDERGROUND_DECORATION, OIL_DEPOSIT_PLACED);
         }

         ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
               tryUnlockFineRiches(player);
            }
         });
      }
   }

   private static void tryUnlockFineRiches(ServerPlayer player) {
      if (player.tickCount % 20 != 0 || !(player.level() instanceof ServerLevel level)) {
         return;
      }

      BlockPos center = player.blockPosition();
      for (int dx = -32; dx <= 32; dx += 8) {
         for (int dz = -32; dz <= 32; dz += 8) {
            BlockPos sample = center.offset(dx, 0, dz);
            Identifier biomeId = Identifier.parse(level.getBiome(sample).getRegisteredName());
            if (!BCEnergyConfig.getRichSurfaceDepositBiomes().contains(biomeId)) {
               continue;
            }

            for (int dy = -8; dy <= 8; dy++) {
               if (level.getBlockState(sample.offset(0, dy, 0)).is(BCCoreBlocks.SPRING_OIL)) {
                  AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_FINE_RICHES);
                  return;
               }
            }
         }
      }
   }
}
