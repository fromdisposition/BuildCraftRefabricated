/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.ICustomPipeConnection;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeConnectionAPI;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeEventConnectionChange;
import buildcraft.api.transport.pipe.PipeFaceTex;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.misc.NBTUtilBC;

public final class Pipe implements IPipe, IDebuggable {
    private static final float DEFAULT_CONNECTION_DISTANCE = 0.25f;

    public final IPipeHolder holder;
    public final PipeDefinition definition;
    public final PipeBehaviour behaviour;
    public final PipeFlow flow;
    private DyeColor colour = null;
    private boolean updateMarked = true;
    private final EnumMap<Direction, Float> connected = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, ConnectedType> types = new EnumMap<>(Direction.class);

    public Pipe(IPipeHolder holder, PipeDefinition definition) {
        this.holder = holder;
        this.definition = definition;
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        this.flow = definition.flowType.creator.createFlow(this);
    }

    public Pipe(IPipeHolder holder, CompoundTag nbt) throws InvalidInputDataException {
        this.holder = holder;
        String colStr = nbt.getStringOr("col", "");
        if (!colStr.isEmpty()) {
            this.colour = NBTUtilBC.readEnum(nbt.get("col"), DyeColor.class);
        }
        this.definition = PipeRegistry.INSTANCE.loadDefinition(nbt.getStringOr("def", ""));
        if (!definition.canBeColoured) {
            colour = null;
        }
        this.behaviour = definition.logicLoader.loadBehaviour(this, nbt.getCompoundOrEmpty("beh"));
        this.flow = definition.flowType.loader.loadFlow(this, nbt.getCompoundOrEmpty("flow"));

        int connectionData = nbt.getIntOr("con", 0);
        for (Direction face : Direction.values()) {
            int data = (connectionData >>> (face.ordinal() * 2)) & 0b11;
            if (data == 0b01) {
                connected.put(face, DEFAULT_CONNECTION_DISTANCE);
                types.put(face, ConnectedType.PIPE);
            } else if (data == 0b10) {
                connected.put(face, DEFAULT_CONNECTION_DISTANCE);
                types.put(face, ConnectedType.TILE);
            }
        }
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        if (colour != null) {
            nbt.put("col", NBTUtilBC.writeEnum(colour));
        }
        nbt.putString("def", definition.identifier);
        nbt.put("beh", behaviour.writeToNbt());
        nbt.put("flow", flow.writeToNbt());

        int connectionData = 0;
        for (Direction face : Direction.values()) {
            ConnectedType type = types.get(face);
            if (type != null) {
                int data = type == ConnectedType.PIPE ? 0b01 : 0b10;
                connectionData |= data << (face.ordinal() * 2);
            }
        }
        nbt.putInt("con", connectionData);
        return nbt;
    }

    public void readFromNbt(CompoundTag nbt) {

        String colStr = nbt.getStringOr("col", "");
        if (!colStr.isEmpty()) {
            this.colour = NBTUtilBC.readEnum(nbt.get("col"), DyeColor.class);
        } else {
            this.colour = null;
        }
        if (!definition.canBeColoured) {
            colour = null;
        }

        connected.clear();
        types.clear();
        int connectionData = nbt.getIntOr("con", 0);
        for (Direction face : Direction.values()) {
            int data = (connectionData >>> (face.ordinal() * 2)) & 0b11;
            if (data == 0b01) {
                connected.put(face, DEFAULT_CONNECTION_DISTANCE);
                types.put(face, ConnectedType.PIPE);
            } else if (data == 0b10) {
                connected.put(face, DEFAULT_CONNECTION_DISTANCE);
                types.put(face, ConnectedType.TILE);
            }
        }

        if (nbt.contains("beh")) {
            behaviour.readFromNbt(nbt.getCompoundOrEmpty("beh"));
        }

        if (nbt.contains("flow")) {
            flow.readFromNbt(nbt.getCompoundOrEmpty("flow"));
        }
    }

