package buildcraft.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import buildcraft.core.BCCore;
import buildcraft.core.command.SoundTestCommand;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.marker.MarkerCache;

public class BuildCraftFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        buildcraft.fabric.config.BCFabricConfig.load();
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
        BuildCraftFabricNetworking.register();

        buildcraft.lib.fabric.transfer.FabricTransferBridge.init();

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
                (server, resourceManager, success) -> {
                    if (success) {
                        buildcraft.fabric.config.BCFabricConfig.reload();
                    }
                });

        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getAllLevels().forEach(level -> WorldSavedDataVolumeBoxes.get(level).tick()));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            MarkerCache.onPlayerJoinWorld(handler.player);
            WorldSavedDataVolumeBoxes.get((net.minecraft.server.level.ServerLevel) handler.player.level())
                    .sendTo(handler.player);
        });
    }
}
