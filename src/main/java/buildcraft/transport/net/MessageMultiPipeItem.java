/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.net;

import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MessageMultiPipeItem implements CustomPacketPayload {
   public static final Type<MessageMultiPipeItem> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:multi_pipe_item"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessageMultiPipeItem> STREAM_CODEC = StreamCodec.of(
      MessageMultiPipeItem::encode, MessageMultiPipeItem::decode
   );
   public final Map<BlockPos, List<MessageMultiPipeItem.TravellingItemData>> items = new HashMap<>();
   private int estimatedBytes = 0;

   private static void encode(RegistryFriendlyByteBuf buf, MessageMultiPipeItem msg) {
      List<Entry<BlockPos, List<MessageMultiPipeItem.TravellingItemData>>> sorted = new ArrayList<>(msg.items.entrySet());
      sorted.sort(Comparator.comparingLong(e -> e.getKey().asLong()));
      int blockCount = Math.min(sorted.size(), 4000);
      buf.writeShort(blockCount);

      for (int blockIndex = 0; blockIndex < blockCount; blockIndex++) {
         Entry<BlockPos, List<MessageMultiPipeItem.TravellingItemData>> entry = sorted.get(blockIndex);
         buf.writeBlockPos(entry.getKey());
         List<MessageMultiPipeItem.TravellingItemData> list = entry.getValue();
         int itemCount = Math.min(list.size(), 10);
         buf.writeByte(itemCount);

         for (int i = 0; i < itemCount; i++) {
            list.get(i).toBuffer(buf);
         }
      }
   }

   private static MessageMultiPipeItem decode(RegistryFriendlyByteBuf buf) {
      MessageMultiPipeItem msg = new MessageMultiPipeItem();
      int blockCount = BCPacketLimits.validateCount(buf.readShort(), 4000, "pipe item blocks");

      for (int b = 0; b < blockCount; b++) {
         BlockPos pos = buf.readBlockPos();
         List<MessageMultiPipeItem.TravellingItemData> posItems = new ArrayList<>();
         msg.items.put(pos, posItems);
         int itemCount = BCPacketLimits.validateCount(buf.readUnsignedByte(), 10, "pipe items");

         for (int i = 0; i < itemCount; i++) {
            posItems.add(MessageMultiPipeItem.TravellingItemData.fromBuffer(buf));
         }
      }

      return msg;
   }

   public Type<MessageMultiPipeItem> type() {
      return TYPE;
   }

   public void append(BlockPos pos, ItemStack stack, int stackCount, boolean toCenter, Direction side, @Nullable DyeColor colour, int timeToDest) {
      MessageMultiPipeItem.TravellingItemData data = new MessageMultiPipeItem.TravellingItemData(stack, stackCount, toCenter, side, colour, timeToDest);
      int entryBytes = data.estimateBytes();
      if (this.estimatedBytes + entryBytes > 524288) {
         BCLog.logger.warn("[transport.net] Dropping pipe item visual: packet byte budget exceeded");
      } else {
         List<MessageMultiPipeItem.TravellingItemData> list = this.items.get(pos);
         if (list == null) {
            if (this.items.size() >= 4000) {
               BCLog.logger.warn("[transport.net] Dropping pipe item visual: max positions reached");
               return;
            }

            list = new ArrayList<>();
            this.items.put(pos, list);
         }

         if (list.size() >= 10) {
            BCLog.logger.warn("[transport.net] Dropping pipe item visual: max items per pipe reached at {}", pos);
         } else {
            list.add(data);
            this.estimatedBytes += entryBytes;
         }
      }
   }

   public static void handle(MessageMultiPipeItem message, BCPayloadContext ctx) {
      Level world = ctx.player().level();
      if (world != null) {
         for (Entry<BlockPos, List<MessageMultiPipeItem.TravellingItemData>> entry : message.items.entrySet()) {
            BlockPos pos = entry.getKey();
            if (world.getBlockEntity(pos) instanceof IPipeHolder holder) {
               IPipe pipe = holder.getPipe();
               if (pipe != null && pipe.getFlow() instanceof PipeFlowItems flowItems) {
                  flowItems.handleClientReceivedItems(entry.getValue());
               }
            }
         }
      }
   }

   public static class TravellingItemData {
      public final ItemStack stack;
      public final int stackCount;
      public final boolean toCenter;
      public final Direction side;
      @Nullable
      public final DyeColor colour;
      public final int timeToDest;

      public TravellingItemData(ItemStack stack, int stackCount, boolean toCenter, Direction side, @Nullable DyeColor colour, int timeToDest) {
         this.stack = stack;
         this.stackCount = stackCount;
         this.toCenter = toCenter;
         this.side = side;
         this.colour = colour;
         this.timeToDest = timeToDest;
      }

      private boolean useCompactStack() {
         return !this.stack.isEmpty() && this.stack.getComponents().isEmpty();
      }

      int estimateBytes() {
         if (!this.useCompactStack()) {
            return 96;
         }

         Identifier id = BuiltInRegistries.ITEM.getKey(this.stack.getItem());
         return 16 + id.toString().length() * 3;
      }

      void toBuffer(RegistryFriendlyByteBuf buf) {
         boolean compact = this.useCompactStack();
         buf.writeBoolean(compact);
         if (compact) {
            Identifier id = BuiltInRegistries.ITEM.getKey(this.stack.getItem());
            buf.writeUtf(id.toString());
         } else {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.stack);
         }

         buf.writeVarInt(this.stackCount);
         buf.writeBoolean(this.toCenter);
         buf.writeEnum(this.side);
         buf.writeByte(this.colour == null ? -1 : this.colour.getId());
         buf.writeVarInt(this.timeToDest);
      }

      static MessageMultiPipeItem.TravellingItemData fromBuffer(RegistryFriendlyByteBuf buf) {
         boolean compact = buf.readBoolean();
         ItemStack stack;
         if (compact) {
            Identifier id = Identifier.parse(buf.readUtf());
            Item item = Mc26Compat.getItem(id);
            stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
         } else {
            stack = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
         }

         int stackCount = buf.readVarInt();
         boolean toCenter = buf.readBoolean();
         Direction side = (Direction)buf.readEnum(Direction.class);
         int colourByte = buf.readByte();
         DyeColor colour = colourByte < 0 ? null : DyeColor.byId(colourByte);
         int timeToDest = buf.readVarInt();
         return new MessageMultiPipeItem.TravellingItemData(stack, stackCount, toCenter, side, colour, timeToDest);
      }
   }
}
