package buildcraft.energy;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.gen.OilDepositFeature;
import buildcraft.energy.generation.OilGenerator;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.PositionUtil;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class BCEnergyFeatures {
   private static final Identifier ADVANCEMENT_FINE_RICHES = Identifier.parse("buildcraftenergy:fine_riches");
   private static final int FINE_RICHES_SCAN_RADIUS = 3;
   private static final int FINE_RICHES_TICK_STRIDE = 20;
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

         ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
               tryUnlockFineRiches(player);
            }
         });
      }
   }

   private static void tryUnlockFineRiches(ServerPlayer player) {
      if (player.tickCount % FINE_RICHES_TICK_STRIDE != 0 || !(player.level() instanceof ServerLevel level)) {
         return;
      }

      if (!OilGenerator.canGenerateOilIn(level)) {
         return;
      }

      ChunkPos current = player.chunkPosition();
      int cx = PositionUtil.chunkX(current);
      int cz = PositionUtil.chunkZ(current);
      if (!OilGenerator.isOilDesignBiomeAt(level, cx, cz)) {
         return;
      }

      for (int dx = -FINE_RICHES_SCAN_RADIUS; dx <= FINE_RICHES_SCAN_RADIUS; dx++) {
         for (int dz = -FINE_RICHES_SCAN_RADIUS; dz <= FINE_RICHES_SCAN_RADIUS; dz++) {
            if (OilGenerator.wouldGenerateOilForOriginChunk(level, cx + dx, cz + dz)) {
               AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_FINE_RICHES);
               return;
            }
         }
      }
   }
}
