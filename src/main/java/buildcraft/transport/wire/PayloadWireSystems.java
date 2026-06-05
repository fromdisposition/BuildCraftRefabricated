/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;

public class PayloadWireSystems implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PayloadWireSystems> TYPE =
        new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:wire_systems"));

    public static final StreamCodec<FriendlyByteBuf, PayloadWireSystems> STREAM_CODEC =
        StreamCodec.of(PayloadWireSystems::encode, PayloadWireSystems::decode);

    private final Map<Integer, WireSystem> wireSystems;

    public PayloadWireSystems(Map<Integer, WireSystem> wireSystems) {
        this.wireSystems = wireSystems;
    }

    private static void encode(FriendlyByteBuf buf, PayloadWireSystems msg) {
        buf.writeInt(msg.wireSystems.size());
        msg.wireSystems.forEach((wiresHashCode, wireSystem) -> {
            buf.writeInt(wiresHashCode);
            List<WireSystem.WireElement> elements = wireSystem.elements.stream()
                .filter(element -> element.type == WireSystem.WireElement.Type.WIRE_PART)
                .collect(Collectors.toList());
            buf.writeInt(elements.size());
            elements.forEach(element -> element.toBytes(buf));
        });
    }

    private static PayloadWireSystems decode(FriendlyByteBuf buf) {
        Map<Integer, WireSystem> systems = new HashMap<>();
        int count = BCPacketLimits.validateCount(buf.readInt(), BCPacketLimits.MAX_WIRE_SYSTEMS, "wire systems");
        for (int i = 0; i < count; i++) {
            int wiresHashCode = buf.readInt();
            int localCount = BCPacketLimits.validateCount(buf.readInt(),
                    BCPacketLimits.MAX_WIRE_ELEMENTS_PER_SYSTEM, "wire elements");

            ImmutableList.Builder<WireSystem.WireElement> elements = ImmutableList.builder();
            for (int j = 0; j < localCount; j++) {
                elements.add(new WireSystem.WireElement(buf));
            }
            WireSystem wireSystem = new WireSystem(elements.build(), null);
            systems.put(wiresHashCode, wireSystem);
        }
        return new PayloadWireSystems(systems);
    }

    @Override
    public Type<PayloadWireSystems> type() {
        return TYPE;
    }

    public static void handle(PayloadWireSystems message, BCPayloadContext ctx) {
        ClientWireSystems.INSTANCE.wireSystems.clear();
        ClientWireSystems.INSTANCE.wireSystems.putAll(message.wireSystems);
    }
}
