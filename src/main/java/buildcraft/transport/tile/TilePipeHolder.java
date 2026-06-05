/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.tile;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

import io.netty.buffer.Unpooled;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.net.BCPacketLimits;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.net.MessagePipePayload;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDaizuli;
import buildcraft.transport.wire.WireManager;
import buildcraft.transport.pipe.PipeEventBus;

@SuppressWarnings("this-escape")
public class TilePipeHolder extends BlockEntity
        implements IPipeHolder, IDebuggable, buildcraft.lib.tile.IBlockEntityLoadHook {
    private static final net.minecraft.resources.Identifier ADVANCEMENT_PIPE_DREAM
        = net.minecraft.resources.Identifier.parse("buildcrafttransport:pipe_dream");
    private static final net.minecraft.resources.Identifier ADVANCEMENT_PIPE_DIVERSIFICATION
        = net.minecraft.resources.Identifier.parse("buildcrafttransport:pipe_diversification");
    private static final net.minecraft.resources.Identifier ADVANCEMENT_PIPE_FANATIC
        = net.minecraft.resources.Identifier.parse("buildcrafttransport:pipe_fanatic");
    private static final net.minecraft.resources.Identifier ADVANCEMENT_CATEGORIZING_WITH_COLORS
        = net.minecraft.resources.Identifier.parse("buildcrafttransport:categorizing_with_colors");

    public static final buildcraft.lib.client.model.data.ModelProperty<TilePipeHolder> PIPE_MODEL_DATA =
        buildcraft.lib.client.model.data.ModelProperty.create();

    public final PipeEventBus eventBus = new PipeEventBus();
    private Pipe pipe;
    private GameProfile owner;
    public final WireManager wireManager = new WireManager(this);
    private final PipePluggable[] pluggables = new PipePluggable[6];
    private boolean scheduleRenderUpdate = true;
    private final Set<PipeMessageReceiver> networkUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Set<PipeMessageReceiver> networkGuiUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Set<net.minecraft.server.level.ServerPlayer> guiViewers = new HashSet<>();

    private CompoundTag pipeSaveCache;

    public TilePipeHolder(BlockPos pos, BlockState state) {
        super(BCTransportBlockEntities.PIPE_HOLDER, pos, state);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        if (owner != null && owner.id() != null) {
            output.putString("ownerUUID", owner.id().toString());
            if (owner.name() != null) {
                output.putString("ownerName", owner.name());
            }
        }
        if (pipe != null) {
            output.store("pipe", CompoundTag.CODEC, pipe.writeToNbt());
        }
        CompoundTag wireTag = wireManager.writeToNbt();
        if (!wireTag.isEmpty()) {
            output.store("wires", CompoundTag.CODEC, wireTag);
        }

        CompoundTag plugTag = new CompoundTag();
        for (Direction face : Direction.values()) {
            PipePluggable plug = pluggables[face.ordinal()];
            if (plug != null) {
                CompoundTag entry = new CompoundTag();
                entry.putString("id", plug.definition.identifier.toString());
                entry.put("data", plug.writeToNbt());
                plugTag.put(face.getName(), entry);
            }
        }
        if (!plugTag.isEmpty()) {
            output.store("plugs", CompoundTag.CODEC, plugTag);
        }
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        String ownerUuid = input.getStringOr("ownerUUID", "");
        if (!ownerUuid.isEmpty()) {
            try {
                owner = new GameProfile(UUID.fromString(ownerUuid), input.getStringOr("ownerName", "Unknown"));
            } catch (IllegalArgumentException e) {
                owner = null;
            }
        }
        input.read("pipe", CompoundTag.CODEC).ifPresent(pipeTag -> {
            try {
                if (pipe != null) {

                    pipe.readFromNbt(pipeTag);
                } else {
                    pipe = new Pipe(this, pipeTag);
                    eventBus.registerHandler(pipe.behaviour);
                    eventBus.registerHandler(pipe.flow);
                }
            } catch (InvalidInputDataException e) {
                pipe = null;
            }
        });

        input.read("plugs", CompoundTag.CODEC).ifPresentOrElse(plugTag -> {
            for (Direction face : Direction.values()) {
                if (plugTag.contains(face.getName())) {
                    CompoundTag entry = plugTag.getCompound(face.getName()).orElse(new CompoundTag());
                    String id = entry.getString("id").orElse("");
                    if (!id.isEmpty()) {
                        net.minecraft.resources.Identifier plugId = net.minecraft.resources.Identifier.parse(id);
                        PluggableDefinition def = PipeApi.pluggableRegistry != null
                                ? PipeApi.pluggableRegistry.getDefinition(plugId) : null;
                        if (def != null) {
                            CompoundTag data = entry.getCompound("data").orElse(new CompoundTag());

                            PipePluggable existing = pluggables[face.ordinal()];
                            if (existing != null && existing.definition.identifier.equals(plugId)
                                    && existing.readFromNbt(data)) {

                            } else {
                                try {
                                    pluggables[face.ordinal()] = def.readFromNbt(this, face, data);
                                } catch (RuntimeException e) {
                                    buildcraft.api.core.BCLog.logger.warn(
                                            "[transport.pipe] Failed to load pluggable {} at {} {}",
                                            plugId, getBlockPos(), face, e);
                                    pluggables[face.ordinal()] = null;
                                }
                            }
                        } else {
                            pluggables[face.ordinal()] = null;
                        }
                    } else {
                        pluggables[face.ordinal()] = null;
                    }
                } else {
                    pluggables[face.ordinal()] = null;
                }
            }
        }, () -> {

            for (int i = 0; i < pluggables.length; i++) {
                pluggables[i] = null;
            }
        });

        for (PipePluggable plug : pluggables) {
            eventBus.unregisterHandler(plug);
            eventBus.registerHandler(plug);
        }

        input.read("wires", CompoundTag.CODEC).ifPresent(wireTag -> {
            wireManager.readFromNbt(wireTag);
        });

        if (level != null && level.isClientSide()) {
            refreshClientModel();
            scheduleRenderUpdate = true;
        }
    }

    public void onLoad() {
        if (pipe != null) {
            pipe.onLoad();
        }

        refreshClientModel();
        scheduleRenderUpdate = true;
    }

    private void refreshClientModel() {
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = this.saveCustomOnly(registries);

        CompoundTag plugsClient = new CompoundTag();
        for (Direction face : Direction.values()) {
            PipePluggable plug = pluggables[face.ordinal()];
            if (plug != null) {
                CompoundTag data = plug.writeClientUpdateData();
                if (!data.isEmpty()) {
                    plugsClient.put(face.getName(), data);
                }
            }
        }
        if (!plugsClient.isEmpty()) {
            tag.put("plugsClient", plugsClient);
        }
        return tag;
    }

    public void handleUpdateTag(net.minecraft.world.level.storage.ValueInput input) {
        applyClientUpdateData(input);

        refreshClientModel();
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.world.level.storage.ValueInput input) {
        applyClientUpdateData(input);
        refreshClientModel();
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void applyClientUpdateData(net.minecraft.world.level.storage.ValueInput input) {
        input.read("plugsClient", CompoundTag.CODEC).ifPresent(plugsClient -> {
            for (Direction face : Direction.values()) {
                PipePluggable plug = pluggables[face.ordinal()];
                if (plug != null && plugsClient.contains(face.getName())) {
                    plug.readClientUpdateData(
                        plugsClient.getCompound(face.getName()).orElse(new CompoundTag()));
                }
            }
        });
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IItemPipe) {
            PipeDefinition definition = ((IItemPipe) item).getDefinition();
            this.pipe = new Pipe(this, definition);
            eventBus.registerHandler(pipe.behaviour);
            eventBus.registerHandler(pipe.flow);

            DyeColor col = stack.get(BCTransportItems.PIPE_COLOUR.get());
            if (col != null) {
                pipe.setColour(col);
            }
        }
        if (placer instanceof Player player && level != null && !level.isClientSide()) {
            owner = player.getGameProfile();
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PIPE_DREAM);
            if (pipe != null) {
                PipeDefinition def = pipe.getDefinition();

                String flowCriterion = getFlowTypeCriterion(def);
                if (flowCriterion != null) {
                    AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PIPE_DIVERSIFICATION, flowCriterion);
                }

                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PIPE_FANATIC, def.identifier);

                if (pipe.behaviour instanceof PipeBehaviourDaizuli) {
                    AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_CATEGORIZING_WITH_COLORS);
                }
            }
        }
        scheduleRenderUpdate();
        setChanged();
    }

    private static String getFlowTypeCriterion(PipeDefinition def) {
        if (def.flowType == PipeApi.flowItems) return "item_pipe";
        if (def.flowType == PipeApi.flowFluids) return "fluid_pipe";
        if (def.flowType == PipeApi.flowPower) return "power_pipe";
        if (def.flowType == PipeApi.flowStructure) return "structure_pipe";
        return null;
    }

    public void tick() {
        ProfilerFiller _profiler = Profiler.get();
        _profiler.push("buildcraft:pipe_tick");
        try {

        java.util.Arrays.fill(redstoneOutputsThisTick, 0);

        wireManager.tick();
        if (pipe != null) {
            pipe.onTick();
        }

        for (PipePluggable plug : pluggables) {
            if (plug != null) {
                plug.onTick();
            }
        }
        if (pipe != null) {
            pipe.postPluggableTick();
        }

        boolean redstoneChanged = false;
        for (int i = 0; i < 6; i++) {
            if (redstoneOutputs[i] != redstoneOutputsThisTick[i]) {
                redstoneOutputs[i] = redstoneOutputsThisTick[i];
                redstoneChanged = true;
            }
        }
        if (redstoneChanged && level != null && !level.isClientSide()) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }

        if (scheduleRenderUpdate && level != null && !level.isClientSide()) {
            scheduleRenderUpdate = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }

        if (level != null && !level.isClientSide()) {
            flushScheduledNetworkUpdates();
            setChanged();
        }
        } finally {
            _profiler.pop();
        }
    }

    private void flushScheduledNetworkUpdates() {
        if (!networkUpdates.isEmpty()) {
            Set<PipeMessageReceiver> parts = EnumSet.copyOf(networkUpdates);
            sendScheduledPayloads(parts, this::sendTrackingPayload);
        }
        networkGuiUpdates.removeAll(networkUpdates);
        networkUpdates.clear();

        if (!networkGuiUpdates.isEmpty()) {
            Set<PipeMessageReceiver> parts = EnumSet.copyOf(networkGuiUpdates);
            sendScheduledPayloads(parts, payload -> {
                for (net.minecraft.server.level.ServerPlayer viewer : guiViewers) {
                    buildcraft.lib.fabric.PacketDistributor.sendToPlayer(viewer, payload);
                }
            });
        }
        networkGuiUpdates.clear();
    }

    private void sendScheduledPayloads(Set<PipeMessageReceiver> parts,
            java.util.function.Consumer<MessagePipePayload> sender) {
        if (parts.isEmpty()) {
            return;
        }
        if (parts.size() == 1) {
            PipeMessageReceiver part = parts.iterator().next();
            byte[] data = buildSinglePayload(part);
            if (data != null) {
                sender.accept(new MessagePipePayload(worldPosition, part.ordinal(), data));
            }
            return;
        }
        byte[] data = buildMultiPayload(parts);
        if (data != null) {
            sender.accept(new MessagePipePayload(worldPosition, MessagePipePayload.MULTI_RECEIVER_ORDINAL, data));
        }
    }

    @Nullable
    private byte[] buildSinglePayload(PipeMessageReceiver part) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        try {
            writeScheduledPayload(part, buffer);
            return extractPayloadBytes(buffer);
        } finally {
            buffer.release();
        }
    }

    @Nullable
    private byte[] buildMultiPayload(Set<PipeMessageReceiver> parts) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        try {
            int mask = 0;
            for (PipeMessageReceiver part : parts) {
                mask |= 1 << part.ordinal();
            }
            buffer.writeShort(mask);
            for (PipeMessageReceiver part : PipeMessageReceiver.VALUES) {
                if ((mask & (1 << part.ordinal())) != 0) {
                    writeScheduledPayload(part, buffer);
                }
            }
            return extractPayloadBytes(buffer);
        } finally {
            buffer.release();
        }
    }

    @Nullable
    private byte[] extractPayloadBytes(PacketBufferBC buffer) {
        int size = buffer.readableBytes();
        if (size > BCPacketLimits.MAX_PAYLOAD_BYTES) {
            BCLog.logger.warn("[transport] Pipe payload at {} exceeds limit ({} bytes)", worldPosition, size);
            return null;
        }
        byte[] data = new byte[size];
        buffer.readBytes(data);
        return data;
    }

    private void sendTrackingPayload(MessagePipePayload payload) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            buildcraft.lib.fabric.PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel,
                    new net.minecraft.world.level.ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4),
                    payload);
        }
    }

    private void writeScheduledPayload(PipeMessageReceiver part, FriendlyByteBuf buffer) {
        switch (part) {
            case BEHAVIOUR -> {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writePayload(buffer);
                }
            }
            case FLOW -> {
                if (pipe == null || pipe.getFlow() == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writeShort(PipeFlow.NET_ID_UPDATE);
                    pipe.getFlow().writePayload(PipeFlow.NET_ID_UPDATE, buffer, null);
                }
            }
            case WIRES -> wireManager.writePayload(PacketBufferBC.asPacketBufferBc(buffer));
            default -> {
                if (part.face != null) {
                    PipePluggable plug = getPluggable(part.face);
                    if (plug != null) {
                        plug.writePayload(buffer, part.face);
                    }
                }
            }
        }
    }

    private void sendGuiPayload(PipeMessageReceiver to, IWriter writer) {
        if (level == null || level.isClientSide() || guiViewers.isEmpty()) {
            return;
        }

        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        try {
            writer.write(buffer);
            byte[] data = extractPayloadBytes(buffer);
            if (data == null) {
                return;
            }
            MessagePipePayload payload = new MessagePipePayload(worldPosition, to.ordinal(), data);
            for (net.minecraft.server.level.ServerPlayer viewer : guiViewers) {
                buildcraft.lib.fabric.PacketDistributor.sendToPlayer(viewer, payload);
            }
        } catch (Exception e) {
            BCLog.logger.warn("[transport] Failed to send pipe gui message at " + worldPosition, e);
        } finally {
            buffer.release();
        }
    }

    public void dropPipeItems(Level lvl, BlockPos pos) {
        if (pipe != null) {

            PipeDefinition def = pipe.getDefinition();
            Item pipeItem = (Item) PipeApi.pipeRegistry.getItemForPipe(def);
            if (pipeItem != null) {
                ItemStack pipeStack = new ItemStack(pipeItem);
                DyeColor col = pipe.getColour();
                if (col != null) {
                    pipeStack.set(BCTransportItems.PIPE_COLOUR.get(), col);
                }
                Block.popResource(lvl, pos, pipeStack);
            }

            NonNullList<ItemStack> drops = NonNullList.create();
            pipe.addDrops(drops, 0);
            for (ItemStack drop : drops) {
                Block.popResource(lvl, pos, drop);
            }
        }

        for (int i = 0; i < 6; i++) {
            PipePluggable plug = pluggables[i];
            if (plug != null) {
                NonNullList<ItemStack> plugDrops = NonNullList.create();
                plug.addDrops(plugDrops, 0);
                for (ItemStack drop : plugDrops) {
                    Block.popResource(lvl, pos, drop);
                }
                plug.onRemove();
                pluggables[i] = null;
            }
        }

        for (DyeColor color : wireManager.parts.values()) {
            if (color != null) {
                Item wireItem = buildcraft.transport.BCTransportItems.WIRE_ITEMS.get(color).get();
                if (wireItem != null) {
                    Block.popResource(lvl, pos, new ItemStack(wireItem));
                }
            }
        }
    }

    @Override
    public Level getPipeWorld() {
        return getLevel();
    }

    @Override
    public BlockPos getPipePos() {
        return getBlockPos();
    }

    @Override
    public BlockEntity getPipeTile() {
        return this;
    }

    @Override
    public Pipe getPipe() {
        return pipe;
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        if (level == null) return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Nullable
    @Override
    public PipePluggable getPluggable(Direction side) {
        if (side == null) return null;
        return pluggables[side.ordinal()];
    }

    @Nullable
    public PipePluggable replacePluggable(Direction side, @Nullable PipePluggable with) {
        PipePluggable old = pluggables[side.ordinal()];
        pluggables[side.ordinal()] = with;

        eventBus.unregisterHandler(old);
        eventBus.registerHandler(with);

        if (pipe != null) {
            pipe.markForUpdate();
        }

        IPipe neighbourPipe = getNeighbourPipe(side);
        if (neighbourPipe != null) {
            neighbourPipe.markForUpdate();
        }
        if (level != null && !level.isClientSide()) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            for (Direction dir : Direction.values()) {
                BlockPos npos = worldPosition.relative(dir);
                BlockState nstate = level.getBlockState(npos);
                if (!nstate.isAir()) {
                    BlockState res = nstate.updateShape(level, level, npos, dir.getOpposite(), worldPosition, getBlockState(), level.getRandom());
                    if (res != nstate) {
                        Block.updateOrDestroy(nstate, res, level, npos, Block.UPDATE_ALL);
                    }
                }
            }

            boolean oldWasEmitter = old instanceof buildcraft.api.transport.IWireEmitter;
            boolean newIsEmitter = with instanceof buildcraft.api.transport.IWireEmitter;
            if (oldWasEmitter || newIsEmitter) {
                buildcraft.transport.wire.SavedDataWireSystems wireSystems =
                    buildcraft.transport.wire.SavedDataWireSystems.get(level);
                wireSystems.rebuildWireSystemsAround(this);

                for (Direction dir : Direction.values()) {
                    IPipe neighbour = getNeighbourPipe(dir);
                    if (neighbour != null) {
                        wireSystems.rebuildWireSystemsAround(neighbour.getHolder());
                    }
                }
            }
        }
        scheduleRenderUpdate();
        setChanged();
        return old;
    }

    @Nullable
    @Override
    public BlockEntity getNeighbourTile(Direction side) {
        if (level == null) return null;
        return level.getBlockEntity(worldPosition.relative(side));
    }

    @Nullable
    @Override
    public IPipe getNeighbourPipe(Direction side) {
        BlockEntity neighbour = getNeighbourTile(side);
        if (neighbour instanceof TilePipeHolder other) {
            return other.getPipe();
        }
        return null;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapabilityFromPipe(Direction side, @Nonnull Object capability) {
        if (level == null || side == null) return null;

        PipePluggable plug = getPluggable(side);
        if (plug != null) {
            T t = plug.getInternalCapability(capability);
            if (t != null) {
                return t;
            }
            if (plug.isBlocking()) {
                return null;
            }
        }

        if (pipe == null) {
            return null;
        }
        BlockPos neighborPos = worldPosition.relative(side);
        if (capability instanceof buildcraft.lib.attachments.BlockAttachment<?, ?> blockCap) {

            try {
                return (T) buildcraft.lib.fabric.AttachmentLevelAccess.of(level).getCapability(
                    (buildcraft.lib.attachments.BlockAttachment) blockCap,
                    neighborPos, side.getOpposite());
            } catch (ClassCastException e) {
                return null;
            }
        }

        BlockEntity neighbor = level.getBlockEntity(neighborPos);
        if (neighbor == null) return null;

        IPipe neighborPipe = getNeighbourPipe(side);
        if (neighborPipe != null && neighborPipe.getFlow() != null) {
            T result = neighborPipe.getFlow().getCapability(capability, side.getOpposite());
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public WireManager getWireManager() {
        return wireManager;
    }

    @Override
    public GameProfile getOwner() {
        return owner;
    }

    @Override
    public boolean fireEvent(PipeEvent event) {
        return eventBus.fireEvent(event);
    }

    @Override
    public void scheduleRenderUpdate() {
        if (level != null && level.isClientSide()) {
            refreshClientModel();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        } else {
            scheduleRenderUpdate = true;
        }
    }

    @Override
    public void scheduleNetworkUpdate(PipeMessageReceiver... parts) {
        Collections.addAll(networkUpdates, parts);
        scheduleRenderUpdate = true;
    }

    @Override
    public void scheduleNetworkGuiUpdate(PipeMessageReceiver... parts) {
        Collections.addAll(networkGuiUpdates, parts);
    }

    @Override
    public void sendMessage(PipeMessageReceiver to, IWriter writer) {
        if (level == null || level.isClientSide()) return;

        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        try {
            writer.write(buffer);
            byte[] data = extractPayloadBytes(buffer);
            if (data == null) {
                return;
            }
            sendTrackingPayload(new MessagePipePayload(worldPosition, to.ordinal(), data));
        } catch (Exception e) {
            BCLog.logger.warn("[transport] Failed to send pipe message at " + worldPosition, e);
        } finally {
            buffer.release();
        }
    }

    @Override
    public void sendGuiMessage(PipeMessageReceiver to, IWriter writer) {
        sendGuiPayload(to, writer);
    }

    @Override
    public void onPlayerOpen(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            guiViewers.add(serverPlayer);
        }
    }

    @Override
    public void onPlayerClose(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            guiViewers.remove(serverPlayer);
        }
    }

    private final int[] redstoneOutputs = new int[Direction.values().length];
    private final int[] redstoneOutputsThisTick = new int[Direction.values().length];

    @Override
    public int getRedstoneInput(Direction side) {
        if (level == null) return 0;
        if (side == null) {
            return level.getBestNeighborSignal(worldPosition);
        }
        return level.getSignal(worldPosition.relative(side), side);
    }

    public int getRedstoneOutput(Direction side) {
        if (side == null) return 0;
        return redstoneOutputs[side.ordinal()];
    }

    @Override
    public boolean setRedstoneOutput(Direction side, int value) {
        if (side == null) {
            boolean changed = false;
            for (int i = 0; i < 6; i++) {
                if (redstoneOutputsThisTick[i] < value) {
                    redstoneOutputsThisTick[i] = value;
                    changed = true;
                }
            }
            return changed;
        }
        int idx = side.ordinal();
        if (redstoneOutputsThisTick[idx] < value) {
            redstoneOutputsThisTick[idx] = value;
            return true;
        }
        return false;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        if (pipe == null) {
            left.add("Pipe = null");
        } else {
            left.add("Pipe:");
            pipe.getDebugInfo(left, right, side);
        }
    }

    public buildcraft.lib.client.model.data.ModelData getModelData() {
        return buildcraft.lib.client.model.data.ModelData.builder()
            .with(PIPE_MODEL_DATA, this)
            .build();
    }
}
