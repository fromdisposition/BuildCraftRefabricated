/* Copyright (c) 2017 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.engine.TileEngineBase_BC8;

@SuppressWarnings("this-escape")
public class ContainerEngineStone_BC8 extends ContainerBC_Neptune {
    public final TileEngineStone_BC8 engine;
    private final ContainerData data;

    private static final int DATA_BURN_TIME = 0;
    private static final int DATA_TOTAL_BURN_TIME = 1;
    private static final int DATA_POWER_HI = 2;
    private static final int DATA_POWER_LO = 3;
    private static final int DATA_HEAT = 4;
    private static final int DATA_OUTPUT_HI = 5;
    private static final int DATA_OUTPUT_LO = 6;
    private static final int DATA_POWER_STAGE = 7;
    private static final int DATA_IS_BURNING_ENGINE = 8;
    private static final int DATA_COUNT = 9;

    public ContainerEngineStone_BC8(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileEngineStone_BC8.class));
    }

    public ContainerEngineStone_BC8(int containerId, Inventory playerInv, TileEngineStone_BC8 engine) {
        super(BCEnergyMenuTypes.ENGINE_STONE, containerId, playerInv.player);
        this.engine = engine;

        if (engine != null && engine.getLevel() != null && !engine.getLevel().isClientSide()) {
            this.data = new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case DATA_BURN_TIME -> engine.burnTime;
                        case DATA_TOTAL_BURN_TIME -> engine.totalBurnTime;
                        case DATA_POWER_HI -> (int) (engine.getPower() >>> 32);
                        case DATA_POWER_LO -> (int) (engine.getPower() & 0xFFFFFFFFL);
                        case DATA_HEAT -> Float.floatToIntBits(engine.getHeat());
                        case DATA_OUTPUT_HI -> (int) (engine.currentOutput >>> 32);
                        case DATA_OUTPUT_LO -> (int) (engine.currentOutput & 0xFFFFFFFFL);
                        case DATA_POWER_STAGE -> engine.getPowerStage().ordinal();
                        case DATA_IS_BURNING_ENGINE -> engine.isBurning() ? 1 : 0;
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {
                    switch (index) {
                        case DATA_BURN_TIME -> engine.burnTime = value;
                        case DATA_TOTAL_BURN_TIME -> engine.totalBurnTime = value;

                    }
                }

                @Override
                public int getCount() {
                    return DATA_COUNT;
                }
            };
        } else {
            SimpleContainerData clientData = new SimpleContainerData(DATA_COUNT);

            clientData.set(DATA_HEAT, Float.floatToIntBits(TileEngineBase_BC8.MIN_HEAT));
            this.data = clientData;
        }

        addDataSlots(this.data);

        addSlot(new FuelSlot(engine, 0, 80, 41));

        addFullPlayerInventory(8, 84, playerInv);
    }

    public int getBurnTime() {
        return data.get(DATA_BURN_TIME);
    }

    public int getTotalBurnTime() {
        return data.get(DATA_TOTAL_BURN_TIME);
    }

    public boolean isBurning() {
        return getBurnTime() > 0;
    }

    public float getBurnProgress() {
        int total = getTotalBurnTime();
        if (total <= 0) return 0;
        return (float) getBurnTime() / total;
    }

    public long getSyncedPower() {
        return ((long) data.get(DATA_POWER_HI) << 32) | (data.get(DATA_POWER_LO) & 0xFFFFFFFFL);
    }

    public float getSyncedHeat() {
        return Float.intBitsToFloat(data.get(DATA_HEAT));
    }

    public buildcraft.api.enums.EnumPowerStage getSyncedPowerStage() {
        int ordinal = data.get(DATA_POWER_STAGE);
        buildcraft.api.enums.EnumPowerStage[] values = buildcraft.api.enums.EnumPowerStage.values();
        if (ordinal >= 0 && ordinal < values.length) return values[ordinal];
        return buildcraft.api.enums.EnumPowerStage.BLUE;
    }

    public boolean isSyncedBurningEngine() {
        return data.get(DATA_IS_BURNING_ENGINE) != 0;
    }

    public long getSyncedCurrentOutput() {
        return ((long) data.get(DATA_OUTPUT_HI) << 32) | (data.get(DATA_OUTPUT_LO) & 0xFFFFFFFFL);
    }

    @Override
    public boolean stillValid(Player player) {
        if (engine == null || engine.isRemoved()) return false;
        return player.distanceToSqr(
            engine.getBlockPos().getX() + 0.5,
            engine.getBlockPos().getY() + 0.5,
            engine.getBlockPos().getZ() + 0.5
        ) <= 64.0;
    }

    private static class FuelSlot extends Slot {
        private final TileEngineStone_BC8 engine;

        public FuelSlot(TileEngineStone_BC8 engine, int index, int x, int y) {
            super(new FuelContainer(engine), index, x, y);
            this.engine = engine;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return engine != null && engine.isValidFuel(stack);
        }
    }

    private static class FuelContainer implements net.minecraft.world.Container {
        private final TileEngineStone_BC8 engine;

        FuelContainer(TileEngineStone_BC8 engine) {
            this.engine = engine;
        }

        @Override
        public int getContainerSize() { return 1; }

        @Override
        public boolean isEmpty() {
            return engine == null || engine.getFuelStack().isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return engine != null ? engine.getFuelStack() : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            if (engine == null) return ItemStack.EMPTY;
            ItemStack stack = engine.getFuelStack();
            if (stack.isEmpty()) return ItemStack.EMPTY;
            ItemStack result = stack.split(count);
            if (stack.isEmpty()) engine.setFuelStack(ItemStack.EMPTY);
            else engine.setFuelStack(stack);
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (engine == null) return ItemStack.EMPTY;
            ItemStack stack = engine.getFuelStack();
            engine.setFuelStack(ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (engine != null) engine.setFuelStack(stack);
        }

        @Override
        public void setChanged() {
            if (engine != null) engine.setChanged();
        }

        @Override
        public boolean stillValid(Player player) { return true; }

        @Override
        public void clearContent() {
            if (engine != null) engine.setFuelStack(ItemStack.EMPTY);
        }
    }
}
