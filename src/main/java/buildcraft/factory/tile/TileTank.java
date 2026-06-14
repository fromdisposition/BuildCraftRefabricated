/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;


import buildcraft.lib.fabric.transfer.fluid.FluidStorageOps;
import buildcraft.lib.fluid.display.FluidDisplayNames;
import buildcraft.lib.fluid.identity.FluidIdentity;
import buildcraft.lib.fluid.meta.FluidAttributes;
import buildcraft.api.items.FluidItemDrops;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.container.ContainerTank;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.fluid.SingleFluidTank;
import buildcraft.lib.fabric.transfer.fluid.TankColumnFluidStorage;
import buildcraft.lib.fluid.registry.FluidSmoother;
import buildcraft.lib.fluid.stack.FluidStack;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileTank extends BlockEntity implements MenuProvider, BlockEntityExtendedMenu, IDebuggable {
   public final SingleFluidTank fluidTank = new SingleFluidTank(16000, SingleFluidTank.TankAccess.OPEN, this::requestColumnBalance);
   public final FluidSmoother smoothedTank = new FluidSmoother(this.fluidTank::getAmountMb, this.fluidTank::getFluidStack, this.fluidTank::getCapacityMb);
   private int lastComparatorLevel;
   private int lastSyncedAmount = -1;
   private FluidStack lastSyncedFluid = FluidStack.EMPTY;
   private boolean pendingColumnBalance;

   public TileTank(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.TANK, pos, state);
   }

   public int getComparatorLevel() {
      List<TileTank> column = this.getTankColumn();
      int amountMb = 0;
      int capacityMb = 0;

      for (TileTank segment : column) {
         amountMb += segment.fluidTank.getAmountMb();
         capacityMb += segment.fluidTank.getCapacityMb();
      }

      if (capacityMb <= 0) {
         return 0;
      }

      return amountMb * 14 / capacityMb + (amountMb > 0 ? 1 : 0);
   }

   public void serverTick() {
      if (this.level != null && !this.level.isClientSide()) {
         ProfilerFiller _profiler = Profiler.get();
         _profiler.push("buildcraft:tank_serverTick");

         try {
            if (this.pendingColumnBalance) {
               this.pendingColumnBalance = false;
               this.balanceTankFluids();
            }

            int currentAmount = this.fluidTank.getAmountMb();
            FluidStack currentFluid = this.fluidTank.getFluidStack();
            if (currentAmount != this.lastSyncedAmount
               || !FluidIdentity.areEquivalentFluidStacks(currentFluid.isEmpty() ? FluidStack.EMPTY : currentFluid.copyWithAmount(1), this.lastSyncedFluid)) {
               this.lastSyncedAmount = currentAmount;
               this.lastSyncedFluid = currentFluid.isEmpty() ? FluidStack.EMPTY : currentFluid.copyWithAmount(1);
               this.setChanged();
               if (this.level instanceof ServerLevel level) {
                  Packet<?> packet = this.getUpdatePacket();
                  if (packet != null) {
                     for (ServerPlayer player : PlayerLookup.tracking(level, this.getBlockPos())) {
                        player.connection.send(packet);
                     }
                  }
               }
            }

            int compLevel = this.getComparatorLevel();
            if (compLevel != this.lastComparatorLevel) {
               this.lastComparatorLevel = compLevel;
               this.setChanged();
               this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            }
         } finally {
            _profiler.pop();
         }
      }
   }

   public void clientTick() {
      this.smoothedTank.tick();
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      String contents = !this.fluidTank.isEmpty() ? "Fluid" : "Empty";
      left.add("fluid = " + FluidDisplayNames.debugString(this.fluidTank.getFluidStack()));
      left.add("current = " + this.fluidTank.getAmountMb() + " of " + contents);
      left.add("lastSent = " + this.lastSyncedAmount + " of " + (!this.fluidTank.isEmpty() ? "Something" : "Nothing"));
   }

   @Override
   public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
      if (this.smoothedTank != null) {
         this.smoothedTank.getDebugInfo(left, right, side);
         left.add("shown = " + (int)this.smoothedTank.getDisplayAmount() + ", target = " + this.fluidTank.getAmountMb());
      }
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void balanceTankFluids() {
      if (this.level != null && !this.level.isClientSide()) {
         List<TileTank> tanks = this.getTankColumn();
         FluidStack fluid = FluidStack.EMPTY;

         for (TileTank tile : tanks) {
            FluidStack held = tile.fluidTank.getFluidStack();
            if (!held.isEmpty()) {
               FluidStack heldIdentity = held.copyWithAmount(1);
               if (fluid.isEmpty()) {
                  fluid = heldIdentity;
               } else if (!FluidIdentity.areEquivalentFluidStacks(fluid, heldIdentity)) {
                  return;
               }
            }
         }

         if (!fluid.isEmpty()) {
            if (!fluid.isEmpty() && FluidAttributes.of(fluid.getFluid()).isLighterThanAir()) {
               TileTank prev = null;

               for (int i = tanks.size() - 1; i >= 0; i--) {
                  TileTank tile = tanks.get(i);
                  if (prev != null) {
                     FluidStorageOps.moveReturningStack(tile.fluidTank, prev.fluidTank, Integer.MAX_VALUE);
                  }

                  prev = tile;
               }
            } else {
               TileTank prev = null;

               for (TileTank tile : tanks) {
                  if (prev != null) {
                     FluidStorageOps.moveReturningStack(tile.fluidTank, prev.fluidTank, Integer.MAX_VALUE);
                  }

                  prev = tile;
               }
            }

            for (TileTank tile : tanks) {
               tile.setChanged();
            }
         }
      }
   }

   public boolean canConnectTo(TileTank other, Direction direction) {
      return true;
   }

   public static boolean canTanksConnect(TileTank from, TileTank to, Direction direction) {
      return from.canConnectTo(to, direction) && to.canConnectTo(from, direction.getOpposite());
   }

   public List<TileTank> getTankColumn() {
      if (this.level == null) {
         return Collections.singletonList(this);
      }

      Deque<TileTank> tanks = new ArrayDeque<>();
      tanks.add(this);
      TileTank prevTank = this;

      while (this.level.getBlockEntity(prevTank.worldPosition.above()) instanceof TileTank tankUp && canTanksConnect(prevTank, tankUp, Direction.UP)) {
         tanks.addLast(tankUp);
         prevTank = tankUp;
      }

      prevTank = this;

      while (this.level.getBlockEntity(prevTank.worldPosition.below()) instanceof TileTank tankBelow && canTanksConnect(prevTank, tankBelow, Direction.DOWN)) {
         tanks.addFirst(tankBelow);
         prevTank = tankBelow;
      }

      return new ArrayList<>(tanks);
   }

   public TankColumnFluidStorage getColumnFluidStorage() {
      return new TankColumnFluidStorage(this);
   }

   public void requestColumnBalance() {
      this.pendingColumnBalance = true;
   }

   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      if (this.level != null && !this.level.isClientSide()) {
         this.dropSegmentContents(pos);
         this.notifyColumnNeighborsOnRemoval();
      }

      super.preRemoveSideEffects(pos, state);
   }

   private void dropSegmentContents(BlockPos pos) {
      if (this.fluidTank.isEmpty()) {
         return;
      }

      FluidStack held = this.fluidTank.getFluidStack();
      int amountMb = this.fluidTank.getAmountMb();
      NonNullList<ItemStack> toDrop = NonNullList.create();
      FluidItemDrops.addFluidDrops(toDrop, this.fluidTank);

      for (ItemStack drop : toDrop) {
         Block.popResource(this.level, pos, drop);
      }

      try (Transaction tx = Transaction.openOuter()) {
         this.fluidTank.extractMb(held, amountMb, tx);
         tx.commit();
      }
   }

   private void notifyColumnNeighborsOnRemoval() {
      if (this.level == null) {
         return;
      }

      if (this.level.getBlockEntity(this.worldPosition.above()) instanceof TileTank above) {
         above.requestColumnBalance();
      }

      if (this.level.getBlockEntity(this.worldPosition.below()) instanceof TileTank below) {
         below.requestColumnBalance();
      }
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.fluidTank.serialize(output);
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.fluidTank.deserialize(input);
      this.pendingColumnBalance = true;
   }

   public void onLoad() {
      if (this.level != null && !this.level.isClientSide()) {
         this.pendingColumnBalance = true;
      }
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftfactory.tank");
   }

   public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
      return new ContainerTank(containerId, playerInventory, this);
   }
}
