/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.transport.pipe.flow.PipeFlowItems;

public class MessageMultiPipeItem implements CustomPacketPayload {

    private static final int MAX_ITEMS_PER_PIPE = 10;
    private static final int MAX_POSITIONS = 4000;

    public static final CustomPacketPayload.Type<MessageMultiPipeItem> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:multi_pipe_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageMultiPipeItem> STREAM_CODEC =
            StreamCodec.of(MessageMultiPipeItem::encode, MessageMultiPipeItem::decode);

    public final Map<BlockPos, List<TravellingItemData>> items = new HashMap<>();

    public MessageMultiPipeItem() {}

    private static void encode(RegistryFriendlyByteBuf buf, MessageMultiPipeItem msg) {
        int blockCount = Math.min(msg.items.size(), MAX_POSITIONS);
        buf.writeShort(blockCount);
        int blockIndex = 0;
        for (var entry : msg.items.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            List<TravellingItemData> list = entry.getValue();
            int itemCount = Math.min(list.size(), MAX_ITEMS_PER_PIPE);
            buf.writeByte(itemCount);
            for (int i = 0; i < itemCount; i++) {
                list.get(i).toBuffer(buf);
            }
            if (++blockIndex >= blockCount) {
                break;
            }
        }
    }

    private static MessageMultiPipeItem decode(RegistryFriendlyByteBuf buf) {
        MessageMultiPipeItem msg = new MessageMultiPipeItem();
        int blockCount = BCPacketLimits.validateCount(buf.readShort(), BCPacketLimits.MAX_PIPE_ITEM_BLOCKS, "pipe item blocks");
        for (int b = 0; b < blockCount; b++) {
            BlockPos pos = buf.readBlockPos();
            List<TravellingItemData> posItems = new ArrayList<>();
            msg.items.put(pos, posItems);
            int itemCount = BCPacketLimits.validateCount(buf.readUnsignedByte(),
                    BCPacketLimits.MAX_PIPE_ITEMS_PER_BLOCK, "pipe items");
            for (int i = 0; i < itemCount; i++) {
                posItems.add(TravellingItemData.fromBuffer(buf));
            }
        }
        return msg;
    }

    @Override
    public Type<MessageMultiPipeItem> type() {
        return TYPE;
    }

    public void append(BlockPos pos, ItemStack stack, int stackCount, boolean toCenter,
            Direction side, @Nullable DyeColor colour, int timeToDest) {
        List<TravellingItemData> list = items.get(pos);
        if (list == null) {
            if (items.size() >= MAX_POSITIONS) {
                return;
            }
            list = new ArrayList<>();
            items.put(pos, list);
        }
        if (list.size() >= MAX_ITEMS_PER_PIPE) {
            return;
        }
        list.add(new TravellingItemData(stack, stackCount, toCenter, side, colour, timeToDest));
    }

    public static void handle(MessageMultiPipeItem message, BCPayloadContext ctx) {
        Level world = ctx.player().level();
        if (world == null) return;
        for (var entry : message.items.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IPipeHolder holder) {
                IPipe pipe = holder.getPipe();
                if (pipe == null) continue;
                PipeFlow flow = pipe.getFlow();
                if (flow instanceof PipeFlowItems flowItems) {
                    flowItems.handleClientReceivedItems(entry.getValue());
                }
            }
        }
    }

    public static class TravellingItemData {
        public final ItemStack stack;
        public final int stackCount;
        public final boolean toCenter;
        public final Direction side;
        public final @Nullable DyeColor colour;
        public final int timeToDest;

        public TravellingItemData(ItemStack stack, int stackCount, boolean toCenter,
                Direction side, @Nullable DyeColor colour, int timeToDest) {
            this.stack = stack;
            this.stackCount = stackCount;
            this.toCenter = toCenter;
            this.side = side;
            this.colour = colour;
            this.timeToDest = timeToDest;
        }

        void toBuffer(RegistryFriendlyByteBuf buf) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            buf.writeVarInt(stackCount);
            buf.writeBoolean(toCenter);
            buf.writeEnum(side);
            buf.writeByte(colour == null ? -1 : colour.getId());
            buf.writeVarInt(timeToDest);
        }

        static TravellingItemData fromBuffer(RegistryFriendlyByteBuf buf) {
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            int stackCount = buf.readVarInt();
            boolean toCenter = buf.readBoolean();
            Direction side = buf.readEnum(Direction.class);
            int colourByte = buf.readByte();
            DyeColor colour = colourByte < 0 ? null : DyeColor.byId(colourByte);
            int timeToDest = buf.readVarInt();
            return new TravellingItemData(stack, stackCount, toCenter, side, colour, timeToDest);
        }
    }
}
