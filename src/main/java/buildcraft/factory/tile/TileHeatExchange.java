/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.items.FluidItemDrops;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.FactoryFluidContainers;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.container.ContainerHeatExchange;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.SidedFluidStorages;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.lib.fabric.transfer.TriggerTransferAccess;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.lib.transfer.fabric.TransferConvert;
import buildcraft.lib.transfer.neighbor.NeighborTransfers;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class TileHeatExchange extends BlockEntity implements MenuProvider, BlockEntityExtendedMenu, IDebuggable {
   private static final int[] FLUID_MULT = new int[]{5, 10, 20};
   protected TileHeatExchange.ExchangeSection section;
   private boolean checkNeighbours;
   private int lastSyncHash = 0;
   public final ItemHandlerSimple containerSlots = new ItemHandlerSimple(4, 1);

   public TileHeatExchange(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.HEAT_EXCHANGE, pos, state);
      this.containerSlots.setCallback((handler, slot, bef, aft) -> this.setChanged());
   }

   public boolean isStart() {
      return this.section instanceof TileHeatExchange.ExchangeSectionStart;
   }

   public boolean isEnd() {
      return this.section instanceof TileHeatExchange.ExchangeSectionEnd;
   }

   @Nullable
   public TileHeatExchange.ExchangeSection getSection() {
      return this.section;
   }

   @Nullable
   public SingleFluidTank getFluidTankForDirection(@Nullable Direction direction) {
      if (this.section != null && direction != null) {
         Direction facing = this.getFacing();
         if (facing == null) {
            return null;
         }

         if (this.section instanceof TileHeatExchange.ExchangeSectionStart) {
            if (direction == Direction.DOWN) {
               return this.section.tankInput;
            }

            if (direction == facing.getClockWise()) {
               return this.section.tankOutput;
            }
         } else if (this.section instanceof TileHeatExchange.ExchangeSectionEnd) {
            if (direction == Direction.UP) {
               return this.section.tankOutput;
            }

            if (direction == facing.getCounterClockWise()) {
               return this.section.tankInput;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Nullable
   public Storage<FluidVariant> getSidedFluidStorage(@Nullable Direction direction) {
      SingleFluidTank tank = this.getFluidTankForDirection(direction);
      if (tank == null || this.section == null) {
         return null;
      } else {
         return tank == this.section.tankOutput ? SidedFluidStorages.extractOnly(tank) : SidedFluidStorages.insertOnly(tank);
      }
   }

   public void markCheckNeighbours() {
      this.checkNeighbours = true;
   }

   @Nullable
   public TileHeatExchange findStart() {
      if (this.isStart()) {
         return this;
      }

      if (this.level == null) {
         return null;
      }

      Direction facing = this.getFacing();
      if (facing == null) {
         return null;
      }

      Direction dirToStart = facing.getClockWise();

      for (int i = 1; i < 6; i++) {
         if (!(this.level.getBlockEntity(this.worldPosition.relative(dirToStart, i)) instanceof TileHeatExchange other)) {
            return null;
         }

         if (other.getFacing() != facing) {
            return null;
         }

         if (other.isStart()) {
            return other;
         }
      }

      return null;
   }

   @Nullable
   Direction getFacing() {
      BlockState state = this.getBlockState();
      return state.getBlock() instanceof BlockHeatExchange ? (Direction)state.getValue(BlockHeatExchange.FACING) : null;
   }

   public void serverTick() {
      if (this.level != null) {
         if (this.checkNeighbours) {
            this.checkNeighbours = false;
            Deque<TileHeatExchange> exchangers = this.findAdjacentExchangers();
            if (exchangers.isEmpty()) {
               this.checkNeighbours = true;
            } else if (exchangers.size() < 3) {
               for (TileHeatExchange tile : exchangers) {
                  tile.removeSection();
               }
            } else if (exchangers.size() <= 5) {
               TileHeatExchange.ExchangeSectionStart sectionStart = null;
               TileHeatExchange.ExchangeSectionEnd sectionEnd = null;

               for (TileHeatExchange exchange : exchangers) {
                  exchange.checkNeighbours = false;
                  if (exchange.section instanceof TileHeatExchange.ExchangeSectionStart existingStart) {
                     if (sectionStart == null) {
                        sectionStart = existingStart;
                     }
                  } else if (exchange.section instanceof TileHeatExchange.ExchangeSectionEnd existingEnd && sectionEnd == null) {
                     sectionEnd = existingEnd;
                  }

                  exchange.section = null;
               }

               if (sectionStart == null) {
                  sectionStart = new TileHeatExchange.ExchangeSectionStart(exchangers.getFirst());
               }

               if (sectionEnd == null) {
                  sectionEnd = new TileHeatExchange.ExchangeSectionEnd(exchangers.getLast());
               }

               sectionStart.endSection = sectionEnd;
               sectionStart.middleCount = exchangers.size() - 2;
               exchangers.getFirst().setSection(sectionStart);
               exchangers.getLast().setSection(sectionEnd);
               this.updatePartProperties(exchangers);

               for (TileHeatExchange exchange : exchangers) {
                  exchange.syncToClient();
               }
            }
         }

         if (this.section != null) {
            this.section.tick();
         }

         int hash = this.computeSyncHash();
         if (hash != this.lastSyncHash) {
            this.lastSyncHash = hash;
            this.syncToClient();
         }
      }
   }

   public void clientTick() {
      if (this.level != null) {
         if (this.checkNeighbours) {
            Deque<TileHeatExchange> exchangers = this.findAdjacentExchangers();
            if (exchangers.size() > 2) {
               TileHeatExchange start = exchangers.getFirst();
               TileHeatExchange end = exchangers.getLast();
               if (start.isStart() && end.isEnd()) {
                  ((TileHeatExchange.ExchangeSectionStart)start.section).endSection = (TileHeatExchange.ExchangeSectionEnd)end.section;
                  this.checkNeighbours = false;
               }
            }
         }

         if (this.section != null) {
            this.section.clientTick();
         }
      }
   }

   private void removeSection() {
      if (this.section != null) {
         if (this.level != null && !this.level.isClientSide()) {
            NonNullList<ItemStack> toDrop = NonNullList.create();
            FluidItemDrops.addFluidDrops(toDrop, this.section.tankInput, this.section.tankOutput);

            for (ItemStack drop : toDrop) {
               Block.popResource(this.level, this.worldPosition, drop);
            }
         }

         this.section = null;
         if (this.level != null) {
            BlockState oldState = this.getBlockState();
            if (oldState.getBlock() instanceof BlockHeatExchange) {
               BlockState newState = (BlockState)oldState.setValue(BlockHeatExchange.PART, BlockHeatExchange.EnumExchangePart.MIDDLE);
               if (oldState != newState) {
                  this.level.setBlock(this.worldPosition, newState, 3);
               }
            }
         }

         this.syncToClient();
      }
   }

   private void setSection(TileHeatExchange.ExchangeSection section) {
      if (this.section != section) {
         this.section = section;
         section.setTile(this);
         this.syncToClient();
      }
   }

   private void updatePartProperties(Deque<TileHeatExchange> exchangers) {
      if (this.level != null) {
         TileHeatExchange[] arr = exchangers.toArray(new TileHeatExchange[0]);

         for (int i = 0; i < arr.length; i++) {
            TileHeatExchange tile = arr[i];
            BlockHeatExchange.EnumExchangePart part;
            if (i == 0) {
               part = BlockHeatExchange.EnumExchangePart.START;
            } else if (i == arr.length - 1) {
               part = BlockHeatExchange.EnumExchangePart.END;
            } else {
               part = BlockHeatExchange.EnumExchangePart.MIDDLE;
            }

            BlockState oldState = tile.getBlockState();
            if (oldState.getBlock() instanceof BlockHeatExchange) {
               BlockState newState = (BlockState)oldState.setValue(BlockHeatExchange.PART, part);
               if (oldState != newState) {
                  this.level.setBlock(tile.worldPosition, newState, 3);
               }
            }
         }
      }
   }

   private Deque<TileHeatExchange> findAdjacentExchangers() {
      Direction thisFacing = this.getFacing();
      if (thisFacing == null) {
         return new ArrayDeque<>();
      }

      Direction dirToStart = thisFacing.getClockWise();
      Direction dirToEnd = thisFacing.getCounterClockWise();
      Deque<TileHeatExchange> exchangers = new ArrayDeque<>();
      exchangers.add(this);

      for (int i = 1; i < 6; i++) {
         if (!(this.level.getBlockEntity(this.worldPosition.relative(dirToStart, i)) instanceof TileHeatExchange other) || other.getFacing() != thisFacing) {
            break;
         }

         exchangers.addFirst(other);
      }

      for (int i = 1; i < 6; i++) {
         if (!(this.level.getBlockEntity(this.worldPosition.relative(dirToEnd, i)) instanceof TileHeatExchange other) || other.getFacing() != thisFacing) {
            break;
         }

         exchangers.addLast(other);
      }

      return exchangers;
   }

   public boolean rotate() {
      Direction thisFacing = this.getFacing();
      if (thisFacing != null && this.level != null) {
         Deque<TileHeatExchange> exchangers = this.findAdjacentExchangers();
         if (exchangers.size() == 1) {
            Direction[] horizontals = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
            int idx = 0;

            for (int i = 0; i < horizontals.length; i++) {
               if (horizontals[i] == thisFacing) {
                  idx = i;
                  break;
               }
            }

            Direction next = horizontals[(idx + 1) % 4];
            this.level.setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue(BlockHeatExchange.FACING, next), 3);
         } else {
            TileHeatExchange.ExchangeSectionStart start = null;
            TileHeatExchange.ExchangeSectionEnd end = null;

            for (TileHeatExchange exchange : exchangers) {
               if (exchange.section instanceof TileHeatExchange.ExchangeSectionStart s) {
                  start = s;
               } else if (exchange.section instanceof TileHeatExchange.ExchangeSectionEnd e) {
                  end = e;
               }

               exchange.section = null;
               this.level
                  .setBlock(exchange.worldPosition, (BlockState)exchange.getBlockState().setValue(BlockHeatExchange.FACING, thisFacing.getOpposite()), 3);
               exchange.checkNeighbours = true;
               exchange.setChanged();
            }

            if (start != null) {
               TileHeatExchange tile = exchangers.getLast();
               tile.section = start;
               start.setTile(tile);
               tile.setChanged();
               tile.syncToClient();
            }

            if (end != null) {
               TileHeatExchange tile = exchangers.getFirst();
               tile.section = end;
               end.setTile(tile);
               tile.setChanged();
               tile.syncToClient();
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public int getSyncHash() {
      return this.computeSyncHash();
   }

   private int computeSyncHash() {
      if (this.section == null) {
         return 0;
      }

      int h = this.section instanceof TileHeatExchange.ExchangeSectionStart ? 1 : 2;
      h = h * 31 + this.section.tankInput.getAmountMb();
      h = h * 31 + this.section.tankOutput.getAmountMb();
      h = h * 31 + fluidStackSyncHash(this.section.tankInput.getFluidStack());
      h = h * 31 + fluidStackSyncHash(this.section.tankOutput.getFluidStack());
      if (this.section instanceof TileHeatExchange.ExchangeSectionStart s) {
         h = h * 31 + s.progressState.ordinal();
         h = h * 31 + s.middleCount;
      }

      return h;
   }

   private static int fluidStackSyncHash(FluidStack stack) {
      return stack != null && !stack.isEmpty() ? FluidStack.hashFluidAndComponents(stack) : 0;
   }

   private void syncToClient() {
      if (this.level != null && !this.level.isClientSide()) {
         this.setChanged();
         MessageUtil.sendUpdateToTrackingPlayers(this);
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      if (this.section == null) {
         left.add("section = null");
      } else {
         left.add("section = " + (this.section instanceof TileHeatExchange.ExchangeSectionStart ? "start" : "end"));
         this.section.getDebugInfo(left, right, side);
      }
   }

   @Override
   public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
      if (this.section == null) {
         left.add("section = null");
      } else {
         left.add("section = " + (this.section instanceof TileHeatExchange.ExchangeSectionStart ? "start" : "end"));
         this.section.getClientDebugInfo(left, right, side);
      }
   }

   public void setRemoved() {
      super.setRemoved();
      if (this.section instanceof TileHeatExchange.ExchangeSectionStart s) {
         s.endSection = null;
      }
   }

   public void clearRemoved() {
      super.clearRemoved();
      this.checkNeighbours = true;
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   @Override
   public BlockPos getScreenOpeningData(ServerPlayer player) {
      TileHeatExchange start = this.findStart();
      return start != null ? start.getBlockPos() : this.getBlockPos();
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftfactory.heat_exchange");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerHeatExchange(containerId, playerInv, this.findStart());
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.store("containerSlots", CompoundTag.CODEC, this.containerSlots.serializeNBT());
      if (this.section != null) {
         output.putBoolean("hasSection", true);
         output.putBoolean("isStart", this.section instanceof TileHeatExchange.ExchangeSectionStart);
         FluidStack inStack = this.section.tankInput.getFluidStack();
         if (!inStack.isEmpty()) {
            output.store("sectionInput", FluidStack.CODEC, inStack);
         }

         FluidStack outStack = this.section.tankOutput.getFluidStack();
         if (!outStack.isEmpty()) {
            output.store("sectionOutput", FluidStack.CODEC, outStack);
         }

         if (this.section instanceof TileHeatExchange.ExchangeSectionStart s) {
            output.putInt("middleCount", s.middleCount);
            output.putInt("progress", s.progress);
            output.putInt("progressState", s.progressState.ordinal());
         }
      } else {
         output.putBoolean("hasSection", false);
      }
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.containerSlots.deserializeNBT((CompoundTag)input.read("containerSlots", CompoundTag.CODEC).orElseGet(CompoundTag::new));
      if (input.getBooleanOr("hasSection", false)) {
         boolean isStart = input.getBooleanOr("isStart", true);
         if (isStart) {
            TileHeatExchange.ExchangeSectionStart s;
            if (this.section instanceof TileHeatExchange.ExchangeSectionStart existing) {
               s = existing;
            } else {
               s = new TileHeatExchange.ExchangeSectionStart(this);
            }

            FactoryTileUtils.loadTank(s.tankInput, input, "sectionInput");
            FactoryTileUtils.loadTank(s.tankOutput, input, "sectionOutput");
            s.middleCount = input.getIntOr("middleCount", 1);
            int stateOrd = input.getIntOr("progressState", 0);
            s.progressState = TileHeatExchange.EnumProgressState.values()[Math.min(stateOrd, TileHeatExchange.EnumProgressState.values().length - 1)];
            this.section = s;
         } else {
            TileHeatExchange.ExchangeSectionEnd e;
            if (this.section instanceof TileHeatExchange.ExchangeSectionEnd existing) {
               e = existing;
            } else {
               e = new TileHeatExchange.ExchangeSectionEnd(this);
            }

            FactoryTileUtils.loadTank(e.tankInput, input, "sectionInput");
            FactoryTileUtils.loadTank(e.tankOutput, input, "sectionOutput");
            this.section = e;
         }
      } else if (this.section != null) {
         this.section = null;
      }

      this.checkNeighbours = true;
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public enum EnumProgressState {
      OFF,
      PREPARING,
      RUNNING,
      STOPPING;
   }

   public abstract static class ExchangeSection {
      public final SingleFluidTank tankInput;
      public final SingleFluidTank tankOutput;
      public final FluidSmoother smoothedTankInput;
      public final FluidSmoother smoothedTankOutput;
      private TileHeatExchange tile;

      ExchangeSection(TileHeatExchange tile, Predicate<FluidStack> inputFilter) {
         this.tankInput = new SingleFluidTank(2000, SingleFluidTank.TankAccess.filteredInput(inputFilter::test));
         this.tankOutput = new SingleFluidTank(2000, SingleFluidTank.TankAccess.MACHINE_OUTPUT);
         this.smoothedTankInput = new FluidSmoother(this.tankInput);
         this.smoothedTankOutput = new FluidSmoother(this.tankOutput);
         this.tile = tile;
      }

      void tick() {
      }

      void clientTick() {
         this.smoothedTankInput.tick();
         this.smoothedTankOutput.tick();
      }

      void getDebugInfo(List<String> left, List<String> right, Direction side) {
         left.add("tank_input = " + FluidUtilBC.getDebugString(this.tankInput.getFluidStack()));
         left.add("tank_output = " + FluidUtilBC.getDebugString(this.tankOutput.getFluidStack()));
      }

      void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
         this.smoothedTankInput.getDebugInfo(left, right, side);
         this.smoothedTankOutput.getDebugInfo(left, right, side);
      }

      public TileHeatExchange getTile() {
         return this.tile;
      }

      public void setTile(TileHeatExchange tile) {
         this.tile = tile;
      }

      @Nullable
      Storage<FluidVariant> getFluidAutoOutputTarget() {
         return null;
      }
   }

   public static class ExchangeSectionEnd extends TileHeatExchange.ExchangeSection {
      ExchangeSectionEnd(TileHeatExchange tile) {
         super(tile, TileHeatExchange.ExchangeSectionEnd::isCoolant);
      }

      private static boolean isCoolant(FluidStack fluid) {
         IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
         return manager == null ? false : manager.getCoolableRegistry().getRecipeForInput(fluid) != null;
      }

      @Nullable
      @Override
      Storage<FluidVariant> getFluidAutoOutputTarget() {
         return this.getTile().level == null
            ? null
            : BcTransfers.fluid(this.getTile().level, this.getTile().worldPosition.above(), Direction.DOWN);
      }
   }

   public static class ExchangeSectionStart extends TileHeatExchange.ExchangeSection {
      TileHeatExchange.ExchangeSectionEnd endSection;
      public int middleCount;
      int progress = 0;
      int progressLast = 0;
      TileHeatExchange.EnumProgressState progressState = TileHeatExchange.EnumProgressState.OFF;

      ExchangeSectionStart(TileHeatExchange tile) {
         super(tile, TileHeatExchange.ExchangeSectionStart::isHeatant);
      }

      public TileHeatExchange.ExchangeSectionEnd getEndSection() {
         return this.endSection;
      }

      public TileHeatExchange.EnumProgressState getProgressState() {
         return this.progressState;
      }

      public double getProgress(float partialTicks) {
         return (this.progressLast + (this.progress - this.progressLast) * partialTicks) / 120.0;
      }

      private static boolean isHeatant(FluidStack fluid) {
         IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
         return manager == null ? false : manager.getHeatableRegistry().getRecipeForInput(fluid) != null;
      }

      @Nullable
      @Override
      Storage<FluidVariant> getFluidAutoOutputTarget() {
         Direction facing = this.getTile().getFacing();
         if (facing != null && this.getTile().level != null) {
            BlockPos targetPos = this.getTile().worldPosition.relative(facing.getClockWise());
            return BcTransfers.fluid(this.getTile().level, targetPos, facing.getCounterClockWise());
         } else {
            return null;
         }
      }

      @Override
      void tick() {
         super.tick();
         this.updateProgress();
         if (this.getTile().level != null && !this.getTile().level.isClientSide()) {
            if (this.endSection != null) {
               this.craft();
            } else if (this.progressState != TileHeatExchange.EnumProgressState.OFF) {
               this.progressState = TileHeatExchange.EnumProgressState.STOPPING;
            }

            this.output();
            this.processContainerSlots();
         }
      }

      private void processContainerSlots() {
         TileHeatExchange tile = this.getTile();
         if (tile != null && tile.level != null) {
            if (tile.level.getGameTime() % 5L == 0L) {
               if (this.endSection != null) {
                  drainSlotIntoTank(tile, 0, this.endSection.tankInput);
               }

               drainSlotIntoTank(tile, 1, this.tankInput);
               if (this.endSection != null) {
                  fillSlotFromTank(tile, 2, this.endSection.tankOutput);
               }

               fillSlotFromTank(tile, 3, this.tankOutput);
            }
         }
      }

      private static void drainSlotIntoTank(TileHeatExchange tile, int slot, SingleFluidTank tank) {
         FactoryFluidContainers.syncDrainSlot(tile.containerSlots, slot, tank);
      }

      private static void fillSlotFromTank(TileHeatExchange tile, int slot, SingleFluidTank tank) {
         FactoryFluidContainers.syncFillSlot(tile.containerSlots, slot, tank);
      }

      @Override
      void clientTick() {
         super.clientTick();
         this.updateProgress();
         this.spawnParticles();
      }

      private void updateProgress() {
         this.progressLast = this.progress;
         switch (this.progressState) {
            case PREPARING:
            case RUNNING:
               int lag = 120;
               this.progress++;
               if (this.progress >= lag) {
                  this.progress = lag;
                  this.progressState = TileHeatExchange.EnumProgressState.RUNNING;
               }
               break;
            case STOPPING:
               this.progress--;
               if (this.progress <= 0) {
                  this.progress = 0;
                  this.progressState = TileHeatExchange.EnumProgressState.OFF;
               }
         }
      }

      private void craft() {
         if (this.endSection != null) {
            SingleFluidTank c_in = this.endSection.tankInput;
            SingleFluidTank c_out = this.tankOutput;
            SingleFluidTank h_in = this.tankInput;
            SingleFluidTank h_out = this.endSection.tankOutput;
            IRefineryRecipeManager reg = BuildcraftRecipeRegistry.refineryRecipes;
            if (reg == null) {
               this.progressState = TileHeatExchange.EnumProgressState.STOPPING;
            } else {
               FluidStack c_in_fluid = c_in.getFluidStack();
               FluidStack h_in_fluid = h_in.getFluidStack();
               IRefineryRecipeManager.ICoolableRecipe c_recipe = reg.getCoolableRegistry().getRecipeForInput(c_in_fluid);
               IRefineryRecipeManager.IHeatableRecipe h_recipe = reg.getHeatableRegistry().getRecipeForInput(h_in_fluid);
               if (h_recipe != null && c_recipe != null) {
                  if (c_recipe.heatFrom() <= h_recipe.heatFrom()) {
                     this.progressState = TileHeatExchange.EnumProgressState.STOPPING;
                  } else {
                     int c_diff = c_recipe.heatFrom() - c_recipe.heatTo();
                     int h_diff = h_recipe.heatTo() - h_recipe.heatFrom();
                     if (h_diff >= 1 && c_diff >= 1) {
                        int max_amount = TileHeatExchange.FLUID_MULT[Math.min(this.middleCount - 1, TileHeatExchange.FLUID_MULT.length - 1)];
                        FluidStack c_in_f = setAmount(c_recipe.in(), max_amount);
                        FluidStack c_out_f = setAmount(c_recipe.out(), max_amount);
                        FluidStack h_in_f = setAmount(h_recipe.in(), max_amount);
                        FluidStack h_out_f = setAmount(h_recipe.out(), max_amount);
                        int c_out_amount = c_out_f != null && !c_out_f.isEmpty() ? simulateInsert(c_out, c_out_f) : max_amount;
                        int h_out_amount = h_out_f != null && !h_out_f.isEmpty() ? simulateInsert(h_out, h_out_f) : max_amount;
                        int c_in_amount = simulateExtract(c_in, c_in_f);
                        int h_in_amount = simulateExtract(h_in, h_in_f);
                        int min_common = Math.min(Math.min(c_out_amount, h_out_amount), Math.min(c_in_amount, h_in_amount));
                        if (min_common > 0) {
                           c_in_f = setAmount(c_recipe.in(), min_common);
                           c_out_f = setAmount(c_recipe.out(), min_common);
                           h_in_f = setAmount(h_recipe.in(), min_common);
                           h_out_f = setAmount(h_recipe.out(), min_common);
                           if (this.progressState == TileHeatExchange.EnumProgressState.OFF) {
                              this.progressState = TileHeatExchange.EnumProgressState.PREPARING;
                           } else if (this.progressState == TileHeatExchange.EnumProgressState.RUNNING) {
                              try (Transaction tx = Transaction.openOuter()) {
                                 boolean ok = true;
                                 if (c_out_f != null && !c_out_f.isEmpty()) {
                                    long n = c_out.insertInternal(TransferConvert.toVariant(c_out_f), TransferConvert.mbToDroplets(c_out_f.getAmount()), tx);
                                    ok = ok && TransferConvert.dropletsToMb(n) == c_out_f.getAmount();
                                 }

                                 if (ok && h_out_f != null && !h_out_f.isEmpty()) {
                                    long n = h_out.insertInternal(TransferConvert.toVariant(h_out_f), TransferConvert.mbToDroplets(h_out_f.getAmount()), tx);
                                    ok = ok && TransferConvert.dropletsToMb(n) == h_out_f.getAmount();
                                 }

                                 if (ok) {
                                    long n = c_in.extractInternal(TransferConvert.toVariant(c_in_f), TransferConvert.mbToDroplets(c_in_f.getAmount()), tx);
                                    ok = ok && TransferConvert.dropletsToMb(n) == c_in_f.getAmount();
                                 }

                                 if (ok) {
                                    long n = h_in.extractInternal(TransferConvert.toVariant(h_in_f), TransferConvert.mbToDroplets(h_in_f.getAmount()), tx);
                                    ok = ok && TransferConvert.dropletsToMb(n) == h_in_f.getAmount();
                                 }

                                 if (ok) {
                                    tx.commit();
                                 }
                              }
                           }
                        } else {
                           this.progressState = TileHeatExchange.EnumProgressState.STOPPING;
                        }
                     } else {
                        this.progressState = TileHeatExchange.EnumProgressState.STOPPING;
                     }
                  }
               } else {
                  this.progressState = TileHeatExchange.EnumProgressState.STOPPING;
               }
            }
         }
      }

      private void spawnParticles() {
         if (this.progressState == TileHeatExchange.EnumProgressState.RUNNING) {
            TileHeatExchange.ExchangeSectionEnd end = this.endSection;
            if (end != null && this.getTile().level != null) {
               Vec3 from = Vec3.atCenterOf(this.getTile().getBlockPos());
               FluidStack c_in_f = end.tankInput.getFluidStack();
               if (!c_in_f.isEmpty() && FluidUtilBC.areFluidsEqual(c_in_f.getFluid(), Fluids.LAVA)) {
                  Direction facing = this.getTile().getFacing();
                  if (facing != null) {
                     this.spewForth(from, facing.getClockWise(), true);
                  }
               }

               FluidStack h_in_f = this.tankInput.getFluidStack();
               from = Vec3.atCenterOf(end.getTile().getBlockPos());
               if (!h_in_f.isEmpty() && FluidUtilBC.areFluidsEqual(h_in_f.getFluid(), Fluids.WATER)) {
                  this.spewForth(from, Direction.UP, false);
               }
            }
         }
      }

      private void spewForth(Vec3 from, Direction dir, boolean smoke) {
         Level w = this.getTile().getLevel();
         if (w != null) {
            Vec3 vecDir = Vec3.atLowerCornerOf(dir.getUnitVec3i());
            from = from.add(vecDir);
            double x = from.x;
            double y = from.y;
            double z = from.z;
            Vec3 motion = vecDir.scale(0.4);

            for (int i = 0; i < 3; i++) {
               double dx = motion.x + (Math.random() - 0.5) * 0.1;
               double dy = motion.y + (Math.random() - 0.5) * 0.1;
               double dz = motion.z + (Math.random() - 0.5) * 0.1;
               w.addParticle(smoke ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, x, y, z, dx, dy, dz);
            }
         }
      }

      private void output() {
         Storage<FluidVariant> thisOut = this.getFluidAutoOutputTarget();
         if (thisOut != null) {
            NeighborTransfers.moveFluidCommitted(this.tankOutput, thisOut, 1000);
         }

         if (this.endSection != null) {
            Storage<FluidVariant> endOut = this.endSection.getFluidAutoOutputTarget();
            if (endOut != null) {
               NeighborTransfers.moveFluidCommitted(this.endSection.tankOutput, endOut, 1000);
            }
         }
      }

      @Override
      void getDebugInfo(List<String> left, List<String> right, Direction side) {
         super.getDebugInfo(left, right, side);
         left.add("progress = " + this.progress);
         left.add("state = " + this.progressState);
         left.add("has_end = " + (this.endSection != null));
      }

      @Nullable
      private static FluidStack setAmount(@Nullable FluidStack fluid, int amount) {
         return fluid != null && !fluid.isEmpty() ? fluid.copyWithAmount(amount) : null;
      }

      private static int simulateExtract(SingleFluidTank t, @Nullable FluidStack fluid) {
         if (fluid != null && !fluid.isEmpty()) {
            Transaction tx = Transaction.openOuter();

            int var5;
            try {
               long moved = t.extractInternal(TransferConvert.toVariant(fluid), TransferConvert.mbToDroplets(fluid.getAmount()), tx);
               var5 = (int)TransferConvert.dropletsToMb(moved);
            } catch (Throwable var7) {
               if (tx != null) {
                  try {
                     tx.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (tx != null) {
               tx.close();
            }

            return var5;
         } else {
            return 0;
         }
      }

      private static int simulateInsert(SingleFluidTank t, @Nullable FluidStack fluid) {
         if (fluid != null && !fluid.isEmpty()) {
            Transaction tx = Transaction.openOuter();

            int var5;
            try {
               long moved = t.insertInternal(TransferConvert.toVariant(fluid), TransferConvert.mbToDroplets(fluid.getAmount()), tx);
               var5 = (int)TransferConvert.dropletsToMb(moved);
            } catch (Throwable var7) {
               if (tx != null) {
                  try {
                     tx.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (tx != null) {
               tx.close();
            }

            return var5;
         } else {
            return 0;
         }
      }
   }
}
