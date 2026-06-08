/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.WireNode;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.misc.PositionUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class WireSystem {
   public final ImmutableList<WireSystem.WireElement> elements;
   public final DyeColor color;
   public final boolean hasEmitters;
   private final transient int cachedHashCode;
   private final transient int cachedWiresHashCode;

   public boolean hasElement(WireSystem.WireElement element) {
      return this.elements.contains(element);
   }

   public static boolean canWireConnect(IPipeHolder holder, Direction side) {
      IPipe pipe = holder.getPipe();
      if (pipe == null) {
         return false;
      } else {
         IPipe oPipe = holder.getNeighbourPipe(side);
         if (oPipe == null) {
            return false;
         } else if (pipe.isConnected(side)) {
            return true;
         } else if ((holder.getPluggable(side) == null || !holder.getPluggable(side).isBlocking())
            && (oPipe.getHolder().getPluggable(side.getOpposite()) == null || !oPipe.getHolder().getPluggable(side.getOpposite()).isBlocking())) {
            return pipe.getDefinition().flowType != PipeApi.flowStructure && oPipe.getDefinition().flowType != PipeApi.flowStructure
               ? false
               : pipe.getColour() == null || oPipe.getColour() == null || pipe.getColour() == oPipe.getColour();
         } else {
            return false;
         }
      }
   }

   public static List<WireSystem.WireElement> getConnectedElementsOfElement(IPipeHolder holder, WireSystem.WireElement element) {
      assert element.wirePart != null;
      WireNode node = new WireNode(element.blockPos, element.wirePart);
      List<WireSystem.WireElement> list = new ArrayList<>();

      for (Direction face : Direction.values()) {
         WireNode oNode = node.offset(face);
         if (oNode.pos == node.pos || canWireConnect(holder, face)) {
            list.add(new WireSystem.WireElement(oNode.pos, oNode.part));
         }
      }

      return list;
   }

   public static List<WireSystem.WireElement> getConnectedElementsOfElement(Level world, WireSystem.WireElement element) {
      return element.type == WireSystem.WireElement.Type.WIRE_PART && world.getBlockEntity(element.blockPos) instanceof IPipeHolder holder
         ? getConnectedElementsOfElement(holder, element)
         : Collections.emptyList();
   }

   public WireSystem(ImmutableList<WireSystem.WireElement> elements, DyeColor color) {
      this.elements = Objects.requireNonNull(elements, "elements");
      this.color = color;
      this.hasEmitters = computeHasEmitters(this.elements);
      this.cachedHashCode = this.computeHashCode();
      this.cachedWiresHashCode = this.computeCachedWiresHashCode();
   }

   public WireSystem(SavedDataWireSystems wireSystems, WireSystem.WireElement startElement) {
      Map<BlockPos, IPipeHolder> holdersCache = new HashMap<>();
      Set<WireSystem.WireElement> walked = new HashSet<>();
      Queue<WireSystem.WireElement> queue = new ArrayDeque<>();
      queue.add(startElement);
      DyeColor tempColor = null;
      Builder<WireSystem.WireElement> elementBuilder = ImmutableList.builder();

      while (!queue.isEmpty()) {
         WireSystem.WireElement element = queue.remove();
         if (!walked.contains(element)) {
            if (!holdersCache.containsKey(element.blockPos)) {
               BlockEntity tile = wireSystems.world.getBlockEntity(element.blockPos);
               IPipeHolder holder = null;
               if (tile instanceof IPipeHolder) {
                  holder = (IPipeHolder)tile;
               }

               holdersCache.put(element.blockPos, holder);
            }

            IPipeHolder holder = holdersCache.get(element.blockPos);
            if (holder != null) {
               if (element.type == WireSystem.WireElement.Type.WIRE_PART) {
                  DyeColor colorOfPart = holder.getWireManager().getColorOfPart(element.wirePart);
                  if (tempColor == null && colorOfPart != null) {
                     tempColor = colorOfPart;
                  }

                  if (tempColor != null && colorOfPart == tempColor) {
                     DyeColor colorButFinal = tempColor;
                     wireSystems.getWireSystemsWithElement(element)
                        .stream()
                        .filter(wireSystem -> wireSystem != this && wireSystem.color == colorButFinal)
                        .forEach(wireSystems::removeWireSystem);
                     elementBuilder.add(element);
                     queue.addAll(getConnectedElementsOfElement(wireSystems.world, element));
                     Arrays.stream(Direction.values()).forEach(side -> queue.add(new WireSystem.WireElement(element.blockPos, side)));
                  }
               } else if (element.type == WireSystem.WireElement.Type.EMITTER_SIDE && holder.getPluggable(element.emitterSide) instanceof IWireEmitter) {
                  elementBuilder.add(new WireSystem.WireElement(element.blockPos, element.emitterSide));
               }
            }

            walked.add(element);
         }
      }

      this.elements = elementBuilder.build();
      this.color = tempColor;
      this.hasEmitters = computeHasEmitters(this.elements);
      this.cachedHashCode = this.computeHashCode();
      this.cachedWiresHashCode = this.computeCachedWiresHashCode();
   }

   private static boolean computeHasEmitters(ImmutableList<WireSystem.WireElement> wireElements) {
      UnmodifiableIterator var1 = wireElements.iterator();

      while (var1.hasNext()) {
         WireSystem.WireElement element = (WireSystem.WireElement)var1.next();
         if (element.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            return true;
         }
      }

      return false;
   }

   public boolean isEmpty() {
      return this.elements.stream().noneMatch(element -> element.type == WireSystem.WireElement.Type.WIRE_PART);
   }

   public boolean update(SavedDataWireSystems wireSystems) {
      if (!this.hasEmitters) {
         return false;
      }

      UnmodifiableIterator var2 = this.elements.iterator();

      while (var2.hasNext()) {
         WireSystem.WireElement element = (WireSystem.WireElement)var2.next();
         if (element.type == WireSystem.WireElement.Type.EMITTER_SIDE && wireSystems.isEmitterEmitting(element, this.color)) {
            return true;
         }
      }

      return false;
   }

   public List<ChunkPos> getChunkPoses() {
      return this.getChunkPosesAsStream().collect(Collectors.toList());
   }

   public Stream<ChunkPos> getChunkPosesAsStream() {
      return this.elements.stream().map(element -> PositionUtil.chunkContaining(element.blockPos));
   }

   public boolean isPlayerWatching(ServerPlayer player) {
      Level level = player.level();
      return level instanceof ServerLevel serverLevel
         ? this.getChunkPosesAsStream().anyMatch(chunkPos -> serverLevel.isPositionEntityTicking(chunkPos.getWorldPosition()))
         : false;
   }

   public int getWiresHashCode() {
      return this.cachedWiresHashCode;
   }

   public int getNetworkId() {
      return this.cachedHashCode;
   }

   private int computeCachedWiresHashCode() {
      return this.elements
         .stream()
         .filter(element -> element.type == WireSystem.WireElement.Type.WIRE_PART)
         .mapToInt(WireSystem.WireElement::hashCode)
         .reduce(1, (hashCode, elementHashCode) -> hashCode * 31 + elementHashCode);
   }

   public CompoundTag writeToNBT() {
      CompoundTag nbt = new CompoundTag();
      ListTag elementsList = new ListTag();
      this.elements.stream().map(WireSystem.WireElement::writeToNBT).forEach(elementsList::add);
      nbt.put("elements", elementsList);
      if (this.color != null) {
         nbt.putInt("color", this.color.getId());
      }

      return nbt;
   }

   public WireSystem(CompoundTag nbt) {
      ListTag elementsList = nbt.getListOrEmpty("elements");
      this.elements = IntStream.range(0, elementsList.size())
         .mapToObj(i -> elementsList.get(i) instanceof CompoundTag ct ? ct : new CompoundTag())
         .map(WireSystem.WireElement::new)
         .collect(ImmutableList.toImmutableList());
      this.color = DyeColor.byId(nbt.getIntOr("color", 0));
      this.hasEmitters = computeHasEmitters(this.elements);
      this.cachedHashCode = this.computeHashCode();
      this.cachedWiresHashCode = this.computeCachedWiresHashCode();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (o != null && this.getClass() == o.getClass()) {
         WireSystem that = (WireSystem)o;
         if (this.cachedHashCode != that.cachedHashCode) {
            return false;
         } else {
            return !this.elements.equals(that.elements) ? false : this.color == that.color;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.cachedHashCode;
   }

   private int computeHashCode() {
      int result = this.elements.hashCode();
      return 31 * result + (this.color != null ? this.color.hashCode() : 0);
   }

   public static class WireElement {
      public final WireSystem.WireElement.Type type;
      public final BlockPos blockPos;
      public final EnumWirePart wirePart;
      public final Direction emitterSide;

      public WireElement(BlockPos blockPos, EnumWirePart wirePart) {
         this.type = WireSystem.WireElement.Type.WIRE_PART;
         this.blockPos = blockPos;
         this.wirePart = wirePart;
         this.emitterSide = null;
      }

      public WireElement(BlockPos blockPos, Direction emitterSide) {
         this.type = WireSystem.WireElement.Type.EMITTER_SIDE;
         this.blockPos = blockPos;
         this.wirePart = null;
         this.emitterSide = emitterSide;
      }

      public WireElement(FriendlyByteBuf buf) {
         this.type = WireSystem.WireElement.Type.values()[buf.readInt()];
         this.blockPos = buf.readBlockPos();
         if (this.type == WireSystem.WireElement.Type.WIRE_PART) {
            this.wirePart = EnumWirePart.VALUES[buf.readInt()];
            this.emitterSide = null;
         } else if (this.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            this.wirePart = null;
            this.emitterSide = Direction.from3DDataValue(buf.readInt());
         } else {
            this.wirePart = null;
            this.emitterSide = null;
         }
      }

      public WireElement(CompoundTag nbt) {
         this.type = WireSystem.WireElement.Type.values()[nbt.getIntOr("type", 0)];
         int bpX = nbt.getIntOr("bpX", 0);
         int bpY = nbt.getIntOr("bpY", 0);
         int bpZ = nbt.getIntOr("bpZ", 0);
         this.blockPos = new BlockPos(bpX, bpY, bpZ);
         if (this.type == WireSystem.WireElement.Type.WIRE_PART) {
            this.wirePart = EnumWirePart.VALUES[nbt.getIntOr("wirePart", 0)];
            this.emitterSide = null;
         } else if (this.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            this.wirePart = null;
            this.emitterSide = Direction.from3DDataValue(nbt.getIntOr("emitterSide", 0));
         } else {
            this.wirePart = null;
            this.emitterSide = null;
         }
      }

      public void toBytes(FriendlyByteBuf buf) {
         buf.writeInt(this.type.ordinal());
         buf.writeBlockPos(this.blockPos);
         if (this.type == WireSystem.WireElement.Type.WIRE_PART) {
            assert this.wirePart != null;
            buf.writeInt(this.wirePart.ordinal());
         } else if (this.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            assert this.emitterSide != null;
            buf.writeInt(this.emitterSide.get3DDataValue());
         }
      }

      public CompoundTag writeToNBT() {
         CompoundTag nbt = new CompoundTag();
         nbt.putInt("type", this.type.ordinal());
         nbt.putInt("bpX", this.blockPos.getX());
         nbt.putInt("bpY", this.blockPos.getY());
         nbt.putInt("bpZ", this.blockPos.getZ());
         if (this.type == WireSystem.WireElement.Type.WIRE_PART) {
            assert this.wirePart != null;
            nbt.putInt("wirePart", this.wirePart.ordinal());
         } else if (this.type == WireSystem.WireElement.Type.EMITTER_SIDE) {
            assert this.emitterSide != null;
            nbt.putInt("emitterSide", this.emitterSide.get3DDataValue());
         }

         return nbt;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }

         if (o != null && this.getClass() == o.getClass()) {
            WireSystem.WireElement element = (WireSystem.WireElement)o;
            if (this.type != element.type) {
               return false;
            } else if (!this.blockPos.equals(element.blockPos)) {
               return false;
            } else {
               return this.wirePart != element.wirePart ? false : this.emitterSide == element.emitterSide;
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.type.hashCode();
         result = 31 * result + this.blockPos.hashCode();
         result = 31 * result + (this.wirePart != null ? this.wirePart.hashCode() : 0);
         return 31 * result + (this.emitterSide != null ? this.emitterSide.hashCode() : 0);
      }

      @Override
      public String toString() {
         return "Element{type=" + this.type + ", blockPos=" + this.blockPos + ", wirePart=" + this.wirePart + ", emitterSide=" + this.emitterSide + "}";
      }

      public enum Type {
         WIRE_PART,
         EMITTER_SIDE;
      }
   }
}
