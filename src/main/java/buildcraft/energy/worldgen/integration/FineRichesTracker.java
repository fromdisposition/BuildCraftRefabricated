package buildcraft.energy.worldgen.integration;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.worldgen.BCEnergyWorldgen;
import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

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

      // Vanilla inside-a-structure check against the oil-field tag (how adventure triggers do it).
      if (!((net.minecraft.server.level.ServerLevel) player.level()).structureManager().getStructureWithPieceAt(player.blockPosition(), BCEnergyStructures.OIL_FIELD_TAG).isValid()) {
         return;
      }

      BCEnergyWorldgen.OIL_DESIGN_BIOME_NEARBY.trigger(player);
   }
}
