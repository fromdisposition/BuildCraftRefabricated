package buildcraft.transport.tile;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.model.data.ModelData;
import buildcraft.lib.model.data.ModelProperty;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.net.BcEnvelopeCodec;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.IBlockEntityLoadHook;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.net.MessagePipePayload;
import buildcraft.transport.net.PipePayloadMessageQueue;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.pipe.PipeEventBus;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDaizuli;
import buildcraft.transport.pipe.flow.PipeFlowInternalAccess;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraft.transport.wire.SavedDataWireSystems;
import buildcraft.transport.wire.WireManager;
import com.mojang.authlib.GameProfile;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import team.reborn.energy.api.EnergyStorage;

public class TilePipeHolder extends BlockEntity implements IPipeHolder, IDebuggable, IBlockEntityLoadHook {
   private static final Identifier ADVANCEMENT_PIPE_DREAM = Identifier.parse("buildcrafttransport:pipe_dream");
   private static final Identifier ADVANCEMENT_PIPE_DIVERSIFICATION = Identifier.parse("buildcrafttransport:pipe_diversification");
   private static final Identifier ADVANCEMENT_PIPE_FANATIC = Identifier.parse("buildcrafttransport:pipe_fanatic");
   private static final Identifier ADVANCEMENT_CATEGORIZING_WITH_COLORS = Identifier.parse("buildcrafttransport:categorizing_with_colors");
   @Deprecated
   public static final ModelProperty<TilePipeHolder> PIPE_MODEL_DATA = ModelProperty.create();
   public final PipeEventBus eventBus = new PipeEventBus();
   private Pipe pipe;
   private GameProfile owner;
   public final WireManager wireManager = new WireManager(this);
   private final PipePluggable[] pluggables = new PipePluggable[6];
   private boolean scheduleRenderUpdate = true;
   private final Set<IPipeHolder.PipeMessageReceiver> networkUpdates = EnumSet.noneOf(IPipeHolder.PipeMessageReceiver.class);
   private final Set<IPipeHolder.PipeMessageReceiver> networkGuiUpdates = EnumSet.noneOf(IPipeHolder.PipeMessageReceiver.class);
   private final Set<ServerPlayer> guiViewers = new HashSet<>();
   private int wakeTicks = 0;
   private boolean saveDirtyThisTick = false;
   private final int[] redstoneOutputs = new int[Direction.values().length];
   private final int[] redstoneOutputsThisTick = new int[Direction.values().length];

   public TilePipeHolder(BlockPos pos, BlockState state) {
      super(BCTransportBlockEntities.PIPE_HOLDER, pos, state);
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      if (this.owner != null && this.owner.id() != null) {
         output.putString("ownerUUID", this.owner.id().toString());
         if (this.owner.name() != null) {
            output.putString("ownerName", this.owner.name());
         }
      }

      if (this.pipe != null) {
         output.store("pipe", CompoundTag.CODEC, this.pipe.writeToNbt());
      }

      CompoundTag wireTag = this.wireManager.writeToNbt();
      if (!wireTag.isEmpty()) {
         output.store("wires", CompoundTag.CODEC, wireTag);
      }

      CompoundTag plugTag = new CompoundTag();

      for (Direction face : Direction.values()) {
         PipePluggable plug = this.pluggables[face.ordinal()];
         if (plug != null) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", plug.definition.identifier.toString());
            entry.put("data", plug.writeToNbt());
            plugTag.put(face.getName(), entry);
         }
      }

      if (!plugTag.isEmpty()) {
         output.store("plugs", CompoundTag.CODEC, plugTag);
      }
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      String ownerUuid = input.getStringOr("ownerUUID", "");
      if (!ownerUuid.isEmpty()) {
         try {
            this.owner = new GameProfile(UUID.fromString(ownerUuid), input.getStringOr("ownerName", "Unknown"));
         } catch (IllegalArgumentException e) {
            this.owner = null;
         }
      }

