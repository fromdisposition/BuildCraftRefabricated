/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.net;

import java.io.IOException;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.net.BoundedByteArrays;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;

public record MessagePipePayload(
        BlockPos pos,
        int receiverOrdinal,
        byte[] payload
) implements CustomPacketPayload {

    /** Batched update mask + concatenated receiver payloads (8.0 NET_UPDATE_MULTI). */
    public static final int MULTI_RECEIVER_ORDINAL = PipeMessageReceiver.VALUES.length;

    public static final CustomPacketPayload.Type<MessagePipePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:pipe_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePipePayload> STREAM_CODEC =
            StreamCodec.of(MessagePipePayload::encode, MessagePipePayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, MessagePipePayload msg) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.receiverOrdinal);
        BoundedByteArrays.write(buf, msg.payload);
    }

    private static MessagePipePayload decode(RegistryFriendlyByteBuf buf) {
        try {
            BlockPos pos = buf.readBlockPos();
            int receiverOrdinal = buf.readVarInt();
            byte[] payload = BoundedByteArrays.read(buf);
            return new MessagePipePayload(pos, receiverOrdinal, payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Type<MessagePipePayload> type() {
        return TYPE;
    }

    public static void handle(MessagePipePayload message, BCPayloadContext ctx) {
        Level world = ctx.player().level();
        if (world == null) return;

        BlockEntity tile = world.getBlockEntity(message.pos);
        if (!(tile instanceof TilePipeHolder holder)) {
            return;
        }
        Pipe pipe = holder.getPipe();
        if (pipe == null) return;

        PacketBufferBC buffer = new PacketBufferBC(Unpooled.wrappedBuffer(message.payload));
        try {
            if (message.receiverOrdinal == MULTI_RECEIVER_ORDINAL) {
                int mask = buffer.readUnsignedShort();
                for (PipeMessageReceiver receiver : PipeMessageReceiver.VALUES) {
                    if ((mask & (1 << receiver.ordinal())) != 0) {
                        applyReceiverPayload(holder, pipe, receiver, buffer);
                    }
                }
            } else {
                PipeMessageReceiver[] receivers = PipeMessageReceiver.VALUES;
                if (message.receiverOrdinal < 0 || message.receiverOrdinal >= receivers.length) {
                    BCLog.logger.warn("[transport.net] Invalid pipe message receiver ordinal: "
                            + message.receiverOrdinal);
                    return;
                }
                applyReceiverPayload(holder, pipe, receivers[message.receiverOrdinal], buffer);
            }
        } catch (Exception e) {
            BCLog.logger.warn("[transport.net] Error handling pipe payload at " + message.pos, e);
        } finally {
            buffer.release();
        }
    }

    private static void applyReceiverPayload(TilePipeHolder holder, Pipe pipe,
            PipeMessageReceiver receiver, PacketBufferBC buffer) throws IOException {
        switch (receiver) {
            case FLOW -> {
                PipeFlow flow = pipe.getFlow();
                if (flow != null) {
                    boolean hasId = buffer.readBoolean();
                    if (hasId) {
                        int id = buffer.readShort();
                        flow.readPayload(id, buffer, null);
                    }
                }
            }
            case BEHAVIOUR -> {
                if (buffer.readBoolean()) {
                    pipe.readPayload(buffer);
                }
            }
            case WIRES -> holder.wireManager.readPayload(buffer);
            default -> {
                if (receiver.face != null) {
                    buildcraft.api.transport.pluggable.PipePluggable plug = holder.getPluggable(receiver.face);
                    if (plug != null) {
                        plug.readPayload(buffer, receiver.face, Boolean.TRUE);
                    }
                }
            }
        }
    }
}
