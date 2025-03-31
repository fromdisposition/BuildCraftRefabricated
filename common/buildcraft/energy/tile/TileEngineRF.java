/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.tile;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IItemPipe;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.lib.tile.item.StackInsertionFunction;

import buildcraft.core.BCCoreItems;
import buildcraft.energy.BCEnergyGuis;

public class TileEngineRF extends TileEngineBase_BC8 {
    public static final int MAX_RF = 10_000;
    public static final double HEAT_RATE = 0.06;
    public static final double COOLDOWN_RATE = 0.01;

    public static final Map<Item, Long> RF_UPGRADE = new LinkedHashMap<>();

    static {
        RF_UPGRADE.put(BCCoreItems.gearIron, MjAPI.MJ * 2);
        RF_UPGRADE.put(BCCoreItems.gearGold, MjAPI.MJ * 3);
    }

    int currentRF;
    public final ItemHandlerSimple invUpgrades;

    public TileEngineRF() {
        caps.addCapabilityInstance(CapabilityEnergy.ENERGY, new Rf(), EnumPipePart.VALUES);
        invUpgrades = itemManager.addInvHandler("upgrades", 4, this::isValidUpgrade, StackInsertionFunction.getInsertionFunction(1), EnumAccess.NONE);
    }

    // TileEntity overrides

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("currentRF", currentRF);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        currentRF = nbt.getInteger("currentRF");
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                currentRF = buffer.readInt();
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                buffer.writeInt(currentRF);
            }
        }
    }

    protected boolean isValidUpgrade(int slot, ItemStack stack) {
        Item item = stack.getItem();
        return RF_UPGRADE.containsKey(item);
    }

    // TileEngineBase overrides

    @Override
    public boolean onActivated(
        EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ
    ) {
        ItemStack current = player.getHeldItem(hand).copy();
        if (super.onActivated(player, hand, side, hitX, hitY, hitZ)) {
            return true;
        }
        if (!current.isEmpty()) {
            if (EntityUtil.getWrenchHand(player) != null) {
                return false;
            }
            if (current.getItem() instanceof IItemPipe) {
                return false;
            }
        }
        if (!world.isRemote) {
            BCEnergyGuis.ENGINE_RF.openGUI(player, getPos());
        }
        return true;
    }

    @Override
    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.04;
            case GREEN:
                return 0.05;
            case YELLOW:
                return 0.06;
            case RED:
                return 0.07;
            default:
                return 0;
        }
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        return currentRF > 0 && isRedstonePowered;
    }

    public long getMjPerTick() {
        long value = MjAPI.MJ * 4;
        for (int slot = 0; slot < invUpgrades.getSlots(); slot++) {
            ItemStack stack = invUpgrades.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Long add = RF_UPGRADE.get(stack.getItem());
            if (add != null) {
                value += add;
            }
        }
        return value;
    }

    public int getRfConsumptionRate() {

        final long mjPerTick = getMjPerTick();
        long mjPerRf = BCLibConfig.mjRfConversion.mjPerRf;

        return (int) (mjPerTick / mjPerRf);
    }

    @Override
    protected void burn() {
        if (currentRF <= 0) {
            return;
        }

        if (isRedstonePowered) {
            long mjPerRf = BCLibConfig.mjRfConversion.mjPerRf;
            int maxRf = getRfConsumptionRate();

            int rfConsumed = Math.min(currentRF, maxRf);
            long mjGenerated = rfConsumed * mjPerRf;

            if (power + mjGenerated >= getMaxPower()) {
                return;
            }

            currentOutput = mjGenerated;
            addPower(mjGenerated);
            currentRF -= rfConsumed;
            heat += HEAT_RATE;
            if (heat >= 200) {
                heat = 200;
            }
        }
    }

    @Override
    public void updateHeatLevel() {

        if (heat > MIN_HEAT) {
            heat -= COOLDOWN_RATE;
        }

        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }

        getPowerStage();
    }

    @Override
    public long getMaxPower() {
        return 1000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerReceived() {
        return 200 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 500 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 4;
    }

    @Override
    protected int getMaxChainLength() {
        return 4;
    }

    @Override
    public long getCurrentOutput() {
        if (currentRF > 0) {
            return getMjPerTick();
        } else {
            return 0;
        }
    }

    public int getCurrentRF() {
        return currentRF;
    }

    private final class Rf implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int max = Math.min(MAX_RF - currentRF, maxReceive);
            if (max <= 0) {
                return 0;
            }

            if (!simulate) {
                currentRF += max;
            }
            return max;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return currentRF;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_RF;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
