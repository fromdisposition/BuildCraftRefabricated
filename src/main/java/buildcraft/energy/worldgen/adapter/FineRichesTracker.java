package buildcraft.energy.worldgen.adapter;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.BCEnergyFeatures;
import buildcraft.energy.worldgen.core.BCEnergyBiomeTags;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;

public final class FineRichesTracker {
   private static final int FINE_RICHES_TICK_STRIDE = 20;

   private FineRichesTracker() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tryUnlockFineRiches(player);
         }
      });
   }

   private static void tryUnlockFineRiches(ServerPlayer player) {
      if (player.tickCount % FINE_RICHES_TICK_STRIDE != 0) {
         return;
      }

      if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get()) {
         return;
      }

      Holder<Biome> biome = player.level().getBiome(player.blockPosition());
      if (!biome.is(BCEnergyBiomeTags.OIL_DESIGN_BIOME)) {
         return;
      }

      BCEnergyFeatures.OIL_DESIGN_BIOME_NEARBY.trigger(player);
   }
}