      input.read("pipe", CompoundTag.CODEC).ifPresent(pipeTag -> {
         try {
            if (this.pipe != null) {
               this.pipe.readFromNbt(pipeTag);
            } else {
               this.pipe = new Pipe(this, pipeTag);
               this.eventBus.registerHandler(this.pipe.behaviour);
               this.eventBus.registerHandler(this.pipe.flow);
            }
         } catch (InvalidInputDataException e) {
            this.pipe = null;
         }
      });
      input.read("plugs", CompoundTag.CODEC).ifPresentOrElse(plugTag -> {
         for (Direction face : Direction.values()) {
            if (plugTag.contains(face.getName())) {
               CompoundTag entry = plugTag.getCompound(face.getName()).orElse(new CompoundTag());
               String id = entry.getString("id").orElse("");
               if (!id.isEmpty()) {
                  Identifier plugId = Identifier.parse(id);
                  PluggableDefinition def = PipeApi.pluggableRegistry != null ? PipeApi.pluggableRegistry.getDefinition(plugId) : null;
                  if (def != null) {
                     CompoundTag data = entry.getCompound("data").orElse(new CompoundTag());
                     PipePluggable existing = this.pluggables[face.ordinal()];
                     if (existing == null || !existing.definition.identifier.equals(plugId) || !existing.readFromNbt(data)) {
                        try {
                           this.pluggables[face.ordinal()] = def.readFromNbt(this, face, data);
                        } catch (RuntimeException e) {
                           BCLog.logger.warn("[transport.pipe] Failed to load pluggable {} at {} {}", plugId, this.getBlockPos(), face, e);
                           this.pluggables[face.ordinal()] = null;
                        }
                     }
                  } else {
                     this.pluggables[face.ordinal()] = null;
                  }
               } else {
                  this.pluggables[face.ordinal()] = null;
               }
            } else {
               this.pluggables[face.ordinal()] = null;
            }
         }
      }, () -> {
         for (int i = 0; i < this.pluggables.length; i++) {
            this.pluggables[i] = null;
         }
      });

      for (PipePluggable plug : this.pluggables) {
         this.eventBus.unregisterHandler(plug);
         this.eventBus.registerHandler(plug);
      }

