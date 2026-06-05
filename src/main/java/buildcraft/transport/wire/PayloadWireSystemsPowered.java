/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;

import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipeHolder;

public class PayloadWireSystemsPowered implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PayloadWireSystemsPowered> TYPE =
        new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:wire_systems_powered"));

    public static final StreamCodec<FriendlyByteBuf, PayloadWireSystemsPowered> STREAM_CODEC =
        StreamCodec.of(PayloadWireSystemsPowered::encode, PayloadWireSystemsPowered::decode);

    private final Map<Integer, Boolean> hashesPowered;

    public PayloadWireSystemsPowered(Map<Integer, Boolean> hashesPowered) {
        this.hashesPowered = hashesPowered;
    }

    private static void encode(FriendlyByteBuf buf, PayloadWireSystemsPowered msg) {
        buf.writeInt(msg.hashesPowered.size());
        msg.hashesPowered.forEach((wiresHashCode, powered) -> {
            buf.writeInt(wiresHashCode);
            buf.writeBoolean(powered);
        });
    }

    private static PayloadWireSystemsPowered decode(FriendlyByteBuf buf) {
        Map<Integer, Boolean> map = new HashMap<>();
        int count = BCPacketLimits.validateCount(buf.readInt(), BCPacketLimits.MAX_WIRE_POWERED_ENTRIES, "wire powered");
        for (int i = 0; i < count; i++) {
            map.put(buf.readInt(), buf.readBoolean());
        }
        return new PayloadWireSystemsPowered(map);
    }

    @Override
    public Type<PayloadWireSystemsPowered> type() {
        return TYPE;
    }

    public static void handle(PayloadWireSystemsPowered message, BCPayloadContext ctx) {
        Level world = ctx.player().level();
        for (Map.Entry<Integer, Boolean> hashPowered : message.hashesPowered.entrySet()) {
            WireSystem wireSystem = ClientWireSystems.INSTANCE.wireSystems.get(hashPowered.getKey());
            if (wireSystem == null) continue;
            boolean powered = hashPowered.getValue();
            for (WireSystem.WireElement element : wireSystem.elements) {
                if (element.type == WireSystem.WireElement.Type.WIRE_PART) {
                    BlockEntity tile = world.getBlockEntity(element.blockPos);
                    if (tile instanceof IPipeHolder holder) {
                        IWireManager iWireManager = holder.getWireManager();
                        if (iWireManager instanceof WireManager wireManager) {
                            if (wireManager.getColorOfPart(element.wirePart) != null) {
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
