/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public record PayloadWireSync(@Nullable Map<Integer, WireSystem> topology, @Nullable Map<Integer, Boolean> powered, @Nullable int[] removedIds)
   implements CustomPacketPayload {
   private static final byte FLAG_TOPOLOGY = 1;
   private static final byte FLAG_POWERED = 2;
   private static final byte FLAG_REMOVED = 4;
   public static final Type<PayloadWireSync> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:wire_sync"));
   public static final StreamCodec<FriendlyByteBuf, PayloadWireSync> STREAM_CODEC = StreamCodec.of(PayloadWireSync::encode, PayloadWireSync::decode);

   public PayloadWireSync(@Nullable Map<Integer, WireSystem> topology, @Nullable Map<Integer, Boolean> powered) {
      this(topology, powered, null);
   }

   public static PayloadWireSync pack(@Nullable Map<Integer, WireSystem> topology, @Nullable Map<Integer, Boolean> powered, @Nullable int[] removedIds) {
      return new PayloadWireSync(topology, powered, removedIds);
   }

   private static void encode(FriendlyByteBuf buf, PayloadWireSync msg) {
      encodeTo(buf, msg);
   }

   static void encodeTo(FriendlyByteBuf buf, PayloadWireSync msg) {
      int packetStart = buf.writerIndex();
      byte flags = 0;
      if (msg.topology != null && !msg.topology.isEmpty()) {
         flags = (byte)(flags | 1);
      }

      if (msg.powered != null && !msg.powered.isEmpty()) {
         flags = (byte)(flags | 2);
      }

      if (msg.removedIds != null && msg.removedIds.length > 0) {
         flags = (byte)(flags | 4);
      }

      buf.writeByte(flags);
      if ((flags & 1) != 0) {
         int countIndex = buf.writerIndex();
         buf.writeInt(0);
         int written = 0;

         for (Entry<Integer, WireSystem> entry : msg.topology.entrySet()) {
            writeTopologyEntry(buf, entry.getKey(), entry.getValue());
            if (++written >= 2048) {
               break;
            }
         }

         buf.setInt(countIndex, written);
      }

      if ((flags & 2) != 0) {
         int countIndex = buf.writerIndex();
         buf.writeInt(0);
         int written = 0;

         for (Entry<Integer, Boolean> entry : msg.powered.entrySet()) {
            buf.writeInt(entry.getKey());
            buf.writeBoolean(entry.getValue());
            if (++written >= 8192) {
               break;
            }
         }

         buf.setInt(countIndex, written);
      }

      if ((flags & 4) != 0) {
         int count = Math.min(msg.removedIds.length, 2048);
         buf.writeInt(count);

         for (int i = 0; i < count; i++) {
            buf.writeInt(msg.removedIds[i]);
         }
      }

      int size = buf.writerIndex() - packetStart;
      if (size > 524288) {
         throw new IllegalStateException("wire_sync packet exceeded byte budget (" + size + " bytes) — use WireSyncSplitter");
      }
   }

   private static void writeTopologyEntry(FriendlyByteBuf buf, int networkId, WireSystem wireSystem) {
      buf.writeInt(networkId);
      List<WireSystem.WireElement> elements = wireSystem.elements
         .stream()
         .filter(element -> element.type == WireSystem.WireElement.Type.WIRE_PART)
         .collect(Collectors.toList());
      buf.writeInt(elements.size());
      elements.forEach(element -> element.toBytes(buf));
   }

   private static PayloadWireSync decode(FriendlyByteBuf buf) {
      byte flags = buf.readByte();
      Map<Integer, WireSystem> topology = null;
      Map<Integer, Boolean> powered = null;
      int[] removedIds = null;
      if ((flags & 1) != 0) {
         topology = new HashMap<>();
         int count = BCPacketLimits.validateCount(buf.readInt(), 2048, "wire systems");

         for (int i = 0; i < count; i++) {
            int networkId = buf.readInt();
            int localCount = BCPacketLimits.validateCount(buf.readInt(), 4096, "wire elements");
            Builder<WireSystem.WireElement> elements = ImmutableList.builder();

            for (int j = 0; j < localCount; j++) {
               elements.add(new WireSystem.WireElement(buf));
            }

            topology.put(networkId, new WireSystem(elements.build(), null));
         }
      }

      if ((flags & 2) != 0) {
         powered = new HashMap<>();
         int count = BCPacketLimits.validateCount(buf.readInt(), 8192, "wire powered");

         for (int i = 0; i < count; i++) {
            powered.put(buf.readInt(), buf.readBoolean());
         }
      }

      if ((flags & 4) != 0) {
         int count = BCPacketLimits.validateCount(buf.readInt(), 2048, "wire removals");
         removedIds = new int[count];

         for (int i = 0; i < count; i++) {
            removedIds[i] = buf.readInt();
         }
      }

      return new PayloadWireSync(topology, powered, removedIds);
   }

   public Type<PayloadWireSync> type() {
      return TYPE;
   }

   public static void handle(PayloadWireSync message, BCPayloadContext ctx) {
      ClientWireSystems.INSTANCE.applyRemoved(message.removedIds);
      ClientWireSystems.INSTANCE.applyTopology(message.topology);
      if (message.powered != null) {
         applyPoweredState(ctx.player().level(), message.powered);
      }
   }

   private static void applyPoweredState(Level world, Map<Integer, Boolean> hashesPowered) {
      Map<BlockPos, IPipeHolder> holderCache = new HashMap<>();

      for (Entry<Integer, Boolean> hashPowered : hashesPowered.entrySet()) {
         WireSystem wireSystem = ClientWireSystems.INSTANCE.wireSystems.get(hashPowered.getKey());
         if (wireSystem != null) {
            boolean powered = hashPowered.getValue();
            UnmodifiableIterator var7 = wireSystem.elements.iterator();

            while (var7.hasNext()) {
               WireSystem.WireElement element = (WireSystem.WireElement)var7.next();
               if (element.type == WireSystem.WireElement.Type.WIRE_PART) {
                  IPipeHolder holder = holderCache.computeIfAbsent(
                     element.blockPos, pos -> world.getBlockEntity(pos) instanceof IPipeHolder pipeHolder ? pipeHolder : null
                  );
                  if (holder != null && holder.getWireManager() instanceof WireManager wireManager && wireManager.getColorOfPart(element.wirePart) != null) {
                     boolean wasPowered = wireManager.poweredClient.contains(element.wirePart);
                     if (powered != wasPowered) {
                        if (powered) {
                           wireManager.poweredClient.add(element.wirePart);
                        } else {
                           wireManager.poweredClient.remove(element.wirePart);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
