/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.ChunkPos;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;

public final class WireSystem {
    public final ImmutableList<WireElement> elements;
    public final DyeColor color;

    private final transient int cachedHashCode;
    private final transient int cachedWiresHashCode;

    public boolean hasElement(WireElement element) {
        return elements.contains(element);
    }

    public static boolean canWireConnect(IPipeHolder holder, Direction side) {
        IPipe pipe = holder.getPipe();
        if (pipe == null) {
            return false;
        }
        IPipe oPipe = holder.getNeighbourPipe(side);
        if (oPipe == null) {
            return false;
        }
        if (pipe.isConnected(side)) {
            return true;
        }
        if ((holder.getPluggable(side) != null && holder.getPluggable(side).isBlocking())
            || (oPipe.getHolder().getPluggable(side.getOpposite()) != null && oPipe.getHolder().getPluggable(side.getOpposite()).isBlocking())) {
            return false;
        }
        if (pipe.getDefinition().flowType == PipeApi.flowStructure || oPipe.getDefinition().flowType == PipeApi.flowStructure) {
            return pipe.getColour() == null || oPipe.getColour() == null || pipe.getColour() == oPipe.getColour();
        }
        return false;
    }

    public static List<WireElement> getConnectedElementsOfElement(IPipeHolder holder, WireElement element) {
        assert element.wirePart != null;
        WireNode node = new WireNode(element.blockPos, element.wirePart);

        List<WireElement> list = new ArrayList<>();
        for (Direction face : Direction.values()) {
            WireNode oNode = node.offset(face);

            if (oNode.pos == node.pos || canWireConnect(holder, face)) {
                list.add(new WireElement(oNode.pos, oNode.part));
            }
        }
        return list;
    }

    public static List<WireElement> getConnectedElementsOfElement(Level world, WireElement element) {
        if (element.type == WireElement.Type.WIRE_PART) {
            BlockEntity tile = world.getBlockEntity(element.blockPos);
            if (tile instanceof IPipeHolder holder) {
                return getConnectedElementsOfElement(holder, element);
            }
        }
        return Collections.emptyList();
    }

    public WireSystem(ImmutableList<WireElement> elements, DyeColor color) {
        this.elements = Objects.requireNonNull(elements, "elements");
        this.color = color;

        this.cachedHashCode = this.computeHashCode();
        this.cachedWiresHashCode = this.computeCachedWiresHashCode();
    }

    public WireSystem(SavedDataWireSystems wireSystems, WireElement startElement) {
        Map<BlockPos, IPipeHolder> holdersCache = new HashMap<>();
        Set<WireElement> walked = new HashSet<>();

        Queue<WireElement> queue = new ArrayDeque<>();
        queue.add(startElement);

        DyeColor tempColor = null;
        ImmutableList.Builder<WireElement> elementBuilder = ImmutableList.builder();

        while (!queue.isEmpty()) {
            WireElement element = queue.remove();

            if (!walked.contains(element)) {
                if (!holdersCache.containsKey(element.blockPos)) {
                    BlockEntity tile = wireSystems.world.getBlockEntity(element.blockPos);
                    IPipeHolder holder = null;
                    if (tile instanceof IPipeHolder) {
                        holder = (IPipeHolder) tile;
                    }
                    holdersCache.put(element.blockPos, holder);
                }
                IPipeHolder holder = holdersCache.get(element.blockPos);
                if (holder != null) {
                    if (element.type == WireElement.Type.WIRE_PART) {
                        DyeColor colorOfPart = holder.getWireManager().getColorOfPart(element.wirePart);
                        if (tempColor == null) {
                            if (colorOfPart != null) {
                                tempColor = colorOfPart;
                            }
                        }
                        if (tempColor != null && colorOfPart == tempColor) {
                            DyeColor colorButFinal = tempColor;
                            wireSystems.getWireSystemsWithElement(element).stream()
                                .filter(wireSystem -> wireSystem != this && wireSystem.color == colorButFinal)
                                .forEach(wireSystems::removeWireSystem);
                            elementBuilder.add(element);
                            queue.addAll(getConnectedElementsOfElement(wireSystems.world, element));
                            Arrays.stream(Direction.values()).forEach(side -> queue.add(new WireElement(element.blockPos, side)));
                        }
                    } else if (element.type == WireElement.Type.EMITTER_SIDE) {
                        if (holder.getPluggable(element.emitterSide) instanceof IWireEmitter) {
                            elementBuilder.add(new WireElement(element.blockPos, element.emitterSide));
                        }
                    }
                }
                walked.add(element);
            }
        }

        this.elements = elementBuilder.build();
        this.color = tempColor;

        this.cachedHashCode = this.computeHashCode();
        this.cachedWiresHashCode = this.computeCachedWiresHashCode();
    }

    public boolean isEmpty() {
        return elements.stream().noneMatch(element -> element.type == WireElement.Type.WIRE_PART);
    }

    public boolean update(SavedDataWireSystems wireSystems) {
        return elements.stream()
            .filter(element -> element.type == WireElement.Type.EMITTER_SIDE)
            .map(element -> wireSystems.isEmitterEmitting(element, color))
            .reduce(Boolean::logicalOr).orElse(false);
    }

