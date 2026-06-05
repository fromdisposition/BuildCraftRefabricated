package buildcraft.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.RegisterAttachmentsEvent;
import buildcraft.lib.misc.CapUtil;
import buildcraft.transport.BCTransportAttachments;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportConfig;
import buildcraft.transport.BCTransportCreativeTabs;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.BCTransportPipes;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.net.MessageMultiPipeItem;
import buildcraft.transport.net.MessagePipeLandingEffect;
import buildcraft.transport.net.MessagePipePayload;
import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.StripesRegistry;
import buildcraft.transport.pipe.flow.PipeItemInjectHandler;
import buildcraft.transport.stripes.StripesHandlerDispenser;
import buildcraft.transport.stripes.StripesHandlerEntityInteract;
import buildcraft.transport.stripes.StripesHandlerHoe;
import buildcraft.transport.stripes.StripesHandlerMinecartDestroy;
import buildcraft.transport.stripes.StripesHandlerPipes;
import buildcraft.transport.stripes.StripesHandlerPlaceBlock;
import buildcraft.transport.stripes.StripesHandlerPlant;
import buildcraft.transport.stripes.StripesHandlerShears;
import buildcraft.transport.stripes.StripesHandlerUse;
import buildcraft.transport.wire.PayloadWireSystems;
import buildcraft.transport.wire.PayloadWireSystemsPowered;
import buildcraft.transport.wire.SavedDataWireSystems;

import buildcraft.api.core.EnumHandlerPriority;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.IMjRedstoneReceiver;

public final class BCTransportFabric {
    private BCTransportFabric() {}

