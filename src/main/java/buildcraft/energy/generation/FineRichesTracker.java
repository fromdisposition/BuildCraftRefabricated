package buildcraft.energy.generation.adapter;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.BCEnergyFeatures;
import buildcraft.energy.generation.core.OilGenerator;
import buildcraft.lib.misc.PositionUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class FineRichesTracker {
   private static final int FINE_RICHES_SCAN_RADIUS = 3;
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
      if (player.tickCount % FINE_RICHES_TICK_STRIDE != 0 || !(player.level() instanceof ServerLevel level)) {
         return;
      }

      if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get()) {
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
               BCEnergyFeatures.OIL_DESIGN_BIOME_NEARBY.trigger(player);
               return;
            }
         }
      }
   }
}