      input.read("wires", CompoundTag.CODEC).ifPresent(wireTag -> this.wireManager.readFromNbt(wireTag));
      if (this.level != null && this.level.isClientSide()) {
         this.refreshClientModel();
         this.scheduleRenderUpdate = true;
      }
   }

   @Override
   public void onLoad() {
      if (this.pipe != null) {
         this.pipe.onLoad();
      }

      this.refreshClientModel();
      this.scheduleRenderUpdate = true;
   }

   private void refreshClientModel() {
      if (this.level != null && this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
      }
   }

   public CompoundTag getUpdateTag(Provider registries) {
      CompoundTag tag = this.saveCustomOnly(registries);
      CompoundTag plugsClient = new CompoundTag();

      for (Direction face : Direction.values()) {
         PipePluggable plug = this.pluggables[face.ordinal()];
         if (plug != null) {
            CompoundTag data = plug.writeClientUpdateData();
            if (!data.isEmpty()) {
               plugsClient.put(face.getName(), data);
            }
         }
      }

      if (!plugsClient.isEmpty()) {
         tag.put("plugsClient", plugsClient);
      }

      return tag;
   }

   public void handleUpdateTag(ValueInput input) {
      this.applyClientUpdateData(input);
      this.refreshClientModel();
   }

   public void onDataPacket(Connection net, ValueInput input) {
      this.applyClientUpdateData(input);
      this.refreshClientModel();
   }

   private void applyClientUpdateData(ValueInput input) {
      input.read("plugsClient", CompoundTag.CODEC).ifPresent(plugsClient -> {
         for (Direction face : Direction.values()) {
            PipePluggable plug = this.pluggables[face.ordinal()];
            if (plug != null && plugsClient.contains(face.getName())) {
               plug.readClientUpdateData(plugsClient.getCompound(face.getName()).orElse(new CompoundTag()));
            }
         }
      });
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
      Item item = stack.getItem();
      if (item instanceof IItemPipe) {
         PipeDefinition definition = ((IItemPipe)item).getDefinition();
         this.pipe = new Pipe(this, definition);
         this.eventBus.registerHandler(this.pipe.behaviour);
         this.eventBus.registerHandler(this.pipe.flow);
         DyeColor col = (DyeColor)stack.get(BCTransportItems.PIPE_COLOUR);
         if (col != null) {
            this.pipe.setColour(col);
         }
      }

      if (placer instanceof Player player && this.level != null && !this.level.isClientSide()) {
         this.owner = player.getGameProfile();
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PIPE_DREAM);
         if (this.pipe != null) {
            PipeDefinition def = this.pipe.getDefinition();
            String flowCriterion = getFlowTypeCriterion(def);
            if (flowCriterion != null) {
               AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PIPE_DIVERSIFICATION, flowCriterion);
            }

            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PIPE_FANATIC, def.identifier);
            if (this.pipe.behaviour instanceof PipeBehaviourDaizuli) {
               AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_CATEGORIZING_WITH_COLORS);
            }
         }
      }

      if (this.pipe != null && this.level != null && !this.level.isClientSide()) {
         this.pipe.scheduleConnectionRecheck();
         this.wakePipe();
      }

      this.scheduleRenderUpdate();
      this.setChanged();
   }

   private static String getFlowTypeCriterion(PipeDefinition def) {
      if (def.flowType == PipeApi.flowItems) {
         return "item_pipe";
      } else if (def.flowType == PipeApi.flowFluids) {
         return "fluid_pipe";
      } else if (def.flowType == PipeApi.flowPower) {
         return "power_pipe";
      } else {
         return def.flowType == PipeApi.flowStructure ? "structure_pipe" : null;
      }
   }

   @Override
   public void wakePipe() {
      this.wakeTicks = Math.max(this.wakeTicks, 2);
   }

   public void markPipeSaveDirty() {
      this.saveDirtyThisTick = true;
   }

   private boolean needsClientTick() {
      if (this.scheduleRenderUpdate) {
         return true;
      } else if (!this.wireManager.parts.isEmpty() && !this.wireManager.initialised) {
         return true;
      } else {
         return this.pipe == null ? false : this.pipe.getFlow().hasClientSimulationWork();
      }
   }

   private boolean needsHeavySimulation() {
      if (this.wakeTicks > 0) {
         return true;
      }

      if (!this.wireManager.initialised) {
         return true;
      }

      for (PipePluggable plug : this.pluggables) {
         if (plug != null && plug.needsTick()) {
            return true;
         }
      }

      return this.pipe != null && this.pipe.hasSimulationWork();
   }

   private boolean needsServerTick() {
      return this.needsHeavySimulation() || this.scheduleRenderUpdate || !this.networkUpdates.isEmpty() || !this.networkGuiUpdates.isEmpty();
   }

   public void tick() {
      ProfilerFiller _profiler = Profiler.get();
      _profiler.push("buildcraft:pipe_tick");

      try {
         if (this.level != null) {
            if (this.level.isClientSide()) {
               if (!this.needsClientTick()) {
                  return;
               }
            } else if (!this.needsServerTick()) {
               return;
            }
         }

         Arrays.fill(this.redstoneOutputsThisTick, 0);
         boolean simulate = this.level == null || this.level.isClientSide() || this.needsHeavySimulation();
         boolean redstoneChanged = false;
         if (simulate) {
            this.wireManager.tick();
            if (this.pipe != null) {
               this.pipe.onTick();
            }

            for (PipePluggable plug : this.pluggables) {
               if (plug != null) {
                  plug.onTick();
               }
            }

            if (this.pipe != null) {
               this.pipe.postPluggableTick();
            }
         }

         for (int i = 0; i < 6; i++) {
            if (this.redstoneOutputs[i] != this.redstoneOutputsThisTick[i]) {
               this.redstoneOutputs[i] = this.redstoneOutputsThisTick[i];
               redstoneChanged = true;
            }
         }

         if (redstoneChanged && this.level != null && !this.level.isClientSide()) {
            for (PipePluggable plug : this.pluggables) {
               if (plug instanceof PluggableGate gate) {
                  gate.logic.markResolveDirty();
               }
            }

            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
         }

         if (this.scheduleRenderUpdate && this.level != null && !this.level.isClientSide()) {
            this.scheduleRenderUpdate = false;
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
         }

         if (this.level != null && !this.level.isClientSide()) {
            this.flushScheduledNetworkUpdates();
            if (this.saveDirtyThisTick || redstoneChanged) {
               this.setChanged();
               this.saveDirtyThisTick = false;
            }
         }

         if (this.wakeTicks > 0) {
            this.wakeTicks--;
         }
      } finally {
         _profiler.pop();
      }
   }

   private void flushScheduledNetworkUpdates() {
      PipeHolderNetworkFlush.flush(
         this.networkUpdates,
         this.networkGuiUpdates,
         parts -> this.sendScheduledPayloads(parts, this::sendTrackingPayload),
         parts -> this.sendScheduledPayloads(parts, this::enqueueGuiPayload)
      );
   }

   private void sendScheduledPayloads(Set<IPipeHolder.PipeMessageReceiver> parts, Consumer<MessagePipePayload> sender) {
      if (!parts.isEmpty()) {
         if (parts.size() == 1) {
            IPipeHolder.PipeMessageReceiver part = parts.iterator().next();
            this.dispatchPayload(part, sender);
         } else {
            byte[] data = this.buildMultiPayload(parts);
            if (data != null) {
               sender.accept(new MessagePipePayload(this.worldPosition, MessagePipePayload.MULTI_RECEIVER_ORDINAL, data));
            }
         }
      }
   }

   private void dispatchPayload(IPipeHolder.PipeMessageReceiver part, Consumer<MessagePipePayload> sender) {
      byte[] data = this.buildSinglePayload(part);
      if (data != null) {
         sender.accept(new MessagePipePayload(this.worldPosition, part.ordinal(), data));
      }
   }

   @Nullable
   private byte[] buildSinglePayload(IPipeHolder.PipeMessageReceiver part) {
      return PipeHolderPayloadBuilder.buildSingle(this, part);
   }

   @Nullable
   private byte[] buildMultiPayload(Set<IPipeHolder.PipeMessageReceiver> parts) {
      return PipeHolderPayloadBuilder.buildMulti(this, parts);
   }

   private void sendTrackingPayload(MessagePipePayload payload) {
      if (this.level instanceof ServerLevel serverLevel) {
         PipePayloadMessageQueue.enqueueTracking(serverLevel, this.worldPosition, payload);
      }
   }

   private void enqueueGuiPayload(MessagePipePayload payload) {
      for (ServerPlayer viewer : this.guiViewers) {
         PipePayloadMessageQueue.enqueueGui(viewer, payload);
      }
   }

   private void sendGuiPayload(IPipeHolder.PipeMessageReceiver to, IPipeHolder.IWriter writer) {
      if (this.level != null && !this.level.isClientSide() && !this.guiViewers.isEmpty()) {
         try {
            byte[] data = BcEnvelopeCodec.encode(buffer -> writer.write(PacketBufferBC.asPacketBufferBc(buffer)));
            if (data == null) {
               return;
            }

            this.enqueueGuiPayload(new MessagePipePayload(this.worldPosition, to.ordinal(), data));
         } catch (Exception e) {
            BCLog.logger.warn("[transport] Failed to send pipe gui message at " + this.worldPosition, e);
         }
      }
   }

   public void dropPipeItems(Level lvl, BlockPos pos) {
      if (this.pipe != null) {
         PipeDefinition def = this.pipe.getDefinition();
         Item pipeItem = (Item)PipeApi.pipeRegistry.getItemForPipe(def);
         if (pipeItem != null) {
            ItemStack pipeStack = new ItemStack(pipeItem);
            DyeColor col = this.pipe.getColour();
            if (col != null) {
               pipeStack.set(BCTransportItems.PIPE_COLOUR, col);
            }

            Block.popResource(lvl, pos, pipeStack);
         }

         NonNullList<ItemStack> drops = NonNullList.create();
         this.pipe.addDrops(drops, 0);

         for (ItemStack drop : drops) {
            Block.popResource(lvl, pos, drop);
         }
      }

      for (int i = 0; i < 6; i++) {
         PipePluggable plug = this.pluggables[i];
         if (plug != null) {
            NonNullList<ItemStack> plugDrops = NonNullList.create();
            plug.addDrops(plugDrops, 0);

            for (ItemStack drop : plugDrops) {
               Block.popResource(lvl, pos, drop);
            }

            plug.onRemove();
            this.pluggables[i] = null;
         }
      }

      for (DyeColor color : this.wireManager.parts.values()) {
         if (color != null) {
            Item wireItem = BCTransportItems.WIRE_ITEMS.get(color);
            if (wireItem != null) {
               Block.popResource(lvl, pos, new ItemStack(wireItem));
            }
         }
      }
   }

   @Override
   public Level getPipeWorld() {
      return this.getLevel();
   }

   @Override
   public BlockPos getPipePos() {
      return this.getBlockPos();
   }

   @Override
   public BlockEntity getPipeTile() {
      return this;
   }

   public Pipe getPipe() {
      return this.pipe;
   }

   @Override
   public boolean canPlayerInteract(Player player) {
      return this.level == null
         ? false
         : player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
   }

   @Nullable
   @Override
   public PipePluggable getPluggable(Direction side) {
      return side == null ? null : this.pluggables[side.ordinal()];
   }

   @Nullable
   public PipePluggable replacePluggable(Direction side, @Nullable PipePluggable with) {
      PipePluggable old = this.pluggables[side.ordinal()];
      this.pluggables[side.ordinal()] = with;
      this.eventBus.unregisterHandler(old);
      this.eventBus.registerHandler(with);
      if (this.pipe != null) {
         this.pipe.markForUpdate();
      }

      IPipe neighbourPipe = this.getNeighbourPipe(side);
      if (neighbourPipe != null) {
         neighbourPipe.markForUpdate();
      }

      if (this.level != null && !this.level.isClientSide()) {
         this.wireManager.validate();
         this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());

         for (Direction dir : Direction.values()) {
            BlockPos npos = this.worldPosition.relative(dir);
            BlockState nstate = this.level.getBlockState(npos);
            if (!nstate.isAir()) {
               BlockState res = nstate.updateShape(
                  this.level, this.level, npos, dir.getOpposite(), this.worldPosition, this.getBlockState(), this.level.getRandom()
               );
               if (res != nstate) {
                  Block.updateOrDestroy(nstate, res, this.level, npos, 3);
               }
            }
         }

         boolean oldWasEmitter = old instanceof IWireEmitter;
         boolean newIsEmitter = with instanceof IWireEmitter;
         if (oldWasEmitter || newIsEmitter) {
            SavedDataWireSystems wireSystems = SavedDataWireSystems.get(this.level);
            wireSystems.rebuildWireSystemsAround(this);

            for (Direction dir : Direction.values()) {
               IPipe neighbour = this.getNeighbourPipe(dir);
               if (neighbour != null) {
                  wireSystems.rebuildWireSystemsAround(neighbour.getHolder());
               }
            }
         }
      }

      this.scheduleRenderUpdate();
      this.setChanged();
      return old;
   }

   @Nullable
   @Override
   public BlockEntity getNeighbourTile(Direction side) {
      return this.level == null ? null : this.level.getBlockEntity(this.worldPosition.relative(side));
   }

   @Nullable
   @Override
   public IPipe getNeighbourPipe(Direction side) {
      return this.getNeighbourTile(side) instanceof TilePipeHolder other ? other.getPipe() : null;
   }

   @Nullable
   public IMjReceiver getMjReceiverCapability(@Nullable Direction side) {
      Pipe pipe = this.getPipe();
      if (pipe != null && side != null) {
         PipePluggable plug = this.getPluggable(side);
         if (plug != null) {
            IMjReceiver receiver = plug.getCapability(MjAPI.CAP_RECEIVER);
            if (receiver != null) {
               return receiver;
            }

            if (plug.isBlocking()) {
               return null;
            }
         }

         IMjReceiver receiver = pipe.getBehaviour().getCapability(MjAPI.CAP_RECEIVER, side);
         return receiver != null ? receiver : pipe.getFlow().getCapability(MjAPI.CAP_RECEIVER, side);
      } else {
         return null;
      }
   }

   @Nullable
   public IMjRedstoneReceiver getMjRedstoneReceiverCapability(@Nullable Direction side) {
      Pipe pipe = this.getPipe();
      if (pipe != null && side != null) {
         PipePluggable plug = this.getPluggable(side);
         if (plug != null) {
            IMjRedstoneReceiver receiver = plug.getCapability(MjAPI.CAP_REDSTONE_RECEIVER);
            if (receiver != null) {
               return receiver;
            }

            if (plug.isBlocking()) {
               return null;
            }
         }

         IMjRedstoneReceiver receiver = pipe.getBehaviour().getCapability(MjAPI.CAP_REDSTONE_RECEIVER, side);
         return receiver != null ? receiver : pipe.getFlow().getCapability(MjAPI.CAP_REDSTONE_RECEIVER, side);
      } else {
         return null;
      }
   }

   @Nullable
   public IMjConnector getMjConnectorCapability(@Nullable Direction side) {
      Pipe pipe = this.getPipe();
      if (pipe != null && side != null) {
         PipePluggable plug = this.getPluggable(side);
         if (plug != null) {
            IMjConnector connector = plug.getCapability(MjAPI.CAP_CONNECTOR);
            if (connector != null) {
               return connector;
            }

            if (plug.isBlocking()) {
               return null;
            }
         }

         IMjConnector connector = pipe.getBehaviour().getCapability(MjAPI.CAP_CONNECTOR, side);
         return connector != null ? connector : pipe.getFlow().getCapability(MjAPI.CAP_CONNECTOR, side);
      } else {
         return null;
      }
   }

   @Nullable
   public EnergyStorage getSidedEnergyStorage(@Nullable Direction side) {
      Pipe pipe = this.getPipe();
      if (pipe != null && side != null) {
         PipePluggable plug = this.getPluggable(side);
         if (plug != null && plug.isBlocking()) {
            return null;
         } else {
            return pipe.getFlow() instanceof PipeFlowRedstoneFlux ? PipeFlowInternalAccess.energyStorage(pipe.getFlow(), side) : null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   public Storage<FluidVariant> getSidedFluidStorage(@Nullable Direction side) {
      Pipe pipe = this.getPipe();
      if (pipe != null && side != null) {
         PipePluggable plug = this.getPluggable(side);
         if (plug != null && plug.isBlocking()) {
            return null;
         } else {
            return pipe.getFlow() instanceof IFlowFluid ? PipeFlowInternalAccess.fluidStorage(pipe.getFlow(), side) : null;
         }
      } else {
         return null;
      }
   }

   @Nullable
   public Storage<ItemVariant> getSidedItemStorage(@Nullable Direction side) {
      Pipe pipe = this.getPipe();
      if (pipe != null && side != null) {
         PipePluggable plug = this.getPluggable(side);
         return plug != null && plug.isBlocking() ? null : PipeFlowInternalAccess.itemStorage(pipe.getFlow(), side);
      } else {
         return null;
      }
   }

   public WireManager getWireManager() {
      return this.wireManager;
   }

   @Override
   public GameProfile getOwner() {
      return this.owner;
   }

   @Override
   public boolean fireEvent(PipeEvent event) {
      return this.eventBus.fireEvent(event);
   }

   @Override
   public void scheduleRenderUpdate() {
      if (this.level != null && this.level.isClientSide()) {
         this.refreshClientModel();
      } else {
         this.scheduleRenderUpdate = true;
      }
   }

   @Override
   public void scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver... parts) {
      Collections.addAll(this.networkUpdates, parts);
   }

   @Override
   public void scheduleNetworkGuiUpdate(IPipeHolder.PipeMessageReceiver... parts) {
      Collections.addAll(this.networkGuiUpdates, parts);
   }

   @Override
   public void sendMessage(IPipeHolder.PipeMessageReceiver to, IPipeHolder.IWriter writer) {
      if (this.level != null && !this.level.isClientSide()) {
         try {
            byte[] data = BcEnvelopeCodec.encode(buffer -> writer.write(PacketBufferBC.asPacketBufferBc(buffer)));
            if (data == null) {
               return;
            }

            this.sendTrackingPayload(new MessagePipePayload(this.worldPosition, to.ordinal(), data));
         } catch (Exception e) {
            BCLog.logger.warn("[transport] Failed to send pipe message at " + this.worldPosition, e);
         }
      }
   }

   @Override
   public void sendGuiMessage(IPipeHolder.PipeMessageReceiver to, IPipeHolder.IWriter writer) {
      this.sendGuiPayload(to, writer);
   }

   @Override
   public void onPlayerOpen(Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         this.guiViewers.add(serverPlayer);
      }
   }

   @Override
   public void onPlayerClose(Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         this.guiViewers.remove(serverPlayer);
      }
   }

   @Override
   public int getRedstoneInput(Direction side) {
      if (this.level == null) {
         return 0;
      } else {
         return side == null ? this.level.getBestNeighborSignal(this.worldPosition) : this.level.getSignal(this.worldPosition.relative(side), side);
      }
   }

   public int getRedstoneOutput(Direction side) {
      return side == null ? 0 : this.redstoneOutputs[side.ordinal()];
   }

   @Override
   public boolean setRedstoneOutput(Direction side, int value) {
      if (side == null) {
         boolean changed = false;

         for (int i = 0; i < 6; i++) {
            if (this.redstoneOutputsThisTick[i] < value) {
               this.redstoneOutputsThisTick[i] = value;
               changed = true;
            }
         }

         return changed;
      } else {
         int idx = side.ordinal();
         if (this.redstoneOutputsThisTick[idx] < value) {
            this.redstoneOutputsThisTick[idx] = value;
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      if (this.pipe == null) {
         left.add("Pipe = null");
      } else {
         left.add("Pipe:");
         this.pipe.getDebugInfo(left, right, side);
      }
   }

   public ModelData getModelData() {
      return ModelData.builder().with(PIPE_MODEL_DATA, this).build();
   }
}