    public void writePayload(FriendlyByteBuf buffer) {
        buffer.writeByte(colour == null ? 0 : colour.getId() + 1);
        for (Direction face : Direction.values()) {
            Float con = connected.get(face);
            if (con != null) {
                buffer.writeBoolean(true);
                buffer.writeFloat(con);
                ConnectedType type = types.get(face);
                buffer.writeByte(type == null ? -1 : type.ordinal());
            } else {
                buffer.writeBoolean(false);
            }
        }
        behaviour.writePayload(buffer);
    }

    public void readPayload(FriendlyByteBuf buffer) throws IOException {
        connected.clear();
        types.clear();

        int nColour = buffer.readUnsignedByte();
        colour = nColour == 0 ? null : DyeColor.byId(nColour - 1);

        for (Direction face : Direction.values()) {
            if (buffer.readBoolean()) {
                float dist = buffer.readFloat();
                connected.put(face, dist);
                int typeOrd = buffer.readByte();
                if (typeOrd >= 0 && typeOrd < ConnectedType.values().length) {
                    types.put(face, ConnectedType.values()[typeOrd]);
                }
            }
        }

        behaviour.readPayload(buffer, null);
    }

    @Override
    public IPipeHolder getHolder() {
        return holder;
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }

    @Override
    public PipeBehaviour getBehaviour() {
        return behaviour;
    }

    @Override
    public PipeFlow getFlow() {
        return flow;
    }

    @Override
    public DyeColor getColour() {
        return this.colour;
    }

    @Override
    public void setColour(DyeColor colour) {
        if (definition.canBeColoured) {
            this.colour = colour;
            markForUpdate();

            holder.scheduleRenderUpdate();
        }
    }

    public void onLoad() {
        markForUpdate();
    }

    public void onTick() {
        ProfilerFiller _profiler = Profiler.get();
        if (updateMarked) {
            updateConnections();
        }
        _profiler.push("buildcraft:pipe_behaviour");
        try {
            behaviour.onTick();
        } finally {
            _profiler.pop();
        }
        _profiler.push("buildcraft:pipe_flow");
        try {
            flow.onTick();
        } finally {
            _profiler.pop();
        }
        if (updateMarked) {
            updateConnections();
        }
    }

    public void postPluggableTick() {
        flow.postPluggableTick();
    }

