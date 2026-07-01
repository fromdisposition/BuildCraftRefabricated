/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.ICustomPipeConnection;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeConnectionAPI;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeEventConnectionChange;
import buildcraft.api.transport.pipe.PipeFaceTex;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.transport.client.model.PipeModelCacheBase;
import buildcraft.transport.wire.SavedDataWireSystems;
import buildcraft.transport.client.model.key.PipeModelKey;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import buildcraft.lib.nbt.BcProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class Pipe implements IPipe, IDebuggable {
   private static final float DEFAULT_CONNECTION_DISTANCE = 0.25F;
   public final IPipeHolder holder;
   public final PipeDefinition definition;
   public final PipeBehaviour behaviour;
   public final PipeFlow flow;
   private DyeColor colour = null;
   private boolean updateMarked = true;
   private PipeModelKey cachedModelKey;
   private PipeModelCacheBase.PipeBaseCutoutKey cachedCutoutKey;
   private final EnumMap<Direction, Float> connected = new EnumMap<>(Direction.class);
   private final EnumMap<Direction, IPipe.ConnectedType> types = new EnumMap<>(Direction.class);

   public Pipe(IPipeHolder holder, PipeDefinition definition) {
      this.holder = holder;
      this.definition = definition;
      this.behaviour = definition.logicConstructor.createBehaviour(this);
      this.flow = definition.flowType.creator.createFlow(this);
   }

   public Pipe(IPipeHolder holder, CompoundTag nbt) throws InvalidInputDataException {
      this.holder = holder;
      String colStr = BcNbt.getString(nbt, "col", "");
      if (!colStr.isEmpty()) {
         this.colour = NBTUtilBC.readEnum(nbt.get("col"), DyeColor.class);
      }

      this.definition = PipeRegistry.INSTANCE.loadDefinition(BcNbt.getString(nbt, "def", ""));
      if (!this.definition.canBeColoured) {
         this.colour = null;
      }

      this.behaviour = this.definition.logicLoader.loadBehaviour(this, BcNbt.getCompound(nbt, "beh"));
      this.flow = this.definition.flowType.loader.loadFlow(this, BcNbt.getCompound(nbt, "flow"));
      int connectionData = BcNbt.getInt(nbt, "con", 0);

      for (Direction face : Direction.values()) {
         int data = connectionData >>> face.ordinal() * 2 & 3;
         if (data == 1) {
            this.connected.put(face, 0.25F);
            this.types.put(face, IPipe.ConnectedType.PIPE);
         } else if (data == 2) {
            this.connected.put(face, 0.25F);
            this.types.put(face, IPipe.ConnectedType.TILE);
         }
      }
   }

   public CompoundTag writeToNbt() {
      CompoundTag nbt = new CompoundTag();
      if (this.colour != null) {
         nbt.put("col", NBTUtilBC.writeEnum(this.colour));
      }

      nbt.putString("def", this.definition.identifier);
      nbt.put("beh", this.behaviour.writeToNbt());
      nbt.put("flow", this.flow.writeToNbt());
      int connectionData = 0;

      for (Direction face : Direction.values()) {
         IPipe.ConnectedType type = this.types.get(face);
         if (type != null) {
            int data = type == IPipe.ConnectedType.PIPE ? 1 : 2;
            connectionData |= data << face.ordinal() * 2;
         }
      }

      nbt.putInt("con", connectionData);
      return nbt;
   }

   public void readFromNbt(CompoundTag nbt) {
      String colStr = BcNbt.getString(nbt, "col", "");
      if (!colStr.isEmpty()) {
         this.colour = NBTUtilBC.readEnum(nbt.get("col"), DyeColor.class);
      } else {
         this.colour = null;
      }

      if (!this.definition.canBeColoured) {
         this.colour = null;
      }

      this.connected.clear();
      this.types.clear();
      int connectionData = BcNbt.getInt(nbt, "con", 0);

      for (Direction face : Direction.values()) {
         int data = connectionData >>> face.ordinal() * 2 & 3;
         if (data == 1) {
            this.connected.put(face, 0.25F);
            this.types.put(face, IPipe.ConnectedType.PIPE);
         } else if (data == 2) {
            this.connected.put(face, 0.25F);
            this.types.put(face, IPipe.ConnectedType.TILE);
         }
      }

      if (nbt.contains("beh")) {
         this.behaviour.readFromNbt(BcNbt.getCompound(nbt, "beh"));
      }

      if (nbt.contains("flow")) {
         this.flow.readFromNbt(BcNbt.getCompound(nbt, "flow"));
      }

      this.invalidateModelKey();
   }

   public void writePayload(FriendlyByteBuf buffer) {
      buffer.writeByte(this.colour == null ? 0 : this.colour.getId() + 1);

      for (Direction face : Direction.values()) {
         Float con = this.connected.get(face);
         if (con != null) {
            buffer.writeBoolean(true);
            buffer.writeFloat(con);
            IPipe.ConnectedType type = this.types.get(face);
            buffer.writeByte(type == null ? -1 : type.ordinal());
         } else {
            buffer.writeBoolean(false);
         }
      }

      this.behaviour.writePayload(buffer);
   }

   public void readPayload(FriendlyByteBuf buffer) {
      this.connected.clear();
      this.types.clear();
      int nColour = buffer.readUnsignedByte();
      this.colour = nColour == 0 ? null : DyeColor.byId(nColour - 1);

      for (Direction face : Direction.values()) {
         if (buffer.readBoolean()) {
            float dist = buffer.readFloat();
            this.connected.put(face, dist);
            int typeOrd = buffer.readByte();
            if (typeOrd >= 0 && typeOrd < IPipe.ConnectedType.values().length) {
               this.types.put(face, IPipe.ConnectedType.values()[typeOrd]);
            }
         }
      }

      this.behaviour.readPayload(buffer, true);
      this.invalidateModelKey();
   }

   @Override
   public IPipeHolder getHolder() {
      return this.holder;
   }

   @Override
   public PipeDefinition getDefinition() {
      return this.definition;
   }

   @Override
   public PipeBehaviour getBehaviour() {
      return this.behaviour;
   }

   @Override
   public PipeFlow getFlow() {
      return this.flow;
   }

   @Override
   public DyeColor getColour() {
      return this.colour;
   }

   @Override
   public void setColour(DyeColor colour) {
      if (this.definition.canBeColoured) {
         this.colour = colour;
         this.invalidateModelKey();
         this.markForUpdate();
         this.holder.scheduleRenderUpdate();
      }
   }

   public void onLoad() {
      this.markForUpdate();
   }

   public boolean hasSimulationWork() {
      return this.updateMarked ? true : this.behaviour.hasSimulationWork() || this.flow.hasSimulationWork();
   }

   public void onTick() {
      ProfilerFiller _profiler = BcProfiler.get();
      if (this.updateMarked) {
         this.updateConnections();
      }

      _profiler.push("buildcraft:pipe_behaviour");

      try {
         this.behaviour.onTick();
      } finally {
         _profiler.pop();
      }

      _profiler.push("buildcraft:pipe_flow");

      try {
         this.flow.onTick();
      } finally {
         _profiler.pop();
      }

      if (this.updateMarked) {
         this.updateConnections();
      }
   }

   public void postPluggableTick() {
      this.flow.postPluggableTick();
   }

   private void updateConnections() {
      ProfilerFiller _profiler = BcProfiler.get();
      _profiler.push("buildcraft:pipe_connections");

      try {
         if (!this.holder.getPipeWorld().isClientSide()) {
            this.updateMarked = false;
            EnumMap<Direction, Float> old = this.connected.clone();
            EnumMap<Direction, IPipe.ConnectedType> oldTypes = this.types.clone();
            this.connected.clear();
            this.types.clear();

            for (Direction facing : Direction.values()) {
               PipePluggable plug = this.getHolder().getPluggable(facing);
               if (plug == null || !plug.isBlocking()) {
                  BlockEntity oTile = this.getHolder().getNeighbourTile(facing);
                  if (oTile != null) {
                     IPipe oPipe = oTile instanceof IPipeHolder oHolder ? oHolder.getPipe() : null;
                     if (oPipe != null) {
                        PipeBehaviour oBehaviour = oPipe.getBehaviour();
                        if (oBehaviour == null) {
                           continue;
                        }

                        PipePluggable oPlug = oPipe.getHolder().getPluggable(facing.getOpposite());
                        if (oPlug == null || !oPlug.isBlocking()) {
                           if (canPipesConnect(facing, this, oPipe)) {
                              this.connected.put(facing, 0.25F);
                              this.types.put(facing, IPipe.ConnectedType.PIPE);
                           }
                           continue;
                        }
                     }

                     BlockPos nPos = this.holder.getPipePos().relative(facing);
                     BlockState neighbour = this.holder.getPipeWorld().getBlockState(nPos);
                     ICustomPipeConnection cust = PipeConnectionAPI.getCustomConnection(neighbour.getBlock());
                     if (cust == null) {
                        cust = DefaultPipeConnection.INSTANCE;
                     }

                     float ext = 0.25F + cust.getExtension(this.holder.getPipeWorld(), nPos, facing.getOpposite(), neighbour);
                     if (this.behaviour.shouldForceConnection(facing, oTile)
                        || this.flow.shouldForceConnection(facing, oTile)
                        || this.behaviour.canConnect(facing, oTile) && this.flow.canConnect(facing, oTile)) {
                        this.connected.put(facing, ext);
                        this.types.put(facing, IPipe.ConnectedType.TILE);
                     }
                  }
               }
            }

            if (old.equals(this.connected) && oldTypes.equals(this.types)) {
               return;
            }

            this.invalidateModelKey();
            boolean connectionsChanged = false;

            for (Direction face : Direction.values()) {
               boolean o = old.containsKey(face);
               boolean n = this.connected.containsKey(face);
               if (o != n || n && oldTypes.get(face) != this.types.get(face)) {
                  connectionsChanged = true;
                  IPipe oPipe = this.getHolder().getNeighbourPipe(face);
                  if (oPipe != null) {
                     oPipe.markForUpdate();
                  }

                  this.holder.fireEvent(new PipeEventConnectionChange(this.holder, face));
               }
            }

            if (connectionsChanged) {
               this.holder.wakePipe();
               this.holder.scheduleRenderUpdate();
               this.refreshWireSystems();
            }

            this.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
            return;
         }
      } finally {
         _profiler.pop();
      }
   }

   private void refreshWireSystems() {
      IWireManager wireManager = this.holder.getWireManager();
      wireManager.updateBetweens(false);
      SavedDataWireSystems wireSystems = SavedDataWireSystems.get(this.holder.getPipeWorld());
      if (wireManager.hasParts()) {
         wireSystems.scheduleWireRebuild(this.holder);
      }

      for (Direction face : Direction.values()) {
         IPipe neighbour = this.holder.getNeighbourPipe(face);
         if (neighbour != null) {
            wireSystems.scheduleWireRebuild(neighbour.getHolder());
         }
      }
   }

   public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
      this.flow.addDrops(toDrop, fortune);
      this.behaviour.addDrops(toDrop, fortune);
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

   public void scheduleConnectionRecheck() {
      this.updateMarked = true;
   }

   @Override
   public void markForUpdate() {
      this.updateMarked = true;
      this.invalidateModelKey();
   }

   public void invalidateModelKey() {
      this.cachedModelKey = null;
      this.cachedCutoutKey = null;
   }

   public PipeModelCacheBase.PipeBaseCutoutKey getCutoutKey() {
      if (this.cachedCutoutKey == null) {
         PipeModelKey model = this.getModel();
         if (model == null) {
            return null;
         }

         this.cachedCutoutKey = new PipeModelCacheBase.PipeBaseCutoutKey(model);
      }

      return this.cachedCutoutKey;
   }

   @Override
   public BlockEntity getConnectedTile(Direction side) {
      if (this.connected.containsKey(side)) {
         BlockEntity offset = this.getHolder().getNeighbourTile(side);
         if (offset != null || this.getHolder().getPipeWorld().isClientSide()) {
            return offset;
         }

         this.markForUpdate();
      }

      return null;
   }

   @Override
   public IPipe getConnectedPipe(Direction side) {
      if (this.connected.containsKey(side) && this.getConnectedType(side) == IPipe.ConnectedType.PIPE) {
         IPipe offset = this.getHolder().getNeighbourPipe(side);
         if (offset != null || this.getHolder().getPipeWorld().isClientSide()) {
            return offset;
         }

         this.markForUpdate();
      }

      return null;
   }

   @Override
   public IPipe.ConnectedType getConnectedType(Direction side) {
      return this.types.get(side);
   }

   @Override
   public boolean isConnected(Direction side) {
      return this.connected.containsKey(side);
   }

   public float getConnectedDist(Direction face) {
      Float custom = this.connected.get(face);
      return custom == null ? 0.0F : custom;
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("Colour = " + this.colour);
      left.add("Definition = " + this.definition.identifier);
      if (this.behaviour instanceof IDebuggable) {
         left.add("Behaviour:");
         ((IDebuggable)this.behaviour).getDebugInfo(left, right, side);
         left.add("");
      } else {
         left.add("Behaviour = " + this.behaviour.getClass());
      }

      if (this.flow instanceof IDebuggable) {
         left.add("Flow:");
         ((IDebuggable)this.flow).getDebugInfo(left, right, side);
         left.add("");
      } else {
         left.add("Flow = " + this.flow.getClass());
      }

      for (Direction face : Direction.values()) {
         right.add(face + " = " + this.types.get(face) + ", " + this.getConnectedDist(face));
      }
   }

   public PipeModelKey getModel() {
      if (this.cachedModelKey == null) {
         PipeFaceTex[] sides = new PipeFaceTex[6];
         float[] mc = new float[6];

         for (Direction face : Direction.values()) {
            int i = face.ordinal();
            sides[i] = this.behaviour.getTextureData(face);
            mc[i] = this.getConnectedDist(face);
         }

         this.cachedModelKey = new PipeModelKey(this.definition, this.behaviour.getTextureData(null), sides, mc, this.colour);
      }

      return this.cachedModelKey;
   }
}
