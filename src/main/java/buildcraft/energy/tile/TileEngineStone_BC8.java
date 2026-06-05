/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy.tile;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.item.ItemStackResourceHandler;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;

import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import buildcraft.lib.misc.AdvancementUtil;

@SuppressWarnings("deprecation")
public class TileEngineStone_BC8 extends TileEngineBase_BC8 implements MenuProvider, BlockEntityExtendedMenu {
    private static final net.minecraft.resources.Identifier ADVANCEMENT_POWERING_UP
        = net.minecraft.resources.Identifier.parse("buildcraftenergy:powering_up");
    private static final net.minecraft.resources.Identifier ADVANCEMENT_LAVA_POWER
        = net.minecraft.resources.Identifier.parse("buildcraftenergy:lava_power");

    private static final long MAX_OUTPUT = MjAPI.MJ;
    private static final long MIN_OUTPUT = MAX_OUTPUT / 3;
    private static final long eLimit = (MAX_OUTPUT - MIN_OUTPUT) * 20;

    public int burnTime = 0;
    public int totalBurnTime = 0;
    private long esum = 0;

    private ItemStack fuelStack = ItemStack.EMPTY;

    public final ItemStackResourceHandler fuelItemHandler = new ItemStackResourceHandler() {
        @Override
        protected ItemStack getStack() {
            return fuelStack;
        }

        @Override
        protected void setStack(ItemStack stack) {
            fuelStack = stack;
        }

        @Override
        protected boolean isValid(ItemResource resource) {

            return isValidFuel(resource.toStack(1));
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            setChanged();
        }
    };

    public TileEngineStone_BC8(BlockPos pos, BlockState state) {
        super(BCEnergyBlockEntities.ENGINE_STONE, pos, state);
    }

    @Nonnull
    public ItemStack getFuelStack() {
        return fuelStack;
    }

    public void setFuelStack(@Nonnull ItemStack stack) {
        fuelStack = stack;
        setChanged();
    }

    public boolean isValidFuel(@Nonnull ItemStack stack) {
        return getBurnTime(stack) > 0;
    }

    private int getBurnTime(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || level == null) return 0;
        return level.fuelValues().burnDuration(stack);
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        return burnTime > 0;
    }

    @Override
    protected void engineUpdate() {

        if (burnTime > 0) {
            burnTime--;
            if (getPowerStage() != EnumPowerStage.OVERHEAT) {
                long output = getCurrentOutput();
                addPower(output);
            }
        }

        if (burnTime == 0 && isRedstonePowered) {
            int newBurn = getBurnTime(fuelStack);
            if (newBurn > 0) {
                burnTime = newBurn;
                totalBurnTime = newBurn;
                if (getOwner() != null && level != null) {
                    AdvancementUtil.unlockAdvancement(getOwner().id(), level, ADVANCEMENT_POWERING_UP);
                    if (fuelStack.getItem() == net.minecraft.world.item.Items.LAVA_BUCKET) {
                        AdvancementUtil.unlockAdvancement(getOwner().id(), level, ADVANCEMENT_LAVA_POWER);
                    }
                }

                ItemStack consumed = fuelStack.copy();
                consumed.setCount(1);

                fuelStack.shrink(1);
                if (fuelStack.isEmpty()) {
                    fuelStack = ItemStack.EMPTY;
                }

                net.minecraft.world.item.ItemStackTemplate containerTemplate = consumed.getItem().getCraftingRemainder();
                ItemStack container = containerTemplate != null ? containerTemplate.create() : ItemStack.EMPTY;

                if (!container.isEmpty()) {
                    if (fuelStack.isEmpty()) {
                        fuelStack = container;
                    } else {

                        if (level != null) {
                            net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                                level,
                                getBlockPos().getX() + 0.5,
                                getBlockPos().getY() + 1.0,
                                getBlockPos().getZ() + 0.5,
                                container
                            );
                            level.addFreshEntity(entity);
                        }
                    }
                }
                setChanged();
            }
        }
    }

    protected void addPower(long microMj) {
        power = Math.min(power + microMj, getMaxPower());
    }

    @Override
    public long maxPowerReceived() {
        return 200 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 100 * MjAPI.MJ;
    }

    @Override
    public long getMaxPower() {
        return 1000 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 2;
    }

    @Override
    public long getCurrentOutput() {

        long e = 3 * getMaxPower() / 8 - power;
        esum = clamp(esum + e, -eLimit, eLimit);
        return clamp(e + esum / 20, MIN_OUTPUT, MAX_OUTPUT);
    }

    @Override
    public long minPowerReceived() {
        return MjAPI.MJ / 10;
    }

    private static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("burnTime", burnTime);
        output.putInt("totalBurnTime", totalBurnTime);
        output.putLong("esum", esum);
        if (!fuelStack.isEmpty()) {
            net.minecraft.resources.Identifier itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(fuelStack.getItem());
            output.putString("fuelId", itemId.toString());
            output.putInt("fuelCount", fuelStack.getCount());
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        burnTime = input.getIntOr("burnTime", 0);
        totalBurnTime = input.getIntOr("totalBurnTime", 0);
        esum = input.getLongOr("esum", 0L);
        String fuelId = input.getStringOr("fuelId", "");
        if (!fuelId.isEmpty()) {
            net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.tryParse(fuelId);
            if (id != null) {
                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                int count = input.getIntOr("fuelCount", 1);
                if (item != null && item != net.minecraft.world.item.Items.AIR) {
                    fuelStack = new ItemStack(item, count);
                } else {
                    fuelStack = ItemStack.EMPTY;
                }
            }
        } else {
            fuelStack = ItemStack.EMPTY;
        }
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftenergy.engine_stone");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new buildcraft.energy.container.ContainerEngineStone_BC8(containerId, playerInv, this);
    }
}
