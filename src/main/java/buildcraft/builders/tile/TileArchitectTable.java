/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.schematics.SchematicEntityContext;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.snapshot.BuildersServerPayload;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.PaletteIndex;
import buildcraft.builders.snapshot.SchematicBlockManager;
import buildcraft.builders.snapshot.SchematicEntityManager;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import buildcraft.core.PaperAdvancement;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class TileArchitectTable extends BcBlockEntity implements IDebuggable, MenuProvider, BlockEntityExtendedMenu {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftbuilders:architect");
   private EnumSnapshotType snapshotType = EnumSnapshotType.BLUEPRINT;
   public final Box box = new Box();
   public boolean markerBox = false;
   private BitSet templateScannedBlocks;
   private final PaletteIndex blueprintScannedPalette = new PaletteIndex();
   private int[] blueprintScannedData;
   private final List<ISchematicEntity> blueprintScannedEntities = new ArrayList<>();
   private boolean isValid = false;
   private boolean scanning = false;
   public String name = "<unnamed>";
   private int scanX;
   private int scanY;
   private int scanZ;
   private boolean scanInitialized = false;
   public final ItemHandlerSimple invSnapshotIn = this.itemManager
      .addInvHandler("in", 1, (slot, stack) -> stack.getItem() instanceof ItemSnapshot, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
   public final ItemHandlerSimple invSnapshotOut = this.itemManager.addInvHandler("out", 1, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
   private int scanProgress = 0;
   private int scanTotal = 0;
   private static final int DROP_TICKS = 10;
   private int dropCountdown = 0;
   private final List<BlockPos> scannedThisTick = new ArrayList<>();
   @Nullable
   private Blueprint cachedLivePreview;
   private long livePreviewGeneratedTick = Long.MIN_VALUE;
   private static final int LIVE_PREVIEW_TTL_TICKS = 40;
   private static final int LIVE_PREVIEW_MAX_VOLUME = 32768;

   public TileArchitectTable(BlockPos pos, BlockState state) {
      super(BCBuildersBlockEntities.ARCHITECT, pos, state);
   }

   public void setRemoved() {
      super.setRemoved();
      this.cachedLivePreview = null;
      BCBuildersEventDist.INSTANCE.invalidateArchitectTable(this);
   }

   public void clearRemoved() {
      super.clearRemoved();
      BCBuildersEventDist.INSTANCE.validateArchitectTable(this);
   }

   @Override
   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
      if (this.level != null && !this.level.isClientSide()) {
         this.cachedLivePreview = null;
         BlockState blockState = this.level.getBlockState(this.worldPosition);
         Direction facing = (Direction)blockState.getValue(HorizontalDirectionalBlock.FACING);
         BlockPos offsetPos = this.worldPosition.relative(facing.getOpposite());
         WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(this.level);
         VolumeBox volumeBox = volumeBoxes.getVolumeBoxAt(offsetPos);
         BlockEntity tile = this.level.getBlockEntity(offsetPos);
         if (volumeBox != null) {
            this.box.reset();
            this.box.setMin(volumeBox.box.min());
            this.box.setMax(volumeBox.box.max());
            this.isValid = true;
            volumeBox.locks
               .add(
                  new Lock(
                     new Lock.Cause.CauseBlock(this.worldPosition, blockState.getBlock()),
                     new Lock.Target.TargetRemove(),
                     new Lock.Target.TargetResize(),
                     new Lock.Target.TargetUsedByMachine(Lock.Target.TargetUsedByMachine.EnumType.STRIPES_READ)
                  )
               );
            volumeBoxes.markDirtyAndBroadcast();
         } else if (tile instanceof IAreaProvider provider) {
            this.box.reset();
            this.box.setMin(provider.min());
            this.box.setMax(provider.max());
            this.markerBox = true;
            this.isValid = true;
            provider.removeFromWorld();
         } else {
            this.isValid = false;
         }

         super.onPlacedBy(placer, stack);
         this.setChanged();
         if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         }
      }
   }

   public void tick() {
      if (this.level != null) {
         if (!this.level.isClientSide()) {
            ItemStack stackIn = this.invSnapshotIn.getStackInSlot(0);
            if (stackIn.isEmpty() || !this.invSnapshotOut.getStackInSlot(0).isEmpty() || !this.isValid) {
               this.scanning = false;
               if (this.dropCountdown == 0) {
                  this.scanProgress = 0;
                  this.scanTotal = 0;
               }
            } else if (!this.scanning) {
               if (stackIn.getItem() instanceof ItemSnapshot snapshotItem) {
                  this.snapshotType = snapshotItem.getSnapshotType();
               } else {
                  this.snapshotType = EnumSnapshotType.BLUEPRINT;
               }

               this.scanTotal = this.box.size().getX() * this.box.size().getY() * this.box.size().getZ();
               this.scanProgress = 0;
               this.scanning = true;
               this.scanInitialized = false;
               this.blueprintScannedPalette.clear();
               this.dropCountdown = 0;
            }

            if (this.scanning) {
               this.scanMultipleBlocks();
               if (!this.scanning) {
                  if (this.snapshotType == EnumSnapshotType.BLUEPRINT) {
                     this.scanEntities();
                  }

                  this.finishScanning();
                  this.dropCountdown = 10;
               }
            }

            if (this.dropCountdown > 0) {
               this.dropCountdown--;
               this.scanProgress = (int)((long)this.scanTotal * this.dropCountdown / 10L);
               if (this.dropCountdown == 0) {
                  this.scanProgress = 0;
                  this.scanTotal = 0;
               }
            }

            if (!this.scannedThisTick.isEmpty() && this.level instanceof ServerLevel serverLevel) {
               BcPacketDistributor.sendToPlayersTrackingChunk(
                  serverLevel,
                  new ChunkPos(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4),
                  BuildersServerPayload.architectScan(new ArrayList<>(this.scannedThisTick))
               );
               this.scannedThisTick.clear();
            }
         }
      }
   }

   private void scanMultipleBlocks() {
      int maxPerTick = this.snapshotType.maxPerTick;

      for (int i = maxPerTick; i > 0; i--) {
         this.scanSingleBlock();
         if (!this.scanning) {
            break;
         }
      }
   }

   private void scanSingleBlock() {
      BlockPos size = this.box.size();
      if (!this.scanInitialized) {
         this.templateScannedBlocks = new BitSet(Snapshot.getDataSize(size));
         this.blueprintScannedData = new int[Snapshot.getDataSize(size)];
         this.scanX = 0;
         this.scanY = 0;
         this.scanZ = 0;
         this.scanInitialized = true;
      }

      BlockPos min = this.box.min();
      BlockPos worldScanPos = new BlockPos(min.getX() + this.scanX, min.getY() + this.scanY, min.getZ() + this.scanZ);
      BlockPos schematicPos = new BlockPos(this.scanX, this.scanY, this.scanZ);
      this.scannedThisTick.add(worldScanPos);
      if (this.snapshotType == EnumSnapshotType.TEMPLATE) {
         this.templateScannedBlocks.set(Snapshot.posToIndex(size, schematicPos), !this.level.isEmptyBlock(worldScanPos));
      }

      if (this.snapshotType == EnumSnapshotType.BLUEPRINT) {
         ISchematicBlock schematicBlock = this.readSchematicBlock(worldScanPos);
         int index = this.blueprintScannedPalette.indexOf(schematicBlock);
         this.blueprintScannedData[Snapshot.posToIndex(size, schematicPos)] = index;
      }

      this.scanProgress++;
      this.scanX++;
      if (this.scanX >= size.getX()) {
         this.scanX = 0;
         this.scanZ++;
         if (this.scanZ >= size.getZ()) {
            this.scanZ = 0;
            this.scanY++;
            if (this.scanY >= size.getY()) {
               this.scanning = false;
               this.scanInitialized = false;
            }
         }
      }
   }

   private ISchematicBlock readSchematicBlock(BlockPos worldScanPos) {
      return SchematicBlockManager.getSchematicBlock(
         new SchematicBlockContext(
            this.level, this.box.min(), worldScanPos, this.level.getBlockState(worldScanPos), this.level.getBlockState(worldScanPos).getBlock()
         )
      );
   }

   @Nullable
   public Blueprint getOrRefreshLivePreview() {
      if (this.level == null || this.level.isClientSide()) {
         return null;
      }

      if (this.isValid && this.box.isInitialized()) {
         BlockPos size = this.box.size();
         long volume = (long)size.getX() * size.getY() * size.getZ();
         if (volume > 0L && volume <= 32768L) {
            long now = this.level.getGameTime();
            if (this.cachedLivePreview != null && now - this.livePreviewGeneratedTick < 40L) {
               return this.cachedLivePreview;
            }

            BlockState thisState = this.level.getBlockState(this.worldPosition);
            if (thisState.getBlock() != BCBuildersBlocks.ARCHITECT) {
               return null;
            }

            Direction facing = (Direction)thisState.getValue(HorizontalDirectionalBlock.FACING);
            int sizeX = size.getX();
            int sizeY = size.getY();
            int sizeZ = size.getZ();
            PaletteIndex palette = new PaletteIndex();
            int[] data = new int[Snapshot.getDataSize(size)];
            BlockPos min = this.box.min();

            for (int y = 0; y < sizeY; y++) {
               for (int z = 0; z < sizeZ; z++) {
                  for (int x = 0; x < sizeX; x++) {
                     BlockPos worldScanPos = new BlockPos(min.getX() + x, min.getY() + y, min.getZ() + z);
                     BlockPos schematicPos = new BlockPos(x, y, z);
                     ISchematicBlock sb = this.readSchematicBlock(worldScanPos);
                     data[Snapshot.posToIndex(size, schematicPos)] = palette.indexOf(sb);
                  }
               }
            }

            Blueprint preview = new Blueprint();
            preview.size = size;
            preview.facing = facing;
            preview.offset = this.box.min().subtract(this.worldPosition.relative(facing.getOpposite()));
            preview.palette.addAll(palette.asList());
            preview.data = data;
            preview.computeKey();
            this.cachedLivePreview = preview;
            this.livePreviewGeneratedTick = now;
            return preview;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private void scanEntities() {
      BlockPos min = this.box.min();
      BlockPos max = this.box.max();
      this.level
         .getEntities((Entity)null, new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1), entity -> true)
         .stream()
         .map(entity -> SchematicEntityManager.getSchematicEntity(new SchematicEntityContext(this.level, this.box.min(), entity)))
         .filter(Objects::nonNull)
         .forEach(this.blueprintScannedEntities::add);
   }

   private void finishScanning() {
      BlockState thisState = this.level.getBlockState(this.worldPosition);
      if (thisState.getBlock() == BCBuildersBlocks.ARCHITECT) {
         Direction facing = (Direction)thisState.getValue(HorizontalDirectionalBlock.FACING);
         Snapshot snapshot = Snapshot.create(this.snapshotType);
         snapshot.size = this.box.size();
         snapshot.facing = facing;
         snapshot.offset = this.box.min().subtract(this.worldPosition.relative(facing.getOpposite()));
         if (snapshot instanceof Template) {
            ((Template)snapshot).data = this.templateScannedBlocks;
         }

         if (snapshot instanceof Blueprint) {
            ((Blueprint)snapshot).palette.addAll(this.blueprintScannedPalette.asList());
            ((Blueprint)snapshot).data = this.blueprintScannedData;
            ((Blueprint)snapshot).entities.addAll(this.blueprintScannedEntities);
         }

         snapshot.computeKey();
         GlobalSavedDataSnapshots.get(this.level).addSnapshot(snapshot);
         ItemStack stackIn = this.invSnapshotIn.getStackInSlot(0);
         stackIn.shrink(1);
         this.invSnapshotIn.setStackInSlot(0, stackIn.isEmpty() ? ItemStack.EMPTY : stackIn);
         ItemSnapshot usedItem = this.snapshotType == EnumSnapshotType.BLUEPRINT ? BCBuildersItems.BLUEPRINT_USED : BCBuildersItems.TEMPLATE_USED;
         this.invSnapshotOut
            .setStackInSlot(
               0,
               usedItem.createUsedStack(
                  new Snapshot.Header(snapshot.key, this.getOwner() != null ? this.getOwner().id() : new UUID(0L, 0L), new Date(), this.name)
               )
            );
         this.templateScannedBlocks = null;
         this.blueprintScannedData = null;
         this.blueprintScannedPalette.clear();
         this.blueprintScannedEntities.clear();
         if (this.getOwner() != null) {
            AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, ADVANCEMENT);
            String paperCriterion = this.snapshotType == EnumSnapshotType.BLUEPRINT ? "write_to_blueprint" : "write_to_template";
            AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, PaperAdvancement.ID, paperCriterion);
         }

         this.setChanged();
         if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
         }
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
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

      output.putBoolean("markerBox", this.markerBox);
      output.putBoolean("scanning", this.scanning);
      output.putInt("snapshotType", this.snapshotType.ordinal());
      output.putBoolean("isValid", this.isValid);
      output.putString("name", this.name);
      output.store("items", CompoundTag.CODEC, this.itemManager.serializeNBT());
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
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

      this.markerBox = input.getBooleanOr("markerBox", false);
      this.scanning = input.getBooleanOr("scanning", false);
      int stOrd = input.getIntOr("snapshotType", 0);
      EnumSnapshotType[] stValues = EnumSnapshotType.values();
      this.snapshotType = stOrd >= 0 && stOrd < stValues.length ? stValues[stOrd] : EnumSnapshotType.BLUEPRINT;
      this.isValid = input.getBooleanOr("isValid", false);
      this.name = input.getStringOr("name", "<unnamed>");
      input.read("items", CompoundTag.CODEC).ifPresent(this.itemManager::deserializeNBT);
      if (this.invSnapshotIn.getStackInSlot(0).isEmpty()) {
         input.read("invSnapshotIn", ItemStack.CODEC).ifPresent(s -> this.invSnapshotIn.setStackInSlot(0, s));
      }

      if (this.invSnapshotOut.getStackInSlot(0).isEmpty()) {
         input.read("invSnapshotOut", ItemStack.CODEC).ifPresent(s -> this.invSnapshotOut.setStackInSlot(0, s));
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("box:");
      left.add(" - min = " + this.box.min());
      left.add(" - max = " + this.box.max());
      left.add("scanning = " + this.scanning);
      left.add("isValid = " + this.isValid);
      left.add("scanProgress = " + this.scanProgress + "/" + this.scanTotal);
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftbuilders.architect");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerArchitectTable(containerId, playerInv, this);
   }

   public ItemStack getSnapshotIn() {
      return this.invSnapshotIn.getStackInSlot(0);
   }

   public void setSnapshotIn(ItemStack stack) {
      this.invSnapshotIn.setStackInSlot(0, stack);
   }

   public ItemStack getSnapshotOut() {
      return this.invSnapshotOut.getStackInSlot(0);
   }

   public void setSnapshotOut(ItemStack stack) {
      this.invSnapshotOut.setStackInSlot(0, stack);
   }

   public boolean isScanning() {
      return this.scanning;
   }

   public int getScanProgress() {
      return this.scanProgress;
   }

   public int getScanTotal() {
      return this.scanTotal;
   }

   public boolean getIsValid() {
      return this.isValid;
   }

   public AABB getRenderBoundingBox() {
      if (this.box.isInitialized()) {
         BlockPos min = this.box.min();
         BlockPos max = this.box.max();
         return new AABB(
            Math.min(this.worldPosition.getX(), min.getX()),
            Math.min(this.worldPosition.getY(), min.getY()),
            Math.min(this.worldPosition.getZ(), min.getZ()),
            Math.max(this.worldPosition.getX() + 1, max.getX() + 1),
            Math.max(this.worldPosition.getY() + 1, max.getY() + 1),
            Math.max(this.worldPosition.getZ() + 1, max.getZ() + 1)
         );
      } else {
         return new AABB(this.worldPosition);
      }
   }
}