    public static void register() {
        BCTransportConfig.ensureLoaded();
        BCTransportPipes.preInit();
        BCTransportPlugs.preInit();
        BCTransportStatements.preInit();

        BCTransportBlocks.register();
        BCTransportItems.register();
        buildcraft.transport.BCTransportRecipeSerializers.register();
        BCTransportMenuTypes.register();
        BCTransportBlockEntities.register();
        BCTransportCreativeTabs.register();
        BCTransportAttachments.register();

        BCTransportConfig.registerPowerTransferData();
        BCTransportConfig.registerRfTransferData();
        BCTransportConfig.registerFluidTransferData();
        initStripesRegistry();
        registerCapabilities();
        registerNetworking();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PipeItemMessageQueue.serverTick();
            for (net.minecraft.server.level.ServerLevel serverLevel : server.getAllLevels()) {
                SavedDataWireSystems.get(serverLevel).tick();
            }
        });
    }

    private static void registerNetworking() {
        BuildCraftFabricNetworking.registerPlayToClient(
                MessageMultiPipeItem.TYPE, MessageMultiPipeItem.STREAM_CODEC, MessageMultiPipeItem::handle);
        BuildCraftFabricNetworking.registerPlayToClient(
                MessagePipePayload.TYPE, MessagePipePayload.STREAM_CODEC, MessagePipePayload::handle);
        BuildCraftFabricNetworking.registerPlayToClient(
                PayloadWireSystems.TYPE, PayloadWireSystems.STREAM_CODEC, PayloadWireSystems::handle);
        BuildCraftFabricNetworking.registerPlayToClient(
                PayloadWireSystemsPowered.TYPE,
                PayloadWireSystemsPowered.STREAM_CODEC,
                PayloadWireSystemsPowered::handle);
        BuildCraftFabricNetworking.registerPlayToClient(
                MessagePipeLandingEffect.TYPE,
                MessagePipeLandingEffect.STREAM_CODEC,
                MessagePipeLandingEffect::handle);
    }

    private static void registerCapabilities() {
        RegisterAttachmentsEvent event = new RegisterAttachmentsEvent();

        event.registerBlockEntity(
                MjAPI.CAP_RECEIVER,
                BCTransportBlockEntities.PIPE_HOLDER,
                (tile, side) -> {
                    Pipe pipe = tile.getPipe();
                    if (pipe == null || side == null) {
                        return null;
                    }
                    buildcraft.api.transport.pluggable.PipePluggable plug = tile.getPluggable(side);
                    if (plug != null) {
                        IMjReceiver r = plug.getCapability(MjAPI.CAP_RECEIVER);
                        if (r != null) {
                            return r;
                        }
                        if (plug.isBlocking()) {
                            return null;
                        }
                    }
                    IMjReceiver r = pipe.getBehaviour().getCapability(MjAPI.CAP_RECEIVER, side);
                    if (r != null) {
                        return r;
                    }
                    return pipe.getFlow().getCapability(MjAPI.CAP_RECEIVER, side);
                });

        event.registerBlockEntity(
                MjAPI.CAP_REDSTONE_RECEIVER,
                BCTransportBlockEntities.PIPE_HOLDER,
                (tile, side) -> {
                    Pipe pipe = tile.getPipe();
                    if (pipe == null || side == null) {
                        return null;
                    }
                    buildcraft.api.transport.pluggable.PipePluggable plug = tile.getPluggable(side);
                    if (plug != null) {
                        IMjRedstoneReceiver r = plug.getCapability(MjAPI.CAP_REDSTONE_RECEIVER);
                        if (r != null) {
                            return r;
                        }
                        if (plug.isBlocking()) {
                            return null;
                        }
                    }
                    IMjRedstoneReceiver r = pipe.getBehaviour().getCapability(MjAPI.CAP_REDSTONE_RECEIVER, side);
                    if (r != null) {
                        return r;
                    }
                    return pipe.getFlow().getCapability(MjAPI.CAP_REDSTONE_RECEIVER, side);
                });

        event.registerBlockEntity(
                MjAPI.CAP_CONNECTOR,
                BCTransportBlockEntities.PIPE_HOLDER,
                (tile, side) -> {
                    Pipe pipe = tile.getPipe();
                    if (pipe == null || side == null) {
                        return null;
                    }
                    buildcraft.api.transport.pluggable.PipePluggable plug = tile.getPluggable(side);
                    if (plug != null) {
                        IMjConnector c = plug.getCapability(MjAPI.CAP_CONNECTOR);
                        if (c != null) {
                            return c;
                        }
                        if (plug.isBlocking()) {
                            return null;
                        }
                    }
                    IMjConnector c = pipe.getBehaviour().getCapability(MjAPI.CAP_CONNECTOR, side);
                    if (c != null) {
                        return c;
                    }
                    return pipe.getFlow().getCapability(MjAPI.CAP_CONNECTOR, side);
                });

        event.registerBlockEntity(
                Attachments.Fluid.BLOCK,
                BCTransportBlockEntities.PIPE_HOLDER,
                (tile, side) -> {
                    Pipe pipe = tile.getPipe();
                    if (pipe == null || side == null) {
                        return null;
                    }
                    buildcraft.api.transport.pluggable.PipePluggable plug = tile.getPluggable(side);
                    if (plug != null && plug.isBlocking()) {
                        return null;
                    }
                    return pipe.getFlow().getCapability(CapUtil.CAP_FLUIDS, side);
                });

        event.registerBlockEntity(
                Attachments.Energy.BLOCK,
                BCTransportBlockEntities.PIPE_HOLDER,
                (tile, side) -> {
                    if (side != null) {
                        buildcraft.api.transport.pluggable.PipePluggable plug = tile.getPluggable(side);
                        if (plug != null && plug.isBlocking()) {
                            return null;
                        }
                    }
                    buildcraft.api.transport.pipe.IPipe pipe = tile.getPipe();
                    if (pipe != null && pipe.getFlow() != null) {
                        return pipe.getFlow().getCapability(Attachments.Energy.BLOCK, side);
                    }
                    return null;
                });

        event.registerBlockEntity(
                Attachments.Item.BLOCK,
                BCTransportBlockEntities.FILTERED_BUFFER,
                (tile, side) -> tile.getItemHandler(side));

        event.registerBlockEntity(
                Attachments.Item.BLOCK,
                BCTransportBlockEntities.PIPE_HOLDER,
                (tile, side) -> {
                    Pipe pipe = tile.getPipe();
                    if (pipe == null || side == null) {
                        return null;
                    }
                    buildcraft.api.transport.pluggable.PipePluggable plug = tile.getPluggable(side);
                    if (plug != null && plug.isBlocking()) {
                        return null;
                    }
                    if (pipe.getFlow() instanceof IFlowItems itemFlow) {
                        return new PipeItemInjectHandler(itemFlow, side);
                    }
                    return null;
                });
    }

    private static void initStripesRegistry() {
        PipeApi.stripeRegistry = StripesRegistry.INSTANCE;
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlant.INSTANCE);
        PipeApi.stripeRegistry.addHandler(StripesHandlerShears.INSTANCE);
        PipeApi.stripeRegistry.addHandler(new StripesHandlerPipes());
        PipeApi.stripeRegistry.addHandler(StripesHandlerEntityInteract.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerHoe.INSTANCE);
        PipeApi.stripeRegistry.addHandler(StripesHandlerDispenser.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlaceBlock.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerUse.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerMinecartDestroy.INSTANCE);
    }
}

