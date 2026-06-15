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
import buildcraft.fabric.integration.jei.BCJeiBootstrap;
import buildcraft.fabric.network.BCNetworkingRegistry;
import buildcraft.lib.fabric.BCBlockEntityLifecycleEvents;
import buildcraft.lib.fabric.ExternalEnergyCompat;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.transport.wire.SavedDataWireSystems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
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
         BCJeiBootstrap.initSiliconRecipes();
         BCJeiBootstrap.initEnergyRecipes();
      });
      BCNetworkingRegistry.registerCommon();
      BCNetworkingRegistry.registerServer();
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
   }
}
