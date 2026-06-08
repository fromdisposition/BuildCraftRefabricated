/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.container.ContainerTank;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fabric.transfer.TankColumnFluidStorage;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.IBlockEntityLoadHook;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
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

public class TileTank extends BlockEntity implements MenuProvider, BlockEntityExtendedMenu, IDebuggable, IBlockEntityLoadHook {
   public final SingleFluidTank fluidTank = new SingleFluidTank(16000);
   public final FluidSmoother smoothedTank = new FluidSmoother(this.fluidTank);
   private int lastComparatorLevel;
   private int lastSyncedAmount = -1;
   private FluidStack lastSyncedFluid = FluidStack.EMPTY;
   private boolean pendingColumnBalance;

   public TileTank(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.TANK, pos, state);
   }

   public int getComparatorLevel() {
      int amount = this.fluidTank.getAmountMb();
      int cap = this.fluidTank.getCapacityMb();
      return amount * 14 / cap + (amount > 0 ? 1 : 0);
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
               || !FluidUtilBC.areEquivalentFluidStacks(currentFluid.isEmpty() ? FluidStack.EMPTY : currentFluid.copyWithAmount(1), this.lastSyncedFluid)) {
               this.lastSyncedAmount = currentAmount;
               this.lastSyncedFluid = currentFluid.isEmpty() ? FluidStack.EMPTY : currentFluid.copyWithAmount(1);
               this.setChanged();
               MessageUtil.sendUpdateToTrackingPlayers(this);
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
      left.add("fluid = " + FluidUtilBC.getDebugString(this.fluidTank.getFluidStack()));
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
               } else if (!FluidUtilBC.areEquivalentFluidStacks(fluid, heldIdentity)) {
                  return;
               }
            }
         }

         if (!fluid.isEmpty()) {
            if (FluidUtilBC.isGaseous(fluid)) {
               TileTank prev = null;

               for (int i = tanks.size() - 1; i >= 0; i--) {
                  TileTank tile = tanks.get(i);
                  if (prev != null) {
                     FluidUtilBC.move(tile.fluidTank, prev.fluidTank);
                  }

                  prev = tile;
               }
            } else {
               TileTank prev = null;

               for (TileTank tile : tanks) {
                  if (prev != null) {
                     FluidUtilBC.move(tile.fluidTank, prev.fluidTank);
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

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.fluidTank.serialize(output);
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.fluidTank.deserialize(input);
      this.pendingColumnBalance = true;
   }

   @Override
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