    private void updateConnections() {
        ProfilerFiller _profiler = Profiler.get();
        _profiler.push("buildcraft:pipe_connections");
        try {
        if (holder.getPipeWorld().isClientSide()) {
            return;
        }
        updateMarked = false;

        EnumMap<Direction, Float> old = connected.clone();

        connected.clear();
        types.clear();

        for (Direction facing : Direction.values()) {

            var plug = getHolder().getPluggable(facing);
            if (plug != null && plug.isBlocking()) {
                continue;
            }
            BlockEntity oTile = getHolder().getNeighbourTile(facing);
            if (oTile == null) {
                continue;
            }
            IPipe oPipe = getHolder().getNeighbourPipe(facing);
            if (oPipe != null) {
                PipeBehaviour oBehaviour = oPipe.getBehaviour();
                if (oBehaviour == null) {
                    continue;
                }

                var oPlug = oPipe.getHolder().getPluggable(facing.getOpposite());
                if (oPlug == null || !oPlug.isBlocking()) {
                    if (canPipesConnect(facing, this, oPipe)) {
                        connected.put(facing, DEFAULT_CONNECTION_DISTANCE);
                        types.put(facing, ConnectedType.PIPE);
                    }
                    continue;
                }
            }

            BlockPos nPos = holder.getPipePos().relative(facing);
            var neighbour = holder.getPipeWorld().getBlockState(nPos);

            ICustomPipeConnection cust = PipeConnectionAPI.getCustomConnection(neighbour.getBlock());
            if (cust == null) {
                cust = DefaultPipeConnection.INSTANCE;
            }
            float ext = DEFAULT_CONNECTION_DISTANCE
                + cust.getExtension(holder.getPipeWorld(), nPos, facing.getOpposite(), neighbour);

            if (behaviour.shouldForceConnection(facing, oTile) || flow.shouldForceConnection(facing, oTile)
                || (behaviour.canConnect(facing, oTile) && flow.canConnect(facing, oTile))) {
                connected.put(facing, ext);
                types.put(facing, ConnectedType.TILE);
            }
        }
        if (!old.equals(connected)) {
            for (Direction face : Direction.values()) {
                boolean o = old.containsKey(face);
                boolean n = connected.containsKey(face);
                if (o != n) {
                    IPipe oPipe = getHolder().getNeighbourPipe(face);
                    if (oPipe != null) {
                        oPipe.markForUpdate();
                    }
                    holder.fireEvent(new PipeEventConnectionChange(holder, face));
                }
            }

            getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
        } finally {
            _profiler.pop();
        }
    }

    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {

        flow.addDrops(toDrop, fortune);
        behaviour.addDrops(toDrop, fortune);
    }

    public static boolean canPipesConnect(Direction to, IPipe one, IPipe two) {
        return canColoursConnect(one.getColour(), two.getColour())
        && canBehavioursConnect(to, one.getBehaviour(), two.getBehaviour())
        && canFlowsConnect(to, one.getFlow(), two.getFlow());
    }

    public static boolean canColoursConnect(DyeColor one, DyeColor two) {
        return one == null || two == null || one == two;
    }

    public static boolean canBehavioursConnect(Direction to, PipeBehaviour one, PipeBehaviour two) {
        return one.canConnect(to, two) && two.canConnect(to.getOpposite(), one);
    }

    public static boolean canFlowsConnect(Direction to, PipeFlow one, PipeFlow two) {
        return one.canConnect(to, two) && two.canConnect(to.getOpposite(), one);
    }

    @Override
    public void markForUpdate() {
        updateMarked = true;
    }

    @Override
    public BlockEntity getConnectedTile(Direction side) {
        if (connected.containsKey(side)) {
            BlockEntity offset = getHolder().getNeighbourTile(side);
            if (offset == null && !getHolder().getPipeWorld().isClientSide()) {
                markForUpdate();
            } else {
                return offset;
            }
        }
        return null;
    }

    @Override
    public IPipe getConnectedPipe(Direction side) {
        if (connected.containsKey(side) && getConnectedType(side) == ConnectedType.PIPE) {
            IPipe offset = getHolder().getNeighbourPipe(side);
            if (offset == null && !getHolder().getPipeWorld().isClientSide()) {
                markForUpdate();
            } else {
                return offset;
            }
        }
        return null;
    }

    @Override
    public ConnectedType getConnectedType(Direction side) {
        return types.get(side);
    }

    @Override
    public boolean isConnected(Direction side) {
        return connected.containsKey(side);
    }

    public float getConnectedDist(Direction face) {
        Float custom = connected.get(face);
        return custom == null ? 0 : custom;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("Colour = " + colour);
        left.add("Definition = " + definition.identifier);
        if (behaviour instanceof IDebuggable) {
            left.add("Behaviour:");
            ((IDebuggable) behaviour).getDebugInfo(left, right, side);
            left.add("");
        } else {
            left.add("Behaviour = " + behaviour.getClass());
        }

        if (flow instanceof IDebuggable) {
            left.add("Flow:");
            ((IDebuggable) flow).getDebugInfo(left, right, side);
            left.add("");
        } else {
            left.add("Flow = " + flow.getClass());
        }
        for (Direction face : Direction.values()) {
            right.add(face + " = " + types.get(face) + ", " + getConnectedDist(face));
        }
    }

    public buildcraft.transport.client.model.key.PipeModelKey getModel() {
        PipeFaceTex[] sides = new PipeFaceTex[6];
        float[] mc = new float[6];
        for (Direction face : Direction.values()) {
            int i = face.ordinal();
            sides[i] = behaviour.getTextureData(face);
            mc[i] = getConnectedDist(face);
        }
        return new buildcraft.transport.client.model.key.PipeModelKey(definition, behaviour.getTextureData(null), sides, mc, colour);
    }
}
