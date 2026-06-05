/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.ToIntFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.transfer.energy.EnergyHandler;
import buildcraft.lib.transfer.transaction.TransactionContext;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.BCCoreConfig;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowRedstoneFlux;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraft.api.transport.pipe.PipeEventRedstoneFlux;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AverageInt;

@SuppressWarnings("this-escape")
public class PipeFlowRedstoneFlux extends PipeFlow implements IFlowRedstoneFlux, IDebuggable {
    private static final int DEFAULT_MAX_POWER = 100;
    public static final int NET_POWER_AMOUNTS = 2;

    public Vec3 clientDisplayFlowCentre = VecUtil.VEC_HALF;
    public Vec3 clientDisplayFlowCentreLast = VecUtil.VEC_HALF;
    public long clientLastDisplayTime = 0;

    private int maxPower = -1;
    private boolean disabled = false;

    private long currentWorldTime;

    private boolean isReceiver = false;
    private final EnumMap<Direction, Section> sections;
    private SafeTimeTracker networkTracker;

    private SafeTimeTracker getNetworkTracker() {
        if (networkTracker == null) {
            networkTracker = new SafeTimeTracker(BCCoreConfig.networkUpdateRate.get(), 4);
        }
        return networkTracker;
    }

    public PipeFlowRedstoneFlux(IPipe pipe) {
        super(pipe);
        sections = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            sections.put(face, new Section(face));
        }
    }

    public PipeFlowRedstoneFlux(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        isReceiver = nbt.getBooleanOr("isReceiver", false);
        sections = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            sections.put(face, new Section(face));
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.putBoolean("isReceiver", isReceiver);
        return nbt;
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, Object side) {
        super.writePayload(id, buffer, side);
        if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
            PipeEnergyDisplaySupport.writeDisplayState(buffer, sections);
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, Object side) throws IOException {
        super.readPayload(id, buffer, side);
        if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
            PipeEnergyDisplaySupport.readDisplayState(buffer, sections);
        }
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof PipeFlowRedstoneFlux;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {

        buildcraft.lib.transfer.energy.EnergyHandler handler =
            pipe.getHolder().getCapabilityFromPipe(face, buildcraft.lib.attachments.Attachments.Energy.BLOCK);
        return handler != null;
    }

    @Override
    public void reconfigure() {
        PipeEventRedstoneFlux.Configure configure = new PipeEventRedstoneFlux.Configure(pipe.getHolder(), this);
        RedstoneFluxTransferInfo pti = PipeApi.getRfTransferInfo(pipe.getDefinition());
        configure.setReceiver(pti.isReceiver);
        configure.setMaxPower(pti.transferPerTick);
        pipe.getHolder().fireEvent(configure);
        boolean wasReceiver = isReceiver;
        isReceiver = configure.isReceiver();
        if (wasReceiver != isReceiver) {
            pipe.markForUpdate();
        }
        maxPower = configure.getMaxPower();
        disabled = configure.isTransferDisabled();
        if (maxPower <= 0) {
            maxPower = DEFAULT_MAX_POWER;
        }
    }

    @Override
    public int tryExtractPower(int maxExtracted, Direction from) {
        if (!isReceiver || disabled) {
            return 0;
        }
        EnergyHandler source = pipe.getHolder().getCapabilityFromPipe(
            from, buildcraft.lib.attachments.Attachments.Energy.BLOCK
        );
        if (source == null) {
            return 0;
        }
        try (buildcraft.lib.transfer.transaction.Transaction transaction =
                buildcraft.lib.transfer.transaction.Transaction.openRoot()) {
            int extracted = source.extract(maxExtracted, transaction);
            transaction.commit();
            return extracted;
        }
    }

    @Override
    public boolean onFlowActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        return super.onFlowActivate(player, trace, hitX, hitY, hitZ, part);
    }

    public Section getSection(Direction side) {
        return sections.get(side);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Object capability, Direction facing) {
        if (facing == null) {
            return null;
        }
        if (capability == buildcraft.lib.attachments.Attachments.Energy.BLOCK || capability == CapUtil.CAP_ENERGY) {
            return (T) sections.get(facing);
        }
        return null;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("maxPower = " + maxPower);
        left.add("isReceiver = " + isReceiver);
        left.add(
            "internalPower = " + arrayToString(s -> s.internalPower) + " <- " + arrayToString(s -> s.internalNextPower)
        );
        left.add("- powerQuery: " + arrayToString(s -> s.powerQuery) + " <- " + arrayToString(s -> s.nextPowerQuery));
        left.add("- power: OUT " + arrayToString(s -> s.debugPowerOutput));
        left.add("- power: OFFERED " + arrayToString(s -> s.debugPowerOffered));
    }

    private String arrayToString(ToIntFunction<Section> getter) {
        long[] arr = new long[6];
        for (Direction face : Direction.values()) {
            arr[face.ordinal()] = getter.applyAsInt(sections.get(face));
        }
        return Arrays.toString(arr);
    }

    @Override
    public void onTick() {
        if (maxPower == -1) {
            reconfigure();
        }
        if (pipe.getHolder().getPipeWorld().isClientSide()) {
            PipeEnergyDisplaySupport.ClientAnimationState state = PipeEnergyDisplaySupport.tickClientAnimation(
                    clientDisplayFlowCentre, clientDisplayFlowCentreLast, sections);
            clientDisplayFlowCentreLast = state.centreLast();
            clientDisplayFlowCentre = state.centre();
            return;
        }

        PipeEnergyEnumFlow[] lastFlows = new PipeEnergyEnumFlow[6];
        int[] lastDisplayPower = new int[6];
        PipeEnergyDisplaySupport.captureDisplaySnapshot(sections, lastFlows, lastDisplayPower);

        step();

        for (Direction face : Direction.values()) {
            Section s = sections.get(face);
            if (s.internalPower > 0) {
                int totalPowerQuery = 0;
                for (Direction face2 : Direction.values()) {
                    if (face != face2) {
                        totalPowerQuery += sections.get(face2).powerQuery;
                    }
                }

                boolean returnPower = false;
                if (totalPowerQuery <= 0 && s.powerQuery > 0) {
                    totalPowerQuery = s.powerQuery;
                    returnPower = true;
                }

                if (totalPowerQuery > 0) {
                    int unusedPowerQuery = totalPowerQuery;
                    for (Direction face2 : Direction.values()) {
                        if (face == face2 && !returnPower) {
                            continue;
                        }
                        Section s2 = sections.get(face2);
                        if (s2.powerQuery > 0) {
                            int watts = (int) Math.min(s.internalPower * (long) s2.powerQuery / unusedPowerQuery, s.internalPower);
                            unusedPowerQuery -= s2.powerQuery;
                            IPipe neighbour = pipe.getConnectedPipe(face2);
                            int leftover = watts;
                            if (
                                neighbour != null && neighbour.getFlow() instanceof PipeFlowRedstoneFlux && neighbour
                                    .isConnected(face2.getOpposite())
                            ) {
                                PipeFlowRedstoneFlux oFlow = (PipeFlowRedstoneFlux) neighbour.getFlow();
                                leftover = oFlow.sections.get(face2.getOpposite()).receivePowerInternal(watts);
                            } else {
                                EnergyHandler receiver = pipe.getHolder().getCapabilityFromPipe(
                                    face2, buildcraft.lib.attachments.Attachments.Energy.BLOCK
                                );
                                if (receiver != null) {
                                    try (buildcraft.lib.transfer.transaction.Transaction transaction = buildcraft.lib.transfer.transaction.Transaction.openRoot()) {
                                        int accepted = receiver.insert(watts, transaction);
                                        leftover = watts - accepted;
                                        transaction.commit();
                                    }
                                }
                            }
                            int used = watts - leftover;
                            s.internalPower -= used;
                            s2.debugPowerOutput += used;

                            s.powerAverage.push(used);
                            s2.powerAverage.push(used);

                            s.displayFlow = PipeEnergyEnumFlow.OUT;
                            s2.displayFlow = PipeEnergyEnumFlow.IN;
                        }
                    }
                }
            }
        }

        for (Section s : sections.values()) {
            s.powerAverage.tick();
            double value = s.powerAverage.getAverage() / maxPower;
            value = Math.sqrt(value);
            s.displayPower = (int) (value * MjAPI.MJ);
        }

        for (Direction face : Direction.values()) {
            if (pipe.getConnectedType(face) != ConnectedType.TILE) {
                continue;
            }
            EnergyHandler recv = pipe.getHolder().getCapabilityFromPipe(face, buildcraft.lib.attachments.Attachments.Energy.BLOCK);
            if (recv != null) {

                boolean canReceive;
                try (buildcraft.lib.transfer.transaction.Transaction tx =
                         buildcraft.lib.transfer.transaction.Transaction.openRoot()) {
                    canReceive = recv.insert(1, tx) > 0;

                }
                if (canReceive) {
                    int requested = (int) (recv.getCapacityAsLong() - recv.getAmountAsLong());
                    if (requested > 0) {
                        requestPower(face, requested);
                    }
                }
            }
        }

        long[] transferQuery = new long[6];
        for (Direction face : Direction.values()) {
            if (!pipe.isConnected(face)) {
                continue;
            }
            int query = 0;
            for (Direction face2 : Direction.values()) {
                if (face != face2) {
                    query += sections.get(face2).powerQuery;
                }
            }
            transferQuery[face.ordinal()] = query;
        }

        PipeEnergyDisplaySupport.propagateQueriesToNeighbourPipes(
                pipe,
                transferQuery,
                disabled,
                PipeFlowRedstoneFlux.class,
                (neighbourFlow, from, amount) ->
                        ((PipeFlowRedstoneFlux) neighbourFlow).requestPower(from, (int) amount));

        if (PipeEnergyDisplaySupport.displayStateChanged(sections, lastFlows, lastDisplayPower)
                && getNetworkTracker().markTimeIfDelay(pipe.getHolder().getPipeWorld())) {
            sendPayload(NET_POWER_AMOUNTS);
        }
    }

    private void step() {
        long now = pipe.getHolder().getPipeWorld().getGameTime();
        if (currentWorldTime != now) {
            currentWorldTime = now;
            sections.values().forEach(Section::step);
        }
    }

    private void requestPower(Direction from, int amount) {
        step();

        Section s = sections.get(from);
        if (pipe.getBehaviour() instanceof IPipeTransportRfHook) {
            s.nextPowerQuery += ((IPipeTransportRfHook) pipe.getBehaviour()).requestPower(from, amount);
        } else {
            s.nextPowerQuery += amount;
        }
        s.nextPowerQuery = Math.min(s.nextPowerQuery, maxPower);
    }

    public int getPowerRequested(@Nullable Direction side) {
        int req = 0;
        for (Direction face : Direction.values()) {
            if (side == null || face != side) {
                req += sections.get(face).powerQuery;
            }
        }
        return req;
    }

    public class Section implements EnergyHandler, PipeEnergyDisplaySupport.DisplaySection {
        public final Direction side;

        public double clientDisplayFlow, clientDisplayFlowLast;

        public int displayPower;
        public PipeEnergyEnumFlow displayFlow = PipeEnergyEnumFlow.STATIONARY;
        public int nextPowerQuery;
        public int internalNextPower;
        public final AverageInt powerAverage = new AverageInt(1);

        int powerQuery;
        int internalPower;

        int debugPowerOutput, debugPowerOffered;

        public Section(Direction side) {
            this.side = side;
            clientDisplayFlow = (side.getAxisDirection() == AxisDirection.POSITIVE ? 7 : 1) / 8.0;
        }

        void step() {
            powerQuery = nextPowerQuery;
            nextPowerQuery = 0;

            internalPower += internalNextPower;
            internalNextPower = 0;
        }

        private final buildcraft.lib.transfer.transaction.SnapshotJournal<Integer> powerJournal = new buildcraft.lib.transfer.transaction.SnapshotJournal<Integer>() {
            @Override
            protected Integer createSnapshot() {
                return internalNextPower;
            }

            @Override
            protected void revertToSnapshot(Integer snapshot) {
                internalNextPower = snapshot;
            }
        };

        @Override
        public int insert(int maxReceive, TransactionContext transaction) {
            if (isReceiver && maxReceive > 0) {
                PipeFlowRedstoneFlux.this.step();
                int maxCanAccept = maxPower - (internalPower + internalNextPower);
                if (maxCanAccept <= 0) return 0;

                int accepted = Math.min(maxCanAccept, maxReceive);
                if (accepted > 0) {
                    powerJournal.updateSnapshots(transaction);
                    debugPowerOffered += accepted;
                    internalNextPower += accepted;
                    return accepted;
                }
            }
            return 0;
        }

        @Override
        public int extract(int maxExtract, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long getAmountAsLong() {
            return internalPower + internalNextPower;
        }

        @Override
        public long getCapacityAsLong() {
            return maxPower;
        }

        int receivePowerInternal(int sent) {
            if (sent > 0) {
                PipeFlowRedstoneFlux.this.step();
                int max = maxPower - (internalPower + internalNextPower);
                if (max <= 0) {
                    return sent;
                }
                int accepted = Math.min(max, sent);
                debugPowerOffered += accepted;
                internalNextPower += accepted;
                return sent - accepted;
            }
            return sent;
        }

        @Override
        public int getDisplayPower() {
            return displayPower;
        }

        @Override
        public void setDisplayPower(int power) {
            displayPower = power;
        }

        @Override
        public PipeEnergyEnumFlow getDisplayFlow() {
            return displayFlow;
        }

        @Override
        public void setDisplayFlow(PipeEnergyEnumFlow flow) {
            displayFlow = flow;
        }

        @Override
        public double getClientDisplayFlow() {
            return clientDisplayFlow;
        }

        @Override
        public void setClientDisplayFlow(double value) {
            clientDisplayFlow = value;
        }

        @Override
        public void setClientDisplayFlowLast(double value) {
            clientDisplayFlowLast = value;
        }
    }
}
