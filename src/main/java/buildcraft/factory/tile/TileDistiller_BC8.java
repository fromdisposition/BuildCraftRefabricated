/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.fluid.FluidSmoother;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.factory.BCFactoryAttachments;
import buildcraft.factory.FactoryFluidContainers;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.container.ContainerDistiller;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;

@SuppressWarnings("this-escape")
public class TileDistiller_BC8 extends BlockEntity implements MenuProvider, BlockEntityExtendedMenu, IDebuggable {

    public static final long MAX_MJ_PER_TICK = 6 * MjAPI.MJ;

    private static final Identifier ADVANCEMENT_HEATING_AND_DISTILLING =
        Identifier.parse("buildcraftfactory:heating_and_distilling");
    private static final Identifier ADVANCEMENT_REFINE_AND_REDEFINE =
        Identifier.parse("buildcraftenergy:refine_and_redefine");

    private final InputTank tankIn = new InputTank();
    private final OutputTank tankGasOut = new OutputTank();
    private final OutputTank tankLiquidOut = new OutputTank();

    private final MjBattery mjBattery = new MjBattery(1024 * MjAPI.MJ);
    private final IMjReceiver mjReceiver = new MjBatteryReceiver(mjBattery);

    public final buildcraft.lib.tile.item.ItemHandlerSimple containerSlots =
        new buildcraft.lib.tile.item.ItemHandlerSimple(3, 1);

    {
        containerSlots.setCallback((handler, slot, bef, aft) -> setChanged());
    }

    private final FluidSmoother smoothIn = new FluidSmoother(tankIn);
    private final FluidSmoother smoothGasOut = new FluidSmoother(tankGasOut);
    private final FluidSmoother smoothLiquidOut = new FluidSmoother(tankLiquidOut);

    private IDistillationRecipe currentRecipe;
    private long distillPower = 0;
    private boolean isActive = false;

    private boolean isStuck = false;

    private GameProfile owner;

    private boolean wasDistillingForAdvancement = false;

    private long powerAvgSmoothed = 0;
    private long powerAvgClient = 0;

    private double animState = 0;
    private double prevAnimState = 0;

    private int lastSyncedIn = -1;
    private int lastSyncedGas = -1;
    private int lastSyncedLiquid = -1;
    private FluidResource lastSyncedInResource = FluidResource.EMPTY;
    private FluidResource lastSyncedGasResource = FluidResource.EMPTY;
    private FluidResource lastSyncedLiquidResource = FluidResource.EMPTY;
    private boolean lastSyncedActive = false;
    private boolean lastSyncedStuck = false;
    private long lastSyncedPower = -1;

