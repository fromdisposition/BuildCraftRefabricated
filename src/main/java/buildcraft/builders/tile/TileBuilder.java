/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.lib.fabric.Mc26Compat;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.enums.EnumOptionalSnapshotType;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.block.BlockBuilder;
import buildcraft.builders.container.ContainerBuilder;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BlueprintBuilder;
import buildcraft.builders.snapshot.EnumContainerContentsMode;
import buildcraft.builders.snapshot.EnumFluidHandlingMode;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.ITileForBlueprintBuilder;
import buildcraft.builders.snapshot.ITileForTemplateBuilder;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.SnapshotBuilder;
import buildcraft.builders.snapshot.Template;
import buildcraft.builders.snapshot.TemplateBuilder;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.MjEnergyStorage;
import buildcraft.lib.fabric.transfer.MultiFluidTankStorage;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.IBlockEntityLoadHook;
import buildcraft.lib.tile.ItemHandlerSimple;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class TileBuilder
   extends BcBlockEntity
   implements IDebuggable,
   IHasWork,
   ITileForTemplateBuilder,
   ITileForBlueprintBuilder,
   IBlockEntityLoadHook,
   MenuProvider,
   BlockEntityExtendedMenu {
   public static final int RESOURCE_SLOTS = 27;
   public static final int TANK_COUNT = 4;
   public static final int TANK_CAPACITY = 8000;
   private static final Identifier ADVANCEMENT_PAVING_THE_WAY = Identifier.parse("buildcraftbuilders:paving_the_way");
   private static final Identifier ADVANCEMENT_START_OF_SOMETHING_BIG = Identifier.parse("buildcraftbuilders:start_of_something_big");
   public static final long BIG_STRUCTURE_THRESHOLD = 1024L;
   private final MjBattery battery = new MjBattery(16000L * MjAPI.MJ);
   private final MjBatteryReceiver mjReceiver = new MjBatteryReceiver(this.battery);
   private boolean canExcavate = true;
   private EnumFluidHandlingMode fluidMode = EnumFluidHandlingMode.NO_REPLACE;
   private EnumContainerContentsMode containerContentsMode = EnumContainerContentsMode.INCLUDE;
   public List<BlockPos> path = null;
   private List<BlockPos> basePoses = new ArrayList<>();
   private int currentBasePosIndex = 0;
   private Snapshot snapshot = null;
   public EnumSnapshotType snapshotType = null;
   private Template.BuildingInfo templateBuildingInfo = null;
   private Blueprint.BuildingInfo blueprintBuildingInfo = null;
   public TemplateBuilder templateBuilder = new TemplateBuilder(this);
   public BlueprintBuilder blueprintBuilder = new BlueprintBuilder(this);
   private Box currentBox = new Box();
   private Rotation rotation = null;
   private boolean isDone = false;
   private boolean wasDoneLastTick = false;
   private long bigStructureCellsBuilt = 0L;
   private boolean pavingTheWayGranted = false;
   private boolean startOfSomethingBigGranted = false;
   private ItemStack invSnapshot = ItemStack.EMPTY;
   private final ItemHandlerSimple resourceInventory = new ItemHandlerSimple(27, (handler, slot, before, after) -> this.onResourcesChanged());
   private final SingleFluidTank[] tanks = new SingleFluidTank[]{
      new SingleFluidTank(8000, SingleFluidTank.TankAccess.OPEN, this::setChanged),
      new SingleFluidTank(8000, SingleFluidTank.TankAccess.OPEN, this::setChanged),
      new SingleFluidTank(8000, SingleFluidTank.TankAccess.OPEN, this::setChanged),
      new SingleFluidTank(8000, SingleFluidTank.TankAccess.OPEN, this::setChanged)
   };
   private final MultiFluidTankStorage fluidTanks = new MultiFluidTankStorage(this.tanks);

   public TileBuilder(BlockPos pos, BlockState state) {
      super(BCBuildersBlockEntities.BUILDER, pos, state);
   }

   public void setRemoved() {
      super.setRemoved();
      BCBuildersEventDist.INSTANCE.invalidateBuilder(this);
   }

   public void clearRemoved() {
      super.clearRemoved();
      BCBuildersEventDist.INSTANCE.validateBuilder(this);
   }

   @Override
   public void onLoad() {
      if (this.level != null && !this.level.isClientSide()) {
         this.schedulePipeNeighborNotify();
      }
   }

   public MjBatteryReceiver getMjReceiver() {
      return this.mjReceiver;
   }

   @Nullable
   @Override
   public Storage<ItemVariant> getSidedItemStorage(@Nullable Direction direction) {
      return direction == null ? null : this.resourceInventory.fabricItemStorage();
   }

   @Nullable
   public Storage<FluidVariant> getSidedFluidStorage(@Nullable Direction direction) {
      return direction == null ? null : this.fluidTanks;
   }

   @Nullable
   public MjEnergyStorage getSidedEnergyStorage() {
      return MjEnergyStorage.createIfRfEnabled(this.getBattery());
   }

   @Override
   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
      if (this.level != null && !this.level.isClientSide()) {
         super.onPlacedBy(placer, stack);
         Direction facing = (Direction)this.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
         if (this.level.getBlockEntity(this.worldPosition.relative(facing.getOpposite())) instanceof IPathProvider provider) {
            ImmutableList<BlockPos> copiedPath = ImmutableList.copyOf(provider.getPath());
            if (copiedPath.size() >= 2) {
               this.path = copiedPath;
               provider.removeFromWorld();
            }
         }

         this.updateBasePoses();
         this.updateSnapshot(true);
      }
   }

   @Override
   public boolean hasWork() {
      if (this.snapshot == null) {
         return false;
      } else {
         if (this.battery.getStored() <= 0L) {
            return false;
         }

         SnapshotBuilder<?> builder = this.getBuilder();
         if (builder == null) {
            return false;
         } else {
            return !this.isDone ? true : this.currentBasePosIndex < this.basePoses.size() - 1;
         }
      }
   }

   public void tick() {
      if (this.level != null) {
         if (this.level.isClientSide()) {
            SnapshotBuilder<?> b = this.getBuilder();
            if (b != null) {
               b.clientTick();
            }
         } else {
            this.flushPipeNeighborNotify();

            if (this.snapshot == null && !this.invSnapshot.isEmpty() && this.invSnapshot.getItem() instanceof ItemSnapshot) {
               Snapshot.Header header = ItemSnapshot.getHeader(this.invSnapshot);
               if (header != null) {
                  Snapshot resolved = GlobalSavedDataSnapshots.get(this.level).getSnapshot(header.key);
                  if (resolved != null) {
                     this.snapshot = resolved;
                     this.snapshotType = resolved.getType();
                     if (this.basePoses.isEmpty()) {
                        this.updateBasePoses();
                     }

                     this.updateSnapshot(false);
                  }
               }
            }

            this.battery.tick(this.level, this.worldPosition);
            SnapshotBuilder<?> builder = this.getBuilder();
            if (builder != null && this.getBuildingInfo() != null) {
               if (this.level.getGameTime() % 5L == 1L) {
                  builder.onNetworkSync();
               }

               this.isDone = builder.tick();
               boolean justCompletedBasePos = this.isDone && !this.wasDoneLastTick;
               this.wasDoneLastTick = this.isDone;
               if (this.isDone) {
                  builder.onNetworkSync();
                  this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                  if (justCompletedBasePos) {
                     this.tryGrantBuilderAdvancements();
                  }

                  if (this.currentBasePosIndex < this.basePoses.size() - 1) {
                     this.currentBasePosIndex++;
                     if (this.currentBasePosIndex >= this.basePoses.size()) {
                        this.currentBasePosIndex = this.basePoses.size() - 1;
                     }

                     this.updateSnapshot(true);
                  }
               }

               if (this.level.getGameTime() % 5L == 0L) {
                  MessageUtil.sendUpdateToTrackingPlayers(this);
               }
            }
         }
      }
   }

   boolean shouldGrantPavingTheWay() {
      return this.path != null && this.path.size() >= 2 && !this.basePoses.isEmpty() && this.currentBasePosIndex == this.basePoses.size() - 1;
   }

   private void tryGrantBuilderAdvancements() {
      if (this.level != null && !this.level.isClientSide() && this.getOwner() != null) {
         UUID ownerId = this.getOwner().id();
         if (!this.startOfSomethingBigGranted && this.snapshot != null) {
            this.bigStructureCellsBuilt = this.bigStructureCellsBuilt + this.snapshot.countNonAirCells();
            if (this.bigStructureCellsBuilt >= 1024L && AdvancementUtil.unlockAdvancement(ownerId, this.level, ADVANCEMENT_START_OF_SOMETHING_BIG)) {
               this.startOfSomethingBigGranted = true;
            }
         }

         if (!this.pavingTheWayGranted && this.shouldGrantPavingTheWay() && AdvancementUtil.unlockAdvancement(ownerId, this.level, ADVANCEMENT_PAVING_THE_WAY)) {
            this.pavingTheWayGranted = true;
         }
      }
   }

   private void updateSnapshot(boolean canGetFacing) {
      Optional.ofNullable(this.getBuilder()).ifPresent(SnapshotBuilder::cancel);
      if (this.snapshot != null && this.getCurrentBasePos() != null) {
         this.snapshotType = this.snapshot.getType();
         if (canGetFacing) {
            this.rotation = Arrays.stream(Rotation.values())
               .filter(r -> r.rotate(this.snapshot.facing) == this.getBlockState().getValue(HorizontalDirectionalBlock.FACING))
               .findFirst()
               .orElse(null);
         }

         if (this.snapshot.getType() == EnumSnapshotType.TEMPLATE) {
            Template template = (Template)this.snapshot;
            this.templateBuildingInfo = template.new BuildingInfo(this.getCurrentBasePos(), this.rotation);
         }

         if (this.snapshot.getType() == EnumSnapshotType.BLUEPRINT) {
            Blueprint blueprint = (Blueprint)this.snapshot;
            this.blueprintBuildingInfo = blueprint.new BuildingInfo(this.getCurrentBasePos(), this.rotation);
            if (this.containerContentsMode == EnumContainerContentsMode.IGNORE) {
               this.blueprintBuildingInfo.refreshRequiredItemsForContentsMode(this.containerContentsMode);
            }
         }

         this.currentBox = Optional.ofNullable(this.getBuildingInfo()).map(buildingInfo -> buildingInfo.box).orElse(null);
         Optional.ofNullable(this.getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
      } else {
         this.snapshotType = null;
         this.rotation = null;
         this.templateBuildingInfo = null;
         this.blueprintBuildingInfo = null;
         this.currentBox = null;
      }

      if (this.currentBox == null) {
         this.currentBox = new Box();
      }

      this.syncBlockStateToSnapshot();
   }

   private void updateBasePoses() {
      this.basePoses.clear();
      if (this.path != null) {
         int max = this.path.size() - 1;
         this.basePoses.add(this.path.get(0));

         for (int i = 1; i <= max; i++) {
            this.basePoses.addAll(PositionUtil.getAllOnPath(this.path.get(i - 1), this.path.get(i)));
         }
      } else {
         this.basePoses.add(this.worldPosition.relative(((Direction)this.getBlockState().getValue(HorizontalDirectionalBlock.FACING)).getOpposite()));
      }
   }

   private BlockPos getCurrentBasePos() {
      return this.currentBasePosIndex < this.basePoses.size() ? this.basePoses.get(this.currentBasePosIndex) : null;
   }

   public void onSnapshotSlotChanged(ItemStack newStack) {
      if (this.level != null && !this.level.isClientSide()) {
         this.currentBasePosIndex = 0;
         this.snapshot = null;
         if (newStack.getItem() instanceof ItemSnapshot) {
            Snapshot.Header header = ItemSnapshot.getHeader(newStack);
            if (header != null) {
               Snapshot newSnapshot = GlobalSavedDataSnapshots.get(this.level).getSnapshot(header.key);
               if (newSnapshot != null) {
                  this.snapshot = newSnapshot;
               }
            }
         }

         if (this.basePoses.isEmpty()) {
            this.updateBasePoses();
         }

         this.updateSnapshot(true);
      }
   }

   private void syncBlockStateToSnapshot() {
      if (this.level != null && !this.level.isClientSide()) {
         BlockState cur = this.getBlockState();
         if (cur.hasProperty(BlockBuilder.SNAPSHOT_TYPE)) {
            EnumOptionalSnapshotType desired = EnumOptionalSnapshotType.fromNullable(this.snapshotType);
            if (cur.getValue(BlockBuilder.SNAPSHOT_TYPE) != desired) {
               this.level.setBlock(this.worldPosition, (BlockState)cur.setValue(BlockBuilder.SNAPSHOT_TYPE, desired), 3);
            }
         }
      }
   }

   public void onResourcesChanged() {
      this.setChanged();
      if (this.level == null || !this.level.isClientSide()) {
         Optional.ofNullable(this.getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
      }
   }

   public ItemStack getSnapshot() {
      return this.invSnapshot;
   }

   public void setSnapshot(ItemStack stack) {
      this.invSnapshot = stack;
      this.onSnapshotSlotChanged(stack);
      this.setChanged();
   }

   public ItemStack getResource(int slot) {
      return slot >= 0 && slot < this.resourceInventory.stacks.size() ? (ItemStack)this.resourceInventory.stacks.get(slot) : ItemStack.EMPTY;
   }

   public void setResource(int slot, ItemStack stack) {
      if (slot >= 0 && slot < this.resourceInventory.stacks.size()) {
         this.resourceInventory.stacks.set(slot, stack);
         this.onResourcesChanged();
      }
   }

   public SingleFluidTank getTank(int i) {
      return i >= 0 && i < this.tanks.length ? this.tanks[i] : null;
   }

   @Override
   public MultiFluidTankStorage getFluidTanks() {
      return this.fluidTanks;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putLong("battery_mj", this.battery.getStored());
      output.putBoolean("canExcavate", this.canExcavate);
      output.putInt("fluidMode", this.fluidMode.ordinal());
      output.putInt("containerContentsMode", this.containerContentsMode.ordinal());
      output.putInt("currentBasePosIndex", this.currentBasePosIndex);
      if (this.rotation != null) {
         output.putInt("rotation", this.rotation.ordinal());
      }

      if (this.currentBox.isInitialized()) {
         output.putBoolean("box_initialized", true);
         BlockPos bMin = this.currentBox.min();
         BlockPos bMax = this.currentBox.max();
         output.putInt("box_minX", bMin.getX());
         output.putInt("box_minY", bMin.getY());
         output.putInt("box_minZ", bMin.getZ());
         output.putInt("box_maxX", bMax.getX());
         output.putInt("box_maxY", bMax.getY());
         output.putInt("box_maxZ", bMax.getZ());
      } else {
         output.putBoolean("box_initialized", false);
      }

      if (!this.invSnapshot.isEmpty()) {
         output.store("invSnapshot", ItemStack.CODEC, this.invSnapshot);
      }

      for (int i = 0; i < this.resourceInventory.stacks.size(); i++) {
         ItemStack stack = (ItemStack)this.resourceInventory.stacks.get(i);
         if (!stack.isEmpty()) {
            output.store("invRes_" + i, ItemStack.CODEC, stack);
         }
      }

      for (int i = 0; i < this.tanks.length; i++) {
         FluidStack res = this.tanks[i].getFluidStack();
         if (!res.isEmpty()) {
            Identifier id = BuiltInRegistries.FLUID.getKey(res.getFluid());
            if (id != null) {
               output.putString("tank_" + i + "_fluid", id.toString());
               output.putInt("tank_" + i + "_amount", this.tanks[i].getAmountMb());
            }
         }
      }

      if (this.snapshotType != null) {
         output.putInt("snapshotType", this.snapshotType.ordinal());
      }

      if (this.path != null) {
         output.putInt("path_count", this.path.size());

         for (int i = 0; i < this.path.size(); i++) {
            BlockPos p = this.path.get(i);
            output.putInt("path_" + i + "_x", p.getX());
            output.putInt("path_" + i + "_y", p.getY());
            output.putInt("path_" + i + "_z", p.getZ());
         }
      }

      output.putLong("bigStructureCellsBuilt", this.bigStructureCellsBuilt);
      output.putBoolean("pavingTheWayGranted", this.pavingTheWayGranted);
      output.putBoolean("startOfSomethingBigGranted", this.startOfSomethingBigGranted);
      output.putBoolean("wasDoneLastTick", this.wasDoneLastTick);
      SnapshotBuilder<?> activeBuilder = this.getBuilder();
      if (activeBuilder != null) {
         output.store("builderState", CompoundTag.CODEC, activeBuilder.serializeNBT());
         output.store("builderClientData", CompoundTag.CODEC, activeBuilder.serializeClientNBT());
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      long stored = input.getLongOr("battery_mj", 0L);
      this.battery.setStored(stored);
      this.canExcavate = input.getBooleanOr("canExcavate", true);
      this.fluidMode = EnumFluidHandlingMode.fromOrdinal(input.getIntOr("fluidMode", 0));
      this.containerContentsMode = EnumContainerContentsMode.fromOrdinal(input.getIntOr("containerContentsMode", 0));
      this.currentBasePosIndex = input.getIntOr("currentBasePosIndex", 0);
      int rotOrdinal = input.getIntOr("rotation", -1);
      if (rotOrdinal >= 0 && rotOrdinal < Rotation.values().length) {
         this.rotation = Rotation.values()[rotOrdinal];
      }

      if (input.getBooleanOr("box_initialized", false)) {
         int minX = input.getIntOr("box_minX", 0);
         int minY = input.getIntOr("box_minY", 0);
         int minZ = input.getIntOr("box_minZ", 0);
         int maxX = input.getIntOr("box_maxX", 0);
         int maxY = input.getIntOr("box_maxY", 0);
         int maxZ = input.getIntOr("box_maxZ", 0);
         this.currentBox.reset();
         this.currentBox.setMin(new BlockPos(minX, minY, minZ));
         this.currentBox.setMax(new BlockPos(maxX, maxY, maxZ));
      }

      this.invSnapshot = input.read("invSnapshot", ItemStack.CODEC).orElse(ItemStack.EMPTY);

      for (int i = 0; i < this.resourceInventory.stacks.size(); i++) {
         this.resourceInventory.stacks.set(i, input.read("invRes_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
      }

      for (int i = 0; i < this.tanks.length; i++) {
         this.tanks[i].setContents(FluidStack.EMPTY);
         String fluidId = input.getStringOr("tank_" + i + "_fluid", "");
         if (!fluidId.isEmpty()) {
            Identifier id = Identifier.tryParse(fluidId);
            if (id != null) {
               Fluid fluid = Mc26Compat.getFluid(id);
               if (fluid != null && fluid != Fluids.EMPTY) {
                  int amount = input.getIntOr("tank_" + i + "_amount", 0);
                  if (amount > 0) {
                     this.tanks[i].setContents(new FluidStack(fluid, amount));
                  }
               }
            }
         }
      }

      this.bigStructureCellsBuilt = input.getLongOr("bigStructureCellsBuilt", 0L);
      this.pavingTheWayGranted = input.getBooleanOr("pavingTheWayGranted", false);
      this.startOfSomethingBigGranted = input.getBooleanOr("startOfSomethingBigGranted", false);
      this.wasDoneLastTick = input.getBooleanOr("wasDoneLastTick", false);
      int pathCount = input.getIntOr("path_count", 0);
      if (pathCount >= 2) {
         Builder<BlockPos> rebuilt = ImmutableList.builder();

         for (int i = 0; i < pathCount; i++) {
            rebuilt.add(new BlockPos(input.getIntOr("path_" + i + "_x", 0), input.getIntOr("path_" + i + "_y", 0), input.getIntOr("path_" + i + "_z", 0)));
         }

         this.path = rebuilt.build();
      } else {
         this.path = null;
      }

      if (this.level != null && this.level.isClientSide()) {
         int stOrdinal = input.getIntOr("snapshotType", -1);
         if (stOrdinal >= 0 && stOrdinal < EnumSnapshotType.values().length) {
            this.snapshotType = EnumSnapshotType.values()[stOrdinal];
         }
      }

      List<SnapshotBuilder<?>.BreakTask> savedBreak = new ArrayList<>();
      List<SnapshotBuilder<?>.PlaceTask> savedPlace = new ArrayList<>();
      if (this.level != null && this.level.isClientSide()) {
         SnapshotBuilder<?> prev = this.getBuilder();
         if (prev != null) {
            savedBreak.addAll(prev.clientBreakTasks);
            savedPlace.addAll(prev.clientPlaceTasks);
         }
      }

      if (!this.invSnapshot.isEmpty() && this.invSnapshot.getItem() instanceof ItemSnapshot) {
         Snapshot.Header header = ItemSnapshot.getHeader(this.invSnapshot);
         if (header != null && this.level != null) {
            Snapshot newSnapshot = GlobalSavedDataSnapshots.get(this.level).getSnapshot(header.key);
            if (newSnapshot != null) {
               this.snapshot = newSnapshot;
               this.snapshotType = newSnapshot.getType();
               this.updateBasePoses();
               this.updateSnapshot(false);
            }
         }
      }

      SnapshotBuilder<?> active = this.getBuilder();
      if (active != null) {
         input.read("builderState", CompoundTag.CODEC).ifPresent(active::deserializeNBT);
         input.read("builderClientData", CompoundTag.CODEC).ifPresent(tag -> applyBuilderClientData(active, tag, savedBreak, savedPlace));
      }
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static void applyBuilderClientData(
      SnapshotBuilder<?> active, CompoundTag tag, List<SnapshotBuilder<?>.BreakTask> savedBreak, List<SnapshotBuilder<?>.PlaceTask> savedPlace
   ) {
      ((SnapshotBuilder)active).loadClientNBT(tag, (List)savedBreak, (List)savedPlace);
   }

   public Box getBox() {
      return this.currentBox;
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("battery = " + this.battery.getDebugString());
      left.add("basePoses = " + (this.basePoses == null ? "null" : this.basePoses.size()));
      left.add("currentBasePosIndex = " + this.currentBasePosIndex);
      left.add("isDone = " + this.isDone);
      left.add("snapshotType = " + this.snapshotType);
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
   public EnumFluidHandlingMode getFluidMode() {
      return this.fluidMode;
   }

   public void cycleFluidMode() {
      this.fluidMode = this.fluidMode.next();
      this.setChanged();
      SnapshotBuilder<?> b = this.getBuilder();
      if (b != null) {
         b.invalidateChecksForFluidPositions();
      }
   }

   @Override
   public EnumContainerContentsMode getContainerContentsMode() {
      return this.containerContentsMode;
   }

   public void cycleContainerContentsMode() {
      this.containerContentsMode = this.containerContentsMode.next();
      this.setChanged();
      if (this.blueprintBuildingInfo != null) {
         this.blueprintBuildingInfo.refreshRequiredItemsForContentsMode(this.containerContentsMode);
      }

      SnapshotBuilder<?> b = this.getBuilder();
      if (b != null) {
         b.resourcesChanged();
         if (b instanceof BlueprintBuilder bb) {
            bb.refreshDisplayForContentsMode();
         }
      }
   }

   @Override
   public SnapshotBuilder<?> getBuilder() {
      if (this.snapshotType == EnumSnapshotType.TEMPLATE) {
         return this.templateBuilder;
      } else {
         return this.snapshotType == EnumSnapshotType.BLUEPRINT ? this.blueprintBuilder : null;
      }
   }

   private Snapshot.BuildingInfo getBuildingInfo() {
      if (this.snapshotType == EnumSnapshotType.TEMPLATE) {
         return this.templateBuildingInfo;
      } else {
         return this.snapshotType == EnumSnapshotType.BLUEPRINT ? this.blueprintBuildingInfo : null;
      }
   }

   @Override
   public Template.BuildingInfo getTemplateBuildingInfo() {
      return this.templateBuildingInfo;
   }

   @Override
   public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
      return this.blueprintBuildingInfo;
   }

   @Override
   public IItemTransactor getInvResources() {
      return this.resourceInventory;
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
               ItemStack remaining = this.resourceInventory.insert(stack.copy(), false, false);
               if (!remaining.isEmpty()) {
                  Block.popResource(serverLevel, brokenPos, remaining);
               }
            }
         }

         if (xp > 0) {
            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(this.worldPosition), xp);
         }

         if (!capturedFluid.isEmpty() && this.getFluidMode() == EnumFluidHandlingMode.CLEAR) {
            this.fluidTanks.insertMillibuckets(capturedFluid, capturedFluid.getAmount(), true);
         }
      }
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftbuilders.builder");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerBuilder(containerId, playerInv, this);
   }
}