    public List<ChunkPos> getChunkPoses() {
        return this.getChunkPosesAsStream().collect(Collectors.toList());
    }

    public Stream<ChunkPos> getChunkPosesAsStream() {
        return elements.stream().map(element -> buildcraft.lib.misc.PositionUtil.chunkContaining(element.blockPos));
    }

    public boolean isPlayerWatching(ServerPlayer player) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            return getChunkPosesAsStream().anyMatch(chunkPos ->
                serverLevel.isPositionEntityTicking(chunkPos.getWorldPosition()));
        }
        return false;
    }

    public int getWiresHashCode() {
        return this.cachedWiresHashCode;
    }

    private int computeCachedWiresHashCode() {
        return elements.stream().filter(element -> element.type == WireElement.Type.WIRE_PART)
                .mapToInt(WireElement::hashCode).reduce(1, (hashCode, elementHashCode) -> hashCode * 31 + elementHashCode);
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag elementsList = new ListTag();
        elements.stream().map(WireElement::writeToNBT).forEach(elementsList::add);
        nbt.put("elements", elementsList);
        if (color != null) {
            nbt.putInt("color", color.getId());
        }
        return nbt;
    }

    public WireSystem(CompoundTag nbt) {
        ListTag elementsList = nbt.getListOrEmpty("elements");

        elements = IntStream.range(0, elementsList.size())
            .mapToObj(i -> {
                Tag tag = elementsList.get(i);
                return tag instanceof CompoundTag ct ? ct : new CompoundTag();
            })
            .map(WireElement::new)
            .collect(ImmutableList.toImmutableList());
        color = DyeColor.byId(nbt.getIntOr("color", 0));

        this.cachedHashCode = this.computeHashCode();
        this.cachedWiresHashCode = this.computeCachedWiresHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WireSystem that = (WireSystem) o;

        if (this.cachedHashCode != that.cachedHashCode) {
            return false;
        }

        if (!elements.equals(that.elements)) {
            return false;
        }
        return color == that.color;
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    private int computeHashCode() {
        int result = elements.hashCode();
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    public static class WireElement {
        public final Type type;
        public final BlockPos blockPos;
        public final EnumWirePart wirePart;
        public final Direction emitterSide;

        public WireElement(BlockPos blockPos, EnumWirePart wirePart) {
            this.type = Type.WIRE_PART;
            this.blockPos = blockPos;
            this.wirePart = wirePart;
            this.emitterSide = null;
        }

        public WireElement(BlockPos blockPos, Direction emitterSide) {
            this.type = Type.EMITTER_SIDE;
            this.blockPos = blockPos;
            this.wirePart = null;
            this.emitterSide = emitterSide;
        }

        public WireElement(FriendlyByteBuf buf) {
            type = Type.values()[buf.readInt()];
            blockPos = buf.readBlockPos();
            if (type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[buf.readInt()];
                this.emitterSide = null;
            } else if (type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = Direction.from3DDataValue(buf.readInt());
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public WireElement(CompoundTag nbt) {
            type = Type.values()[nbt.getIntOr("type", 0)];
            int bpX = nbt.getIntOr("bpX", 0);
            int bpY = nbt.getIntOr("bpY", 0);
            int bpZ = nbt.getIntOr("bpZ", 0);
            blockPos = new BlockPos(bpX, bpY, bpZ);
            if (type == Type.WIRE_PART) {
                wirePart = EnumWirePart.VALUES[nbt.getIntOr("wirePart", 0)];
                this.emitterSide = null;
            } else if (type == Type.EMITTER_SIDE) {
                this.wirePart = null;
                emitterSide = Direction.from3DDataValue(nbt.getIntOr("emitterSide", 0));
            } else {
                this.wirePart = null;
                this.emitterSide = null;
            }
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeInt(type.ordinal());
            buf.writeBlockPos(blockPos);
            if (type == Type.WIRE_PART) {
                assert wirePart != null;
                buf.writeInt(wirePart.ordinal());
            } else if (type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                buf.writeInt(emitterSide.get3DDataValue());
            }
        }

        public CompoundTag writeToNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("type", type.ordinal());
            nbt.putInt("bpX", blockPos.getX());
            nbt.putInt("bpY", blockPos.getY());
            nbt.putInt("bpZ", blockPos.getZ());
            if (type == Type.WIRE_PART) {
                assert wirePart != null;
                nbt.putInt("wirePart", wirePart.ordinal());
            } else if (type == Type.EMITTER_SIDE) {
                assert emitterSide != null;
                nbt.putInt("emitterSide", emitterSide.get3DDataValue());
            }
            return nbt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            WireElement element = (WireElement) o;

            if (type != element.type) {
                return false;
            }
            if (!blockPos.equals(element.blockPos)) {
                return false;
            }
            if (wirePart != element.wirePart) {
                return false;
            }
            return emitterSide == element.emitterSide;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + blockPos.hashCode();
            result = 31 * result + (wirePart != null ? wirePart.hashCode() : 0);
            result = 31 * result + (emitterSide != null ? emitterSide.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Element{" + "type=" + type + ", blockPos=" + blockPos + ", wirePart=" + wirePart + ", emitterSide=" + emitterSide + '}';
        }

        public enum Type {
            WIRE_PART,
            EMITTER_SIDE
        }
    }
}
