/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.container.ContainerTank;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.tile.IBlockEntityLoadHook;
import buildcraft.api.tiles.IDebuggable;

@SuppressWarnings("deprecation")
public class TileTank extends BlockEntity implements MenuProvider, BlockEntityExtendedMenu, IDebuggable, IBlockEntityLoadHook {

    public final FluidStacksResourceHandler tank = new FluidStacksResourceHandler(1, 16_000);
    public final FluidSmoother smoothedTank = new FluidSmoother(tank);

    private int lastComparatorLevel;
    private int lastSyncedAmount = -1;
    private FluidResource lastSyncedResource = FluidResource.EMPTY;
    private boolean pendingColumnBalance;

    public TileTank(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.TANK, pos, state);
    }

    public int getComparatorLevel() {
        int amount = tank.getAmountAsInt(0);
        int cap = tank.getCapacityAsInt(0, FluidResource.EMPTY);
        return amount * 14 / cap + (amount > 0 ? 1 : 0);
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) return;
        ProfilerFiller _profiler = Profiler.get();
        _profiler.push("buildcraft:tank_serverTick");
        try {
        if (pendingColumnBalance) {
            pendingColumnBalance = false;
            balanceTankFluids();
        }

        int currentAmount = tank.getAmountAsInt(0);
        FluidResource currentResource = tank.getResource(0);

        if (currentAmount != lastSyncedAmount
                || !FluidUtilBC.areEquivalentFluidResources(currentResource, lastSyncedResource)) {
            lastSyncedAmount = currentAmount;
            lastSyncedResource = currentResource;
            setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
        }

        int compLevel = getComparatorLevel();
        if (compLevel != lastComparatorLevel) {
            lastComparatorLevel = compLevel;
            setChanged();
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
        } finally {
            _profiler.pop();
        }
    }

    public void clientTick() {
        smoothedTank.tick();
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        String contents = (!tank.getResource(0).isEmpty()) ? "Fluid" : "Empty";
        left.add("fluid = " + buildcraft.lib.misc.FluidUtilBC.getDebugString(tank.getResource(0).toStack(tank.getAmountAsInt(0))));
        left.add("current = " + tank.getAmountAsInt(0) + " of " + contents);
        left.add("lastSent = " + lastSyncedAmount + " of " + ((!tank.getResource(0).isEmpty()) ? "Something" : "Nothing"));
    }

    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
        if (smoothedTank != null) {
            smoothedTank.getDebugInfo(left, right, side);
            left.add("shown = " + (int) smoothedTank.getDisplayAmount() + ", target = " + tank.getAmountAsInt(0));
        }
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void balanceTankFluids() {
        if (level == null || level.isClientSide()) {
            return;
        }
        List<TileTank> tanks = getTankColumn();
        FluidResource fluid = FluidResource.EMPTY;
        for (TileTank tile : tanks) {
            FluidResource held = tile.tank.getResource(0);
            if (held.isEmpty()) continue;
            if (fluid.isEmpty()) {
                fluid = held;
            } else if (!FluidUtilBC.areEquivalentFluidResources(fluid, held)) {
                return;
            }
        }
        if (fluid.isEmpty()) return;

        if (FluidUtilBC.isGaseous(fluid.toStack(1))) {

            TileTank prev = null;
            for (int i = tanks.size() - 1; i >= 0; i--) {
                TileTank tile = tanks.get(i);
                if (prev != null) {
                    FluidUtilBC.move(tile.tank, prev.tank);
                }
                prev = tile;
            }
        } else {

            TileTank prev = null;
            for (TileTank tile : tanks) {
                if (prev != null) {
                    FluidUtilBC.move(tile.tank, prev.tank);
                }
                prev = tile;
            }
        }
        for (TileTank tile : tanks) {
            tile.setChanged();
        }
    }

    public boolean canConnectTo(TileTank other, Direction direction) {
        return true;
    }

    public static boolean canTanksConnect(TileTank from, TileTank to, Direction direction) {
        return from.canConnectTo(to, direction) && to.canConnectTo(from, direction.getOpposite());
    }

    public List<TileTank> getTankColumn() {
        if (level == null) {
            return Collections.singletonList(this);
        }
        Deque<TileTank> tanks = new ArrayDeque<>();
        tanks.add(this);

        TileTank prevTank = this;
        while (true) {
            BlockEntity tileAbove = level.getBlockEntity(prevTank.worldPosition.above());
            if (!(tileAbove instanceof TileTank tankUp)) break;
            if (canTanksConnect(prevTank, tankUp, Direction.UP)) {
                tanks.addLast(tankUp);
            } else {
                break;
            }
            prevTank = tankUp;
        }

        prevTank = this;
        while (true) {
            BlockEntity tileBelow = level.getBlockEntity(prevTank.worldPosition.below());
            if (!(tileBelow instanceof TileTank tankBelow)) break;
            if (canTanksConnect(prevTank, tankBelow, Direction.DOWN)) {
                tanks.addFirst(tankBelow);
            } else {
                break;
            }
            prevTank = tankBelow;
        }

        return new ArrayList<>(tanks);
    }

    public ResourceHandler<FluidResource> getColumnResourceHandler() {
        return new TankColumnResourceHandler(this);
    }

    public void requestColumnBalance() {
        pendingColumnBalance = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tank.deserialize(input);
        pendingColumnBalance = true;
    }

    @Override
    public void onLoad() {
        if (level != null && !level.isClientSide()) {
            pendingColumnBalance = true;
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("stacks");
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftfactory.tank");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ContainerTank(containerId, playerInventory, this);
    }
}
