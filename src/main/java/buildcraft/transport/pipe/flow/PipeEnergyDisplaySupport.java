/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import java.util.EnumMap;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.misc.VecUtil;

public final class PipeEnergyDisplaySupport {
    private PipeEnergyDisplaySupport() {}

    public interface DisplaySection {
        int getDisplayPower();

        void setDisplayPower(int power);

        PipeEnergyEnumFlow getDisplayFlow();

        void setDisplayFlow(PipeEnergyEnumFlow flow);

        double getClientDisplayFlow();

        void setClientDisplayFlow(double value);

        void setClientDisplayFlowLast(double value);
    }

    public record ClientAnimationState(Vec3 centre, Vec3 centreLast) {}

    public static ClientAnimationState tickClientAnimation(
            Vec3 centre,
            Vec3 centreLast,
            EnumMap<Direction, ? extends DisplaySection> sections) {
        Vec3 newCentreLast = centre;
        Vec3 newCentre = centre;
        for (Direction face : Direction.values()) {
            DisplaySection section = sections.get(face);
            section.setClientDisplayFlowLast(section.getClientDisplayFlow());
            double diff = section.getDisplayFlow().value * 2.4 * face.getAxisDirection().getStep();
            section.setClientDisplayFlow((section.getClientDisplayFlow() + 16 + diff) % 16);

            double centreValue = VecUtil.getValue(newCentre, face.getAxis());
            centreValue = (centreValue + 16 + diff / 2) % 16;
            newCentre = VecUtil.replaceValue(newCentre, face.getAxis(), centreValue);
        }
        return new ClientAnimationState(newCentre, newCentreLast);
    }

    public static void writeDisplayState(FriendlyByteBuf buffer, EnumMap<Direction, ? extends DisplaySection> sections) {
        for (Direction face : Direction.values()) {
            DisplaySection section = sections.get(face);
            buffer.writeInt(section.getDisplayPower());
            buffer.writeEnum(section.getDisplayFlow());
        }
    }

    public static void readDisplayState(FriendlyByteBuf buffer, EnumMap<Direction, ? extends DisplaySection> sections) {
        for (Direction face : Direction.values()) {
            DisplaySection section = sections.get(face);
            section.setDisplayPower(buffer.readInt());
            section.setDisplayFlow(buffer.readEnum(PipeEnergyEnumFlow.class));
        }
    }

    public static void captureDisplaySnapshot(
            EnumMap<Direction, ? extends DisplaySection> sections,
            PipeEnergyEnumFlow[] lastFlows,
            int[] lastDisplayPower) {
        for (Direction face : Direction.values()) {
            DisplaySection section = sections.get(face);
            int index = face.ordinal();
            lastFlows[index] = section.getDisplayFlow();
            lastDisplayPower[index] = section.getDisplayPower();
        }
    }

    public static boolean displayStateChanged(
            EnumMap<Direction, ? extends DisplaySection> sections,
            PipeEnergyEnumFlow[] lastFlows,
            int[] lastDisplayPower) {
        for (Direction face : Direction.values()) {
            DisplaySection section = sections.get(face);
            int index = face.ordinal();
            if (lastFlows[index] != section.getDisplayFlow()
                    || lastDisplayPower[index] != section.getDisplayPower()) {
                return true;
            }
        }
        return false;
    }

    public static void propagateQueriesToNeighbourPipes(
            IPipe pipe,
            long[] transferQuery,
            boolean disabled,
            Class<? extends PipeFlow> flowType,
            NeighbourPowerRequest request) {
        for (Direction face : Direction.values()) {
            if (disabled) {
                continue;
            }
            long query = transferQuery[face.ordinal()];
            if (query <= 0 || !pipe.isConnected(face)) {
                continue;
            }
            IPipe neighbour = pipe.getHolder().getNeighbourPipe(face);
            if (neighbour == null || neighbour.getFlow() == null || !flowType.isInstance(neighbour.getFlow())) {
                continue;
            }
            request.request(neighbour.getFlow(), face.getOpposite(), query);
        }
    }

    @FunctionalInterface
    public interface NeighbourPowerRequest {
        void request(PipeFlow neighbourFlow, Direction from, long amount);
    }
}
