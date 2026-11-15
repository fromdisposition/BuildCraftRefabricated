package buildcraft.fabric;

import buildcraft.builders.platform.BCBuildersFabricClient;
import buildcraft.core.platform.BCCoreFabricClient;
import buildcraft.energy.platform.BCEnergyFabricClient;
import buildcraft.fabric.network.BCNetworkingRegistryClient;
import buildcraft.factory.platform.BCFactoryFabricClient;
import buildcraft.lib.fabric.client.BCClientBlockEntityLifecycleEvents;
import buildcraft.lib.fabric.client.BlockOutlineRegistration;
import buildcraft.lib.fabric.client.PictureInPictureRegistration;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.robotics.platform.BCRoboticsFabricClient;
import buildcraft.silicon.platform.BCSiliconFabricClient;
import buildcraft.transport.platform.BCTransportFabricClient;
import buildcraft.transport.wire.ClientWireSystems;
import net.fabricmc.api.ClientModInitializer;
//? if >= 26.1 {
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
//?} else {
/*import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
*///?}
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class BuildCraftFabricClient implements ClientModInitializer {
   @Override
   public void onInitializeClient() {
      BCClientBlockEntityLifecycleEvents.init();
      // Per-dimension client caches would show ghost markers from the previous world; the server re-sends on join.
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         MarkerCache.clearClientCaches();
         ClientWireSystems.INSTANCE.clear();
         buildcraft.robotics.zone.ZonePlannerMapColours.clearSession();
      });
      //? if >= 26.1 {
      ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((client, level) -> {
         MarkerCache.clearClientCaches();
         ClientWireSystems.INSTANCE.clear();
      });
      //?} else {
      /*ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
         MarkerCache.clearClientCaches();
         ClientWireSystems.INSTANCE.clear();
      });
      *///?}
      BCClientRegistriesFabric.register();
      BCNetworkingRegistryClient.registerClient();
      BCCoreFabricClient.init();
      BCEnergyFabricClient.init();
      BCFactoryFabricClient.init();
      BCTransportFabricClient.init();
      BCBuildersFabricClient.init();
      BCSiliconFabricClient.init();
      BCRoboticsFabricClient.init();
      BCLibFabricClient.init();
      // Both classes are per-version overrides; the oldest nodes shadow them with no-op stubs.
      PictureInPictureRegistration.register();
      BlockOutlineRegistration.install();
   }
}