    public TileDistiller_BC8(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.DISTILLER, pos, state);
    }

    public InputTank getTankIn() {
        return tankIn;
    }

    public OutputTank getTankGasOut() {
        return tankGasOut;
    }

    public OutputTank getTankLiquidOut() {
        return tankLiquidOut;
    }

    public IMjReceiver getMjReceiver() {
        return mjReceiver;
    }

    public MjBattery getBattery() {
        return mjBattery;
    }

    @Nullable
    public GameProfile getOwner() {
        return owner;
    }

    public void onPlacedBy(@Nullable LivingEntity placer) {
        if (placer instanceof Player player) {
            owner = player.getGameProfile();
            setChanged();
            if (level != null && !level.isClientSide()) {
                MessageUtil.sendUpdateToTrackingPlayers(this);
            }
        }
    }

    @Nullable
    public FluidStacksResourceHandler getTankForSide(@Nullable Direction side) {
        if (side == null) return null;
        if (side == Direction.UP) return tankGasOut;
        if (side == Direction.DOWN) return tankLiquidOut;
        return tankIn;
    }

    public FluidSmoother getSmoothIn() {
        return smoothIn;
    }

    public FluidSmoother getSmoothGasOut() {
        return smoothGasOut;
    }

    public FluidSmoother getSmoothLiquidOut() {
        return smoothLiquidOut;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isStuck() {
        return isStuck;
    }

    public long getPowerAvgClient() {
        return powerAvgClient;
    }

    public double getAnimState() {
        return animState;
    }

    public double getPrevAnimState() {
        return prevAnimState;
    }

    public void clientTick() {
        smoothIn.tick();
        smoothGasOut.tick();
        smoothLiquidOut.tick();

        prevAnimState = animState;

        double changeSpeed = isActive && MAX_MJ_PER_TICK > 0
                ? ((double) powerAvgClient / MAX_MJ_PER_TICK) * 0.06
                : 0.01;
        if (isActive) {
            animState += changeSpeed;

            if (animState >= 1.5) {
                animState -= 1.0;
                prevAnimState -= 1.0;
            }
        } else {
            animState = animState > changeSpeed ? animState - changeSpeed : 0;
        }
    }

    private boolean isDistillableFluid(FluidStack fluid) {
        IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
        if (manager == null) return false;
        IDistillationRecipe recipe = manager.getDistillationRegistry().getRecipeForInput(fluid);
        return recipe != null;
    }

    public static boolean qualifiesForHeatingAdvancement(int inputHeat, boolean isNether) {
        if (inputHeat < 0) {
            return false;
        }
        int naturalHeat = isNether ? 2 : 0;
        return inputHeat != naturalHeat;
    }

    private void creditRefineAndRedefine(FluidStack outGas, FluidStack outLiquid) {
        if (owner == null || level == null || level.isClientSide()) return;
        net.minecraft.server.MinecraftServer server = level.getServer();
        if (server == null) return;
        net.minecraft.server.level.ServerPlayer player = server.getPlayerList().getPlayer(owner.id());
        if (player == null) return;
        var tracker = BCFactoryAttachments.get(player);
        String gasBase = BCEnergyFluidsFabric.getBaseName(outGas.getFluid());
        if (gasBase != null) {
            String justSaturated = tracker.recordProduction(gasBase, outGas.getAmount());
            if (justSaturated != null) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_REFINE_AND_REDEFINE, justSaturated);
            }
        }
        String liquidBase = BCEnergyFluidsFabric.getBaseName(outLiquid.getFluid());
        if (liquidBase != null) {
            String justSaturated = tracker.recordProduction(liquidBase, outLiquid.getAmount());
            if (justSaturated != null) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_REFINE_AND_REDEFINE, justSaturated);
            }
        }
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) return;

        if (level.getGameTime() % 5 == 0) {
            FactoryFluidContainers.syncDrainSlot(containerSlots, 0, tankIn);
            FactoryFluidContainers.syncFillSlot(containerSlots, 1, tankGasOut);
            FactoryFluidContainers.syncFillSlot(containerSlots, 2, tankLiquidOut);
        }

        mjBattery.tick(level, worldPosition);

        currentRecipe = null;
        IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
        if (manager != null) {
            FluidStack inFluid = tankIn.getResource(0).toStack(tankIn.getAmountAsInt(0));
            if (!inFluid.isEmpty()) {
                currentRecipe = manager.getDistillationRegistry().getRecipeForInput(inFluid);
            }
        }

        if (currentRecipe == null) {
            mjBattery.addPowerChecking(distillPower, false);
            distillPower = 0;
            isActive = false;
            isStuck = false;
        } else {
            FluidStack reqIn = currentRecipe.in();
            FluidStack outLiquid = currentRecipe.outLiquid();
            FluidStack outGas = currentRecipe.outGas();

            FluidResource resIn = tankIn.getResource(0);
            boolean canExtract = !resIn.isEmpty()
                    && buildcraft.lib.misc.FluidUtilBC.areEquivalentFluidResources(resIn, FluidResource.of(reqIn))
                    && tankIn.getAmountAsInt(0) >= reqIn.getAmount();

            boolean canFillLiquid;
            try (Transaction tx = Transaction.openRoot()) {
                canFillLiquid = tankLiquidOut.insertInternal(0, FluidResource.of(outLiquid), outLiquid.getAmount(), tx) >= outLiquid.getAmount();
            }
            boolean canFillGas;
            try (Transaction tx = Transaction.openRoot()) {
                canFillGas = tankGasOut.insertInternal(0, FluidResource.of(outGas), outGas.getAmount(), tx) >= outGas.getAmount();
            }

            isStuck = !canFillLiquid || !canFillGas;

            if (canExtract && canFillLiquid && canFillGas) {
                long max = MAX_MJ_PER_TICK;
                max *= mjBattery.getStored() + max;
                max /= mjBattery.getCapacity() / 2;
                max = Math.min(max, MAX_MJ_PER_TICK);
                long power = mjBattery.extractPower(0, max);

                powerAvgSmoothed += (long) ((max - powerAvgSmoothed) * 0.05);
                distillPower += power;
                isActive = power > 0;
                long powerReq = currentRecipe.powerRequired();
                if (distillPower >= powerReq) {
                    isActive = true;
                    distillPower -= powerReq;
                    try (Transaction tx = Transaction.openRoot()) {
                        tankIn.extractInternal(0, resIn, reqIn.getAmount(), tx);
                        tankGasOut.insertInternal(0, FluidResource.of(outGas), outGas.getAmount(), tx);
                        tankLiquidOut.insertInternal(0, FluidResource.of(outLiquid), outLiquid.getAmount(), tx);
                        tx.commit();
                    }
                    creditRefineAndRedefine(outGas, outLiquid);
                }
            } else {
                mjBattery.addPowerChecking(distillPower, false);
                distillPower = 0;
                isActive = false;
            }
        }

        boolean distilling = isActive && currentRecipe != null;
        if (distilling && !wasDistillingForAdvancement && owner != null) {
            int inputHeat = BCEnergyFluidsFabric.getHeat(currentRecipe.in().getFluid());
            if (qualifiesForHeatingAdvancement(inputHeat, level.dimension() == Level.NETHER)) {
                AdvancementUtil.unlockAdvancement(owner.id(), level, ADVANCEMENT_HEATING_AND_DISTILLING);
            }
        }
        wasDistillingForAdvancement = distilling;

        if (currentRecipe == null || !isActive) {
            powerAvgSmoothed += (long) ((0 - powerAvgSmoothed) * 0.05);
        }

        long halfMJ = MjAPI.MJ / 2;
        powerAvgClient = Math.round(powerAvgSmoothed / (double) halfMJ) * halfMJ;
        powerAvgClient = Math.min(powerAvgClient, MAX_MJ_PER_TICK);

        int curIn = tankIn.getAmountAsInt(0);
        int curGas = tankGasOut.getAmountAsInt(0);
        int curLiq = tankLiquidOut.getAmountAsInt(0);
        FluidResource curInResource = tankIn.getResource(0);
        FluidResource curGasResource = tankGasOut.getResource(0);
        FluidResource curLiqResource = tankLiquidOut.getResource(0);
        boolean needsSync = curIn != lastSyncedIn || curGas != lastSyncedGas || curLiq != lastSyncedLiquid
                || !buildcraft.lib.misc.FluidUtilBC.areEquivalentFluidResources(curInResource, lastSyncedInResource)
                || !buildcraft.lib.misc.FluidUtilBC.areEquivalentFluidResources(curGasResource, lastSyncedGasResource)
                || !buildcraft.lib.misc.FluidUtilBC.areEquivalentFluidResources(curLiqResource, lastSyncedLiquidResource)
                || isActive != lastSyncedActive || isStuck != lastSyncedStuck || powerAvgClient != lastSyncedPower;
        if (needsSync) {
            lastSyncedIn = curIn;
            lastSyncedGas = curGas;
            lastSyncedLiquid = curLiq;
            lastSyncedInResource = curInResource;
            lastSyncedGasResource = curGasResource;
            lastSyncedLiquidResource = curLiqResource;
            lastSyncedActive = isActive;
            lastSyncedStuck = isStuck;
            lastSyncedPower = powerAvgClient;
            setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
        }
    }

    @Override
    public void getDebugInfo(java.util.List<String> left, java.util.List<String> right, Direction side) {

        left.add("In = " + buildcraft.lib.misc.FluidUtilBC.getDebugString(tankIn.getResource(0).toStack(tankIn.getAmountAsInt(0))));
        left.add("GasOut = " + buildcraft.lib.misc.FluidUtilBC.getDebugString(tankGasOut.getResource(0).toStack(tankGasOut.getAmountAsInt(0))));
        left.add("LiquidOut = " + buildcraft.lib.misc.FluidUtilBC.getDebugString(tankLiquidOut.getResource(0).toStack(tankLiquidOut.getAmountAsInt(0))));
        left.add("Battery = " + mjBattery.getDebugString());
        left.add("Progress = " + MjAPI.formatMj(distillPower));
        left.add("Rate = " + buildcraft.lib.misc.LocaleUtil.localizeMjFlow(powerAvgClient));
        left.add("CurrRecipe = " + currentRecipe);
    }

    @Override
    public void getClientDebugInfo(java.util.List<String> left, java.util.List<String> right, Direction side) {
        if (smoothIn != null) smoothIn.getDebugInfo(left, right, side);
        if (smoothGasOut != null) smoothGasOut.getDebugInfo(left, right, side);
        if (smoothLiquidOut != null) smoothLiquidOut.getDebugInfo(left, right, side);

        Direction facing = Direction.WEST;
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(buildcraft.factory.block.BlockDistiller.FACING)) {
                facing = state.getValue(buildcraft.factory.block.BlockDistiller.FACING);
            }
        }
        left.add("Model Variables:");
        left.add("  facing = " + facing);
        left.add("  active = " + isActive);
        left.add("  power_average = " + (powerAvgClient / MjAPI.MJ));
        left.add("  power_max = " + (MAX_MJ_PER_TICK / MjAPI.MJ));
        left.add("Current Model Variables:");
        left.add("  animState = " + String.format("%.4f", animState));
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftfactory.distiller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new ContainerDistiller(containerId, playerInv, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (owner != null && owner.id() != null) {
            output.putString("ownerUUID", owner.id().toString());
            if (owner.name() != null) {
                output.putString("ownerName", owner.name());
            }
        }

        if (!tankIn.getResource(0).isEmpty()) {
            output.store("fluidIn", FluidStack.CODEC, tankIn.getResource(0).toStack(tankIn.getAmountAsInt(0)));
        }
        if (!tankGasOut.getResource(0).isEmpty()) {
            output.store("fluidGasOut", FluidStack.CODEC, tankGasOut.getResource(0).toStack(tankGasOut.getAmountAsInt(0)));
        }
        if (!tankLiquidOut.getResource(0).isEmpty()) {
            output.store("fluidLiquidOut", FluidStack.CODEC, tankLiquidOut.getResource(0).toStack(tankLiquidOut.getAmountAsInt(0)));
        }
        output.putLong("mjStored", mjBattery.getStored());
        output.putLong("distillPower", distillPower);
        output.putBoolean("isActive", isActive);
        output.putBoolean("isStuck", isStuck);
        output.putLong("powerAvgClient", powerAvgClient);
        output.store("containerSlots", net.minecraft.nbt.CompoundTag.CODEC, containerSlots.serializeNBT());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String ownerUuid = input.getStringOr("ownerUUID", "");
        if (!ownerUuid.isEmpty()) {
            try {
                owner = new GameProfile(UUID.fromString(ownerUuid), input.getStringOr("ownerName", "Unknown"));
            } catch (IllegalArgumentException e) {
                owner = null;
            }
        }
        loadTank(tankIn, input, "fluidIn");
        loadTank(tankGasOut, input, "fluidGasOut");
        loadTank(tankLiquidOut, input, "fluidLiquidOut");
        mjBattery.addPowerChecking(input.getLongOr("mjStored", 0L), false);
        distillPower = input.getLongOr("distillPower", 0L);
        isActive = input.getBooleanOr("isActive", false);
        isStuck = input.getBooleanOr("isStuck", false);
        powerAvgClient = input.getLongOr("powerAvgClient", 0L);
        containerSlots.deserializeNBT(input.read("containerSlots", net.minecraft.nbt.CompoundTag.CODEC).orElseGet(net.minecraft.nbt.CompoundTag::new));
    }

    private static void loadTank(FluidStacksResourceHandler tank, ValueInput input, String key) {
        FluidStack fluid = input.read(key, FluidStack.CODEC).orElse(FluidStack.EMPTY);
        if (fluid.isEmpty()) {
            tank.set(0, FluidResource.EMPTY, 0);
        } else {
            tank.set(0, FluidResource.of(fluid), fluid.getAmount());
        }
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public class InputTank extends FluidStacksResourceHandler {
        private boolean internalExtract = false;

        public InputTank() {
            super(1, 4000);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {

            return isDistillableFluid(resource.toStack(1));
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
            return internalExtract ? super.extract(index, resource, amount, tx) : 0;
        }

        public int extractInternal(int index, FluidResource resource, int amount, TransactionContext tx) {
            internalExtract = true;
            try {
                return super.extract(index, resource, amount, tx);
            } finally {
                internalExtract = false;
            }
        }
    }

    public static class OutputTank extends FluidStacksResourceHandler {
        private boolean internalInsert = false;

        public OutputTank() {
            super(1, 4000);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return internalInsert;
        }

        public int insertInternal(int index, FluidResource resource, int amount, TransactionContext tx) {
            internalInsert = true;
            try {
                return super.insert(index, resource, amount, tx);
            } finally {
                internalInsert = false;
            }
        }
    }
}
