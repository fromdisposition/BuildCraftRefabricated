package buildcraft.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.template.TemplateApi;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersConfig;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.BCBuildersRecipes;
import buildcraft.builders.BCBuildersSchematics;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.builders.BCBuildersEntities;
import buildcraft.builders.registry.FillerRegistry;
import buildcraft.builders.snapshot.RulesLoader;
import buildcraft.builders.snapshot.TemplateHandlerDefault;
import buildcraft.builders.snapshot.TemplateRegistry;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.RegisterAttachmentsEvent;
import buildcraft.lib.mj.MjBatteryEnergyHandler;
import buildcraft.lib.transfer.EmptyResourceHandler;

public final class BCBuildersFabric {
    private BCBuildersFabric() {}

    public static void register() {
        BCBuildersBlocks.register();
        BCBuildersItems.register();
        BCBuildersBlockEntities.register();
        BCBuildersEntities.register();
        BCBuildersMenuTypes.register();

        BCBuildersRecipes.init();
        FillerManager.registry = FillerRegistry.INSTANCE;
        TemplateApi.templateRegistry = TemplateRegistry.INSTANCE;
        TemplateApi.templateRegistry.addHandler(TemplateHandlerDefault.INSTANCE);
        buildcraft.core.marker.volume.AddonsRegistry.INSTANCE.register(
                net.minecraft.resources.Identifier.parse("buildcraftbuilders:filler_planner"),
                buildcraft.builders.addon.AddonFillerPlanner.class);
        BCBuildersSchematics.preInit();
        BCBuildersStatements.preInit();
        BCBuildersConfig.ensureLoaded();
        RulesLoader.loadAll();
        registerCapabilities();

        ServerTickEvents.END_SERVER_TICK.register(server -> BCBuildersEventDist.INSTANCE.onServerTick());
        registerNetworking();
    }

    private static void registerNetworking() {
        BuildCraftFabricNetworking.registerPlayToServer(
                buildcraft.builders.snapshot.SnapshotRequestPayload.TYPE,
                buildcraft.builders.snapshot.SnapshotRequestPayload.STREAM_CODEC,
                buildcraft.builders.snapshot.SnapshotRequestPayload::handle);
        BuildCraftFabricNetworking.registerPlayToServer(
                buildcraft.builders.snapshot.ArchitectPreviewRequestPayload.TYPE,
                buildcraft.builders.snapshot.ArchitectPreviewRequestPayload.STREAM_CODEC,
                buildcraft.builders.snapshot.ArchitectPreviewRequestPayload::handle);
        BuildCraftFabricNetworking.registerPlayToClient(
                buildcraft.builders.snapshot.SnapshotResponsePayload.TYPE,
                buildcraft.builders.snapshot.SnapshotResponsePayload.STREAM_CODEC,
                buildcraft.builders.snapshot.SnapshotResponsePayload::handle);
        BuildCraftFabricNetworking.registerPlayToClient(
                buildcraft.builders.snapshot.ArchitectPreviewResponsePayload.TYPE,
                buildcraft.builders.snapshot.ArchitectPreviewResponsePayload.STREAM_CODEC,
                buildcraft.builders.snapshot.ArchitectPreviewResponsePayload::handle);
        BuildCraftFabricNetworking.registerPlayToClient(
                buildcraft.builders.snapshot.ArchitectScanPayload.TYPE,
                buildcraft.builders.snapshot.ArchitectScanPayload.STREAM_CODEC,
                buildcraft.builders.snapshot.ArchitectScanPayload::handle);
    }

    private static void registerCapabilities() {
        RegisterAttachmentsEvent event = new RegisterAttachmentsEvent();

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCBuildersBlockEntities.QUARRY,
                (quarry, direction) -> quarry.getMjReceiver());
        event.registerBlockEntity(Attachments.Energy.BLOCK, BCBuildersBlockEntities.QUARRY,
                (quarry, direction) -> MjBatteryEnergyHandler.createIfRfEnabled(quarry.getBattery()));
        event.registerBlockEntity(Attachments.Item.BLOCK, BCBuildersBlockEntities.QUARRY,
                (quarry, direction) -> EmptyResourceHandler.instance());

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCBuildersBlockEntities.FILLER,
                (filler, direction) -> filler.getMjReceiver());
        event.registerBlockEntity(Attachments.Energy.BLOCK, BCBuildersBlockEntities.FILLER,
                (filler, direction) -> MjBatteryEnergyHandler.createIfRfEnabled(filler.getBattery()));
        event.registerBlockEntity(Attachments.Item.BLOCK, BCBuildersBlockEntities.FILLER,
                (filler, direction) -> filler.getItemHandler(direction));

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCBuildersBlockEntities.BUILDER,
                (builder, direction) -> builder.getMjReceiver());
        event.registerBlockEntity(Attachments.Energy.BLOCK, BCBuildersBlockEntities.BUILDER,
                (builder, direction) -> MjBatteryEnergyHandler.createIfRfEnabled(builder.getBattery()));
        event.registerBlockEntity(Attachments.Item.BLOCK, BCBuildersBlockEntities.BUILDER,
                (builder, direction) -> builder.getItemHandler(direction));
        event.registerBlockEntity(Attachments.Fluid.BLOCK, BCBuildersBlockEntities.BUILDER,
                (builder, direction) -> builder.getTankManager());

        event.registerBlockEntity(Attachments.Item.BLOCK, BCBuildersBlockEntities.LIBRARY,
                (library, direction) -> library.getItemHandler(direction));

        event.registerBlockEntity(Attachments.Item.BLOCK, BCBuildersBlockEntities.ARCHITECT,
                (architect, direction) -> architect.getItemHandler(direction));
    }
}

