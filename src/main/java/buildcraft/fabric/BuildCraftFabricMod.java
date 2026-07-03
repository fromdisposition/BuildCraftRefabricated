package buildcraft.fabric;

import buildcraft.builders.platform.BCBuildersFabric;
import buildcraft.core.BCCore;
import buildcraft.core.command.SoundTestCommand;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.core.platform.BCCoreFabric;
import buildcraft.energy.platform.BCEnergyFabric;
import buildcraft.fabric.config.BCFabricConfig;
import buildcraft.factory.platform.BCFactoryFabric;
import buildcraft.robotics.platform.BCRoboticsFabric;
import buildcraft.silicon.platform.BCSiliconFabric;
import buildcraft.transport.platform.BCTransportFabric;
//? if has_jei {
import buildcraft.fabric.integration.jei.BCJeiBootstrap;
//?}
import buildcraft.fabric.network.BCNetworkingRegistry;
import buildcraft.lib.fabric.BCBlockEntityLifecycleEvents;
import buildcraft.lib.fabric.ExternalEnergyCompat;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.transport.wire.SavedDataWireSystems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
// Fabric API renamed the per-level lifecycle events for 26.x: ServerWorldEvents (2.x) -> ServerLevelEvents (4.x).
//? if >= 26.1 {
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
//?} else {
/*import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
*///?}
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.EndDataPackReload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.server.level.ServerLevel;

public class BuildCraftFabricMod implements ModInitializer {
   public void onInitialize() {
      BCFabricConfig.load();
      ExternalEnergyCompat.init();
      BCBlockEntityLifecycleEvents.init();
      BCReloadFabric.initCommon();
      // Register packet types before any module can emit packets.
      BCNetworkingRegistry.registerCommon();
      BCNetworkingRegistry.registerServer();
      BCCore.register();
      BCCoreFabric.register();
      SoundTestCommand.init();
      BCEnergyFabric.register();
      BCFactoryFabric.register();
      BCTransportFabric.register();
      BCBuildersFabric.register();
      BCSiliconFabric.register();
      BCRoboticsFabric.register();
      ServerLifecycleEvents.SERVER_STARTING.register((ServerStarting)server -> {
         //? if has_jei {
         BCJeiBootstrap.initSiliconRecipes();
         BCJeiBootstrap.initEnergyRecipes();
         //?}
      });
      BcTransfers.init();
      ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((EndDataPackReload)(server, resourceManager, success) -> {
         if (success) {
            BCFabricConfig.reload();
         }
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> server.getAllLevels().forEach(level -> WorldSavedDataVolumeBoxes.get(level).tick()));
      ServerPlayConnectionEvents.JOIN.register((Join)(handler, sender, server) -> {
         MarkerCache.onPlayerJoinWorld(handler.player);
         if (handler.player.level() instanceof ServerLevel joinLevel) {
            WorldSavedDataVolumeBoxes.get(joinLevel).sendTo(handler.player);
            SavedDataWireSystems.get(joinLevel).sendTo(handler.player);
         }
      });
      // Marker sub-caches are keyed by dimension id, which collides across worlds in the same game session
      // (singleplayer world switching): evict on unload so the next world builds a fresh cache instead of
      // writing markers into the previous world's dead level + detached saved data. This restores the old
      // BuildCraft WorldEvent.Unload semantics the Fabric port had lost.
      //? if >= 26.1 {
      ServerLevelEvents.UNLOAD.register((server, level) -> {
         MarkerCache.onWorldUnload(level);
         buildcraft.lib.fabric.transfer.BcTransfers.onLevelUnload(level);
      });
      //?} else {
      /*ServerWorldEvents.UNLOAD.register((server, world) -> {
         MarkerCache.onWorldUnload(world);
         buildcraft.lib.fabric.transfer.BcTransfers.onLevelUnload(world);
      });
      *///?}
   }
}
