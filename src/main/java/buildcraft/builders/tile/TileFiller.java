/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.container.ContainerFiller;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.filler.FillerUtil;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.TemplateBuilder;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.MjEnergyStorage;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.lib.transfer.neighbor.NeighborTransfers;
import com.mojang.authlib.GameProfile;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class TileFiller
   extends BcBlockEntity
   implements IDebuggable,
   IFillerStatementContainer,
   IControllable,
   IHasWork,
   ITileForTemplateBuilder,
   MenuProvider,
   BlockEntityExtendedMenu {
   public static final int INV_SIZE = 27;
   public static final Identifier ADVANCEMENT_BUILDING_FOR_THE_FUTURE = Identifier.parse("buildcraftbuilders:building_for_the_future");
   private final MjBattery battery = new MjBattery(16000L * MjAPI.MJ);
   private final MjBatteryReceiver mjReceiver = new MjBatteryReceiver(this.battery);
   private boolean canExcavate = true;
   public boolean inverted = false;
   private boolean finished = false;
   private byte lockedTicks = 0;
   private IControllable.Mode mode = IControllable.Mode.ON;
   public final Box box = new Box();
   public AddonFillerPlanner addon;
   public boolean markerBox = true;
   public final FullStatement<IFillerPattern> patternStatement = new FullStatement<>(
      FillerType.INSTANCE, 4, (statement, paramIndex) -> this.onStatementChange()
   );
   public final ItemHandlerSimple invResources;
   private Template.BuildingInfo buildingInfo;
   public TemplateBuilder builder = new TemplateBuilder(this);
   @Nullable
   private GameProfile owner;

   public MjBatteryReceiver getMjReceiver() {
      return this.mjReceiver;
   }

   @Nullable
   public MjEnergyStorage getSidedEnergyStorage() {
      return MjEnergyStorage.createIfRfEnabled(this.getBattery());
   }

   public TileFiller(BlockPos pos, BlockState state) {
      super(BCBuildersBlockEntities.FILLER, pos, state);
      this.invResources = this.itemManager
         .addInvHandler("resources", 27, (slot, stack) -> stack.getItem() instanceof BlockItem, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
      this.invResources.setCallback((handler, slot, before, after) -> {
         this.setChanged();
         if (this.level != null && !this.level.isClientSide()) {
            if (this.builder != null) {
               this.builder.resourcesChanged();
            }

            this.finished = false;
         }
      });
   }

   public void setRemoved() {
      super.setRemoved();
      BCBuildersEventDist.INSTANCE.invalidateFiller(this);
   }

   public void clearRemoved() {
      super.clearRemoved();
      BCBuildersEventDist.INSTANCE.validateFiller(this);
   }

   @Override
   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
      super.onPlacedBy(placer, stack);
      this.owner = super.getOwner();
      if (this.level != null && !this.level.isClientSide()) {
         BlockState blockState = this.level.getBlockState(this.worldPosition);
         Direction facing = (Direction)blockState.getValue(HorizontalDirectionalBlock.FACING);
         BlockPos offsetPos = this.worldPosition.relative(facing.getOpposite());
         WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(this.level);
         VolumeBox volumeBox = volumeBoxes.getVolumeBoxAt(offsetPos);
         BlockEntity tile = this.level.getBlockEntity(offsetPos);
         if (volumeBox != null) {
            this.addon = (AddonFillerPlanner)volumeBox.addons.values().stream().filter(AddonFillerPlanner.class::isInstance).findFirst().orElse(null);
            if (this.addon != null) {
               volumeBox.locks
                  .add(
                     new Lock(
                        new Lock.Cause.CauseBlock(this.worldPosition, blockState.getBlock()),
                        new Lock.Target.TargetAddon(this.addon.getSlot()),
                        new Lock.Target.TargetRemove(),
                        new Lock.Target.TargetResize(),
                        new Lock.Target.TargetUsedByMachine(Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE)
                     )
                  );
               volumeBoxes.markDirtyAndBroadcast();
               this.addon.updateBuildingInfo();
               this.markerBox = false;
            } else {
               this.box.reset();
               this.box.setMin(volumeBox.box.min());
               this.box.setMax(volumeBox.box.max());
               volumeBox.locks
                  .add(
                     new Lock(
                        new Lock.Cause.CauseBlock(this.worldPosition, blockState.getBlock()),
                        new Lock.Target.TargetRemove(),
                        new Lock.Target.TargetResize(),
                        new Lock.Target.TargetUsedByMachine(Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE)
                     )
                  );
               volumeBoxes.markDirtyAndBroadcast();
               this.markerBox = false;
            }
         } else if (tile instanceof IAreaProvider provider) {
            this.box.reset();
            this.box.setMin(provider.min());
            this.box.setMax(provider.max());
            provider.removeFromWorld();
         }

         this.updateBuildingInfo();
         this.setChanged();
         if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         }
      }
   }

   private void updateBuildingInfo() {
      if (this.builder != null && this.getTemplateBuildingInfo() != null) {
         this.builder.cancel();
      }

      this.buildingInfo = this.hasBox() && this.addon == null
         ? FillerUtil.createBuildingInfo(
            this,
            this.patternStatement,
            IntStream.range(0, this.patternStatement.maxParams).mapToObj(this.patternStatement::get).toArray(IStatementParameter[]::new),
            this.inverted
         )
         : null;
      if (this.getTemplateBuildingInfo() != null && this.builder != null) {
         this.builder.updateSnapshot();
      }
   }

   public void tick() {
      if (this.level != null) {
         if (this.level.isClientSide()) {
            this.patternStatement.canInteract = !this.isLocked();
            SnapshotBuilder<?> b = this.getBuilder();
            if (b != null) {
               b.clientTick();
            }
         } else {
            this.battery.tick(this.level, this.worldPosition);
            this.lockedTicks--;
            if (this.lockedTicks < 0) {
               this.lockedTicks = 0;
            }

            if (this.mode != IControllable.Mode.OFF && !this.isFinished()) {
               SnapshotBuilder<?> b = this.getBuilder();
               if (b != null) {
                  if (this.level.getGameTime() % 5L == 1L) {
                     b.onNetworkSync();
                  }

                  boolean done = b.tick();
                  if (done) {
                     if (!this.finished && this.mode == IControllable.Mode.LOOP && this.owner != null) {
                        AdvancementUtil.unlockAdvancement(this.owner.id(), this.level, ADVANCEMENT_BUILDING_FOR_THE_FUTURE);
                     }

                     this.finished = true;
                     b.onNetworkSync();
                     this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                  }

                  if (this.level.getGameTime() % 5L == 0L) {
                     MessageUtil.sendUpdateToTrackingPlayers(this);
                  }
               }
            }
         }
      }
   }

   public void onStatementChange() {
      this.finished = false;
      this.updateBuildingInfo();
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putLong("battery_mj", this.battery.getStored());
      output.putBoolean("canExcavate", this.canExcavate);
      output.putBoolean("inverted", this.inverted);
      output.putBoolean("finished", this.finished);
      output.putByte("lockedTicks", this.lockedTicks);
      output.putByte("mode", (byte)this.mode.ordinal());
      output.putBoolean("markerBox", this.markerBox);
      if (this.box.isInitialized()) {
         output.putBoolean("box_initialized", true);
         BlockPos bMin = this.box.min();
         BlockPos bMax = this.box.max();
         output.putInt("box_minX", bMin.getX());
         output.putInt("box_minY", bMin.getY());
         output.putInt("box_minZ", bMin.getZ());
         output.putInt("box_maxX", bMax.getX());
         output.putInt("box_maxY", bMax.getY());
         output.putInt("box_maxZ", bMax.getZ());
      } else {
         output.putBoolean("box_initialized", false);
      }

      output.store("patternStatement", CompoundTag.CODEC, this.patternStatement.writeToNbt());
      output.store("items", CompoundTag.CODEC, this.itemManager.serializeNBT());
      if (this.builder != null) {
         output.store("builderState", CompoundTag.CODEC, this.builder.serializeNBT());
         output.store("builderClientData", CompoundTag.CODEC, this.builder.serializeClientNBT());
      }

      if (this.owner != null) {
         ValueOutput ownerChild = output.child("owner");
         ownerChild.putString("name", this.owner.name() != null ? this.owner.name() : "");
         ownerChild.putString("uuid", this.owner.id() != null ? this.owner.id().toString() : "");
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      long stored = input.getLongOr("battery_mj", 0L);
      this.battery.setStored(stored);
      this.canExcavate = input.getBooleanOr("canExcavate", true);
      this.inverted = input.getBooleanOr("inverted", false);
      this.finished = input.getBooleanOr("finished", false);
      this.lockedTicks = input.getByteOr("lockedTicks", (byte)0);
      int modeOrdinal = input.getByteOr("mode", (byte)0);
      IControllable.Mode[] modes = IControllable.Mode.values();
      this.mode = modeOrdinal >= 0 && modeOrdinal < modes.length ? modes[modeOrdinal] : IControllable.Mode.ON;
      this.markerBox = input.getBooleanOr("markerBox", true);
      if (input.getBooleanOr("box_initialized", false)) {
         int minX = input.getIntOr("box_minX", 0);
         int minY = input.getIntOr("box_minY", 0);
         int minZ = input.getIntOr("box_minZ", 0);
         int maxX = input.getIntOr("box_maxX", 0);
         int maxY = input.getIntOr("box_maxY", 0);
         int maxZ = input.getIntOr("box_maxZ", 0);
         this.box.reset();
         this.box.setMin(new BlockPos(minX, minY, minZ));
         this.box.setMax(new BlockPos(maxX, maxY, maxZ));
      }

      input.read("patternStatement", CompoundTag.CODEC).ifPresent(this.patternStatement::readFromNbt);
      input.read("items", CompoundTag.CODEC).ifPresent(this.itemManager::deserializeNBT);
      List<SnapshotBuilder<ITileForTemplateBuilder>.BreakTask> savedBreak = new ArrayList<>();
      List<SnapshotBuilder<ITileForTemplateBuilder>.PlaceTask> savedPlace = new ArrayList<>();
      if (this.level != null && this.level.isClientSide() && this.builder != null) {
         savedBreak.addAll(this.builder.clientBreakTasks);
         savedPlace.addAll(this.builder.clientPlaceTasks);
      }

      if (this.level == null || !this.level.isClientSide() || this.buildingInfo == null) {
         this.updateBuildingInfo();
      }

      if (this.builder != null) {
         input.read("builderState", CompoundTag.CODEC).ifPresent(this.builder::deserializeNBT);
         input.read("builderClientData", CompoundTag.CODEC).ifPresent(tag -> {
            Queue<SnapshotBuilder<ITileForTemplateBuilder>.BreakTask> serverBreak = new ArrayDeque<>();
            Queue<SnapshotBuilder<ITileForTemplateBuilder>.PlaceTask> serverPlace = new ArrayDeque<>();
            NBTUtilBC.readCompoundList(tag.get("breakTasks")).map(cmp -> this.builder.new BreakTask(cmp)).forEach(serverBreak::add);
            NBTUtilBC.readCompoundList(tag.get("placeTasks")).map(cmp -> this.builder.new PlaceTask(cmp)).forEach(serverPlace::add);
            this.builder.receiveServerTaskData(serverBreak, serverPlace, savedBreak, savedPlace);
         });
      }

      Optional<ValueInput> ownerInputOpt = input.child("owner");
      if (ownerInputOpt.isPresent()) {
         ValueInput ownerInput = ownerInputOpt.get();
         String uuidStr = ownerInput.getStringOr("uuid", "");
         String name = ownerInput.getStringOr("name", "");
         if (!uuidStr.isEmpty()) {
            try {
               this.owner = new GameProfile(UUID.fromString(uuidStr), name);
            } catch (Exception e) {
               this.owner = null;
            }
         }
      }
   }

   @Override
   public CompoundTag getUpdateTag(Provider registries) {
      CompoundTag tag = this.saveCustomOnly(registries);
      tag.remove("items");
      return tag;
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("battery = " + this.battery.getDebugString());
      left.add("box = " + this.box);
      left.add("pattern = " + this.patternStatement.get());
      left.add("mode = " + this.mode);
      left.add("is_finished = " + this.finished);
      left.add("lockedTicks = " + this.lockedTicks);
      left.add("addon = " + this.addon);
      left.add("markerBox = " + this.markerBox);
      left.add("hasBox = " + this.hasBox());
      left.add("isValid = " + this.isValid());
      left.add("buildingInfo = " + (this.buildingInfo != null ? "present" : "null"));
      left.add("leftToBreak = " + this.getCountToBreak());
      left.add("leftToPlace = " + this.getCountToPlace());
   }

   @Override
   public Level getWorldBC() {
      return this.level;
   }

   @Override
   public MjBattery getBattery() {
      return this.battery;
   }

   @Override
   public BlockPos getBuilderPos() {
      return this.worldPosition;
   }

   @Override
   public boolean canExcavate() {
      return this.canExcavate;
   }

   @Override
   public SnapshotBuilder<?> getBuilder() {
      return this.isValid() ? this.builder : null;
   }

   @Override
   public Template.BuildingInfo getTemplateBuildingInfo() {
      return this.isValid() ? (this.addon != null ? this.addon.buildingInfo : this.buildingInfo) : null;
   }

   @Override
   public IItemTransactor getInvResources() {
      return this.invResources;
   }

   @Override
   public ItemStack getBreakingTool() {
      return new ItemStack(Items.IRON_PICKAXE);
   }

   @Override
   public void onBlockBroken(BlockPos brokenPos, List<ItemStack> drops, int xp, FluidStack capturedFluid) {
      if (this.level instanceof ServerLevel serverLevel) {
         for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
               ItemStack remaining = this.insertIntoAdjacentNonPipeInventory(serverLevel, stack.copy());
               if (!remaining.isEmpty()) {
                  Block.popResource(serverLevel, brokenPos, remaining);
               }
            }
         }

         if (xp > 0) {
            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(this.worldPosition), xp);
         }
      }
   }

   private ItemStack insertIntoAdjacentNonPipeInventory(ServerLevel serverLevel, ItemStack stack) {
      return NeighborTransfers.insertItemsShuffled(serverLevel, this.worldPosition, stack, null, adj -> adj instanceof IPipeHolder);
   }

   @Override
   public GameProfile getOwner() {
      return this.owner;
   }

   public int getCountToPlace() {
      return this.builder != null ? this.builder.leftToPlace : 0;
   }

   public int getCountToBreak() {
      return this.builder != null ? this.builder.leftToBreak : 0;
   }

   public boolean isFinished() {
      return this.mode != IControllable.Mode.LOOP && this.finished;
   }

   public boolean isLocked() {
      return this.lockedTicks > 0;
   }

   @Override
   public BlockEntity getTile() {
      return this;
   }

   @Nullable
   @Override
   public BlockEntity getNeighbourTile(Direction side) {
      return this.level == null ? null : this.level.getBlockEntity(this.worldPosition.relative(side));
   }

   @Override
   public Level getFillerWorld() {
      return this.level;
   }

   @Override
   public boolean hasBox() {
      return this.addon != null || this.box.isInitialized();
   }

   public boolean isValid() {
      return !this.hasBox() ? false : (this.addon != null ? this.addon.buildingInfo : this.buildingInfo) != null;
   }

   @Override
   public IBox getBox() {
      if (!this.hasBox()) {
         throw new IllegalStateException("Called getBox() when hasBox() returned false!");
      } else {
         return this.addon != null ? this.addon.volumeBox.box : this.box;
      }
   }

   @Override
   public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {
      boolean changed = this.patternStatement.get() != pattern;
      if (!changed && params != null) {
         IStatementParameter[] currentParams = this.patternStatement.getParameters();

         for (int i = 0; i < params.length && i < currentParams.length; i++) {
            if (currentParams[i] != params[i]) {
               changed = true;
               break;
            }
         }
      }

      if (changed) {
         this.patternStatement.set(pattern, params);
         this.onStatementChange();
      }

      this.lockedTicks = 3;
   }

   @Override
   public boolean hasWork() {
      if (this.mode == IControllable.Mode.OFF || !this.isValid()) {
         return false;
      }

      if (this.mode != IControllable.Mode.LOOP && this.finished) {
         return false;
      }

      SnapshotBuilder<?> b = this.getBuilder();
      return b != null && (b.leftToPlace > 0 || b.leftToBreak > 0);
   }

   @Override
   public IControllable.Mode getControlMode() {
      return this.mode;
   }

   @Override
   public void setControlMode(IControllable.Mode mode) {
      if (this.mode == IControllable.Mode.OFF && mode != IControllable.Mode.OFF) {
         this.finished = false;
      }

      if (mode == IControllable.Mode.LOOP && this.mode != IControllable.Mode.LOOP) {
         this.finished = false;
      }

      this.mode = mode;
      this.setChanged();
      if (this.level != null && !this.level.isClientSide()) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public boolean hasPower() {
      return this.battery.getStored() > 0L;
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftbuilders.filler");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerFiller(containerId, playerInv, this);
   }

   public boolean getCanExcavate() {
      return this.canExcavate;
   }

   public void setCanExcavate(boolean value) {
      this.canExcavate = value;
   }

   public boolean getFinished() {
      return this.finished;
   }

   public int getLockedTicks() {
      return this.lockedTicks;
   }

   public int getModeOrdinal() {
      return this.mode.ordinal();
   }
}
