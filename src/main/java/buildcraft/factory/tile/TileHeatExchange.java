/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.recipes.IRefineryRecipeManager.ICoolableRecipe;
import buildcraft.api.recipes.IRefineryRecipeManager.IHeatableRecipe;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.items.FluidItemDrops;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.FactoryFluidContainers;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.block.BlockHeatExchange.EnumExchangePart;
import buildcraft.factory.container.ContainerHeatExchange;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import net.minecraft.server.level.ServerPlayer;
import buildcraft.lib.fluid.FluidSmoother;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;

@SuppressWarnings("this-escape")
public class TileHeatExchange extends BlockEntity implements MenuProvider, BlockEntityExtendedMenu, IDebuggable {

    private static final int[] FLUID_MULT = { 5, 10, 20 };

    protected ExchangeSection section;
    private boolean checkNeighbours;

    private int lastSyncHash = 0;

    public final ItemHandlerSimple containerSlots = new ItemHandlerSimple(4, 1);

    {
        containerSlots.setCallback((handler, slot, bef, aft) -> setChanged());
    }

    public TileHeatExchange(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.HEAT_EXCHANGE, pos, state);
    }

    public boolean isStart() {
        return section instanceof ExchangeSectionStart;
    }

    public boolean isEnd() {
        return section instanceof ExchangeSectionEnd;
    }

    @Nullable
    public ExchangeSection getSection() {
        return section;
    }

    @Nullable
    public FluidStacksResourceHandler getFluidTankForDirection(@Nullable Direction direction) {
        if (section == null || direction == null) return null;
        Direction facing = getFacing();
        if (facing == null) return null;

        if (section instanceof ExchangeSectionStart) {
            if (direction == Direction.DOWN) {
                return section.tankInput;
            }
            if (direction == facing.getClockWise()) {
                return section.tankOutput;
            }
        } else if (section instanceof ExchangeSectionEnd) {
            if (direction == Direction.UP) {
                return section.tankOutput;
            }
            if (direction == facing.getCounterClockWise()) {
                return section.tankInput;
            }
        }
        return null;
    }

    public void markCheckNeighbours() {
        checkNeighbours = true;
    }

    @Nullable
    public TileHeatExchange findStart() {
        if (isStart()) return this;
        if (level == null) return null;
        Direction facing = getFacing();
        if (facing == null) return null;
        Direction dirToStart = facing.getClockWise();
        for (int i = 1; i < 6; i++) {
            BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(dirToStart, i));
            if (neighbour instanceof TileHeatExchange other) {
                if (other.getFacing() != facing) {
                    return null;
                }
                if (other.isStart()) {
                    return other;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    @Nullable
    Direction getFacing() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof BlockHeatExchange) {
            return state.getValue(BlockHeatExchange.FACING);
        }
        return null;
    }

    public void serverTick() {
        if (level == null) return;
        if (checkNeighbours) {
            checkNeighbours = false;
            Deque<TileHeatExchange> exchangers = findAdjacentExchangers();
            if (exchangers.isEmpty()) {
                checkNeighbours = true;
            } else if (exchangers.size() < 3) {
                for (TileHeatExchange tile : exchangers) {
                    tile.removeSection();
                }
            } else if (exchangers.size() <= 5) {
                ExchangeSectionStart sectionStart = null;
                ExchangeSectionEnd sectionEnd = null;
                for (TileHeatExchange exchange : exchangers) {
                    exchange.checkNeighbours = false;
                    if (exchange.section instanceof ExchangeSectionStart existingStart) {
                        if (sectionStart == null) {
                            sectionStart = existingStart;
                        }
                    } else if (exchange.section instanceof ExchangeSectionEnd existingEnd) {
                        if (sectionEnd == null) {
                            sectionEnd = existingEnd;
                        }
                    }
                    exchange.section = null;
                }
                if (sectionStart == null) {
                    sectionStart = new ExchangeSectionStart(exchangers.getFirst());
                }
                if (sectionEnd == null) {
                    sectionEnd = new ExchangeSectionEnd(exchangers.getLast());
                }
                sectionStart.endSection = sectionEnd;
                sectionStart.middleCount = exchangers.size() - 2;
                exchangers.getFirst().setSection(sectionStart);
                exchangers.getLast().setSection(sectionEnd);

                updatePartProperties(exchangers);

                for (TileHeatExchange exchange : exchangers) {
                    exchange.syncToClient();
                }
            }
        }
        if (section != null) {
            section.tick();
        }

        int hash = computeSyncHash();
        if (hash != lastSyncHash) {
            lastSyncHash = hash;
            syncToClient();
        }
    }

    public void clientTick() {
        if (level == null) return;
        if (checkNeighbours) {
            Deque<TileHeatExchange> exchangers = findAdjacentExchangers();

            if (exchangers.size() > 2) {
                TileHeatExchange start = exchangers.getFirst();
                TileHeatExchange end = exchangers.getLast();
                if (start.isStart() && end.isEnd()) {
                    ((ExchangeSectionStart) start.section).endSection = (ExchangeSectionEnd) end.section;
                    checkNeighbours = false;
                }
            }
        }
        if (section != null) {
            section.clientTick();
        }
    }

    private void removeSection() {
        if (section == null) return;
        if (level != null && !level.isClientSide()) {
            NonNullList<ItemStack> toDrop = NonNullList.create();
            FluidItemDrops.addFluidDrops(toDrop, section.tankInput, section.tankOutput);
            for (ItemStack drop : toDrop) {
                Block.popResource(level, worldPosition, drop);
            }
        }
        section = null;

        if (level != null) {
            BlockState oldState = getBlockState();
            if (oldState.getBlock() instanceof BlockHeatExchange) {
                BlockState newState = oldState.setValue(BlockHeatExchange.PART, EnumExchangePart.MIDDLE);
                if (oldState != newState) {
                    level.setBlock(worldPosition, newState, Block.UPDATE_ALL);
                }
            }
        }
        syncToClient();
    }

    private void setSection(ExchangeSection section) {
        if (this.section != section) {
            this.section = section;
            section.setTile(this);
            syncToClient();
        }
    }

    private void updatePartProperties(Deque<TileHeatExchange> exchangers) {
        if (level == null) return;
        TileHeatExchange[] arr = exchangers.toArray(new TileHeatExchange[0]);
        for (int i = 0; i < arr.length; i++) {
            TileHeatExchange tile = arr[i];
            EnumExchangePart part;
            if (i == 0) {
                part = EnumExchangePart.START;
            } else if (i == arr.length - 1) {
                part = EnumExchangePart.END;
            } else {
                part = EnumExchangePart.MIDDLE;
            }
            BlockState oldState = tile.getBlockState();
            if (oldState.getBlock() instanceof BlockHeatExchange) {
                BlockState newState = oldState.setValue(BlockHeatExchange.PART, part);
                if (oldState != newState) {
                    level.setBlock(tile.worldPosition, newState, Block.UPDATE_ALL);
                }
            }
        }
    }

    private Deque<TileHeatExchange> findAdjacentExchangers() {
        Direction thisFacing = getFacing();
        if (thisFacing == null) {
            return new ArrayDeque<>();
        }
        Direction dirToStart = thisFacing.getClockWise();
        Direction dirToEnd = thisFacing.getCounterClockWise();
        Deque<TileHeatExchange> exchangers = new ArrayDeque<>();
        exchangers.add(this);
        for (int i = 1; i < 6; i++) {
            BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(dirToStart, i));
            if (neighbour instanceof TileHeatExchange other) {
                if (other.getFacing() != thisFacing) {
                    break;
                }
                exchangers.addFirst(other);
            } else {
                break;
            }
        }
        for (int i = 1; i < 6; i++) {
            BlockEntity neighbour = level.getBlockEntity(worldPosition.relative(dirToEnd, i));
            if (neighbour instanceof TileHeatExchange other) {
                if (other.getFacing() != thisFacing) {
                    break;
                }
                exchangers.addLast(other);
            } else {
                break;
            }
        }
        return exchangers;
    }

    public boolean rotate() {
        Direction thisFacing = getFacing();
        if (thisFacing == null || level == null) return false;
        Deque<TileHeatExchange> exchangers = findAdjacentExchangers();
        if (exchangers.size() == 1) {

            Direction[] horizontals = { Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST };
            int idx = 0;
            for (int i = 0; i < horizontals.length; i++) {
                if (horizontals[i] == thisFacing) {
                    idx = i;
                    break;
                }
            }
            Direction next = horizontals[(idx + 1) % 4];
            level.setBlock(worldPosition, getBlockState().setValue(BlockHeatExchange.FACING, next),
                    Block.UPDATE_ALL);
        } else {

            ExchangeSectionStart start = null;
            ExchangeSectionEnd end = null;
            for (TileHeatExchange exchange : exchangers) {
                if (exchange.section instanceof ExchangeSectionStart s) {
                    start = s;
                } else if (exchange.section instanceof ExchangeSectionEnd e) {
                    end = e;
                }
                exchange.section = null;
                level.setBlock(exchange.worldPosition,
                        exchange.getBlockState().setValue(BlockHeatExchange.FACING, thisFacing.getOpposite()),
                        Block.UPDATE_ALL);
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
    }

    private int computeSyncHash() {
        if (section == null) return 0;
        int h = section instanceof ExchangeSectionStart ? 1 : 2;
        h = h * 31 + section.tankInput.getAmountAsInt(0);
        h = h * 31 + section.tankOutput.getAmountAsInt(0);
        h = h * 31 + fluidResourceSyncHash(section.tankInput.getResource(0));
        h = h * 31 + fluidResourceSyncHash(section.tankOutput.getResource(0));
        if (section instanceof ExchangeSectionStart s) {
            h = h * 31 + s.progressState.ordinal();
            h = h * 31 + s.middleCount;
        }
        return h;
    }

    private static int fluidResourceSyncHash(FluidResource resource) {
        if (resource == null || resource.isEmpty()) {
            return 0;
        }
        net.minecraft.world.level.material.Fluid canonical = FluidUtilBC.canonicalFluid(resource.getFluid());
        net.minecraft.resources.Identifier key = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(canonical);
        int fluidHash = key != null ? key.hashCode() : 0;
        return 31 * fluidHash + resource.getComponentsPatch().hashCode();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        if (section == null) {
            left.add("section = null");
        } else {
            left.add("section = " + (section instanceof ExchangeSectionStart ? "start" : "end"));
            section.getDebugInfo(left, right, side);
        }
    }

    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
        if (section == null) {
            left.add("section = null");
        } else {
            left.add("section = " + (section instanceof ExchangeSectionStart ? "start" : "end"));
            section.getClientDebugInfo(left, right, side);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (section instanceof ExchangeSectionStart s) {
            s.endSection = null;
        }
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        checkNeighbours = true;
    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        TileHeatExchange start = findStart();
        return start != null ? start.getBlockPos() : getBlockPos();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.buildcraftfactory.heat_exchange");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new ContainerHeatExchange(containerId, playerInv, findStart());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("containerSlots", CompoundTag.CODEC, containerSlots.serializeNBT());
        if (section != null) {
            output.putBoolean("hasSection", true);
            output.putBoolean("isStart", section instanceof ExchangeSectionStart);
            FluidStack inStack = section.tankInput.getResource(0).toStack(section.tankInput.getAmountAsInt(0));
            if (!inStack.isEmpty()) {
                output.store("sectionInput", FluidStack.CODEC, inStack);
            }
            FluidStack outStack = section.tankOutput.getResource(0).toStack(section.tankOutput.getAmountAsInt(0));
            if (!outStack.isEmpty()) {
                output.store("sectionOutput", FluidStack.CODEC, outStack);
            }
            if (section instanceof ExchangeSectionStart s) {
                output.putInt("middleCount", s.middleCount);
                output.putInt("progress", s.progress);
                output.putInt("progressState", s.progressState.ordinal());
            }
        } else {
            output.putBoolean("hasSection", false);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        containerSlots.deserializeNBT(input.read("containerSlots", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        if (input.getBooleanOr("hasSection", false)) {
            boolean isStart = input.getBooleanOr("isStart", true);
            if (isStart) {

                ExchangeSectionStart s;
                if (section instanceof ExchangeSectionStart existing) {
                    s = existing;
                } else {
                    s = new ExchangeSectionStart(this);
                }

                loadTank(s.tankInput, input, "sectionInput");
                loadTank(s.tankOutput, input, "sectionOutput");
                s.middleCount = input.getIntOr("middleCount", 1);

                int stateOrd = input.getIntOr("progressState", 0);
                s.progressState = EnumProgressState.values()[Math.min(stateOrd, EnumProgressState.values().length - 1)];
                section = s;
            } else {

                ExchangeSectionEnd e;
                if (section instanceof ExchangeSectionEnd existing) {
                    e = existing;
                } else {
                    e = new ExchangeSectionEnd(this);
                }
                loadTank(e.tankInput, input, "sectionInput");
                loadTank(e.tankOutput, input, "sectionOutput");
                section = e;
            }
        } else if (section != null) {
            section = null;
        }
        checkNeighbours = true;
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

    public enum EnumProgressState {

        OFF,

        PREPARING,

        RUNNING,

        STOPPING
    }

    public static abstract class ExchangeSection {
        public final FluidStacksResourceHandler tankInput;
        public final OutputTank tankOutput;
        public final FluidSmoother smoothedTankInput, smoothedTankOutput;
        private TileHeatExchange tile;

        ExchangeSection(TileHeatExchange tile, Predicate<FluidStack> inputFilter) {
            tankInput = new FluidStacksResourceHandler(1, 2000) {
                @Override
                public boolean isValid(int index, FluidResource resource) {
                    return inputFilter.test(resource.toStack(1));
                }
            };
            tankOutput = new OutputTank();
            smoothedTankInput = new FluidSmoother(tankInput);
            smoothedTankOutput = new FluidSmoother(tankOutput);
            this.tile = tile;
        }

        void tick() {

        }

        void clientTick() {
            smoothedTankInput.tick();
            smoothedTankOutput.tick();
        }

        void getDebugInfo(List<String> left, List<String> right, Direction side) {
            FluidStack inStack = tankInput.getResource(0).toStack(tankInput.getAmountAsInt(0));
            FluidStack outStack = tankOutput.getResource(0).toStack(tankOutput.getAmountAsInt(0));
            left.add("tank_input = " + FluidUtilBC.getDebugString(inStack));
            left.add("tank_output = " + FluidUtilBC.getDebugString(outStack));
        }

        void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
            smoothedTankInput.getDebugInfo(left, right, side);
            smoothedTankOutput.getDebugInfo(left, right, side);
        }

        public TileHeatExchange getTile() {
            return tile;
        }

        public void setTile(TileHeatExchange tile) {
            this.tile = tile;
        }

        @Nullable
        ResourceHandler<FluidResource> getFluidAutoOutputTarget() {
            return null;
        }
    }

    public static class OutputTank extends FluidStacksResourceHandler {
        private boolean internalInsert = false;

        public OutputTank() {
            super(1, 2000);
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

    public static class ExchangeSectionStart extends ExchangeSection {
        ExchangeSectionEnd endSection;
        public int middleCount;
        int progress = 0;
        int progressLast = 0;
        EnumProgressState progressState = EnumProgressState.OFF;

        ExchangeSectionStart(TileHeatExchange tile) {
            super(tile, ExchangeSectionStart::isHeatant);
        }

        public ExchangeSectionEnd getEndSection() {
            return endSection;
        }

        public EnumProgressState getProgressState() {
            return progressState;
        }

        public double getProgress(float partialTicks) {
            return (progressLast + (progress - progressLast) * partialTicks) / 120.0;
        }

        private static boolean isHeatant(FluidStack fluid) {
            IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
            if (manager == null) return false;
            return manager.getHeatableRegistry().getRecipeForInput(fluid) != null;
        }

        @Nullable
        @Override
        ResourceHandler<FluidResource> getFluidAutoOutputTarget() {
            Direction facing = getTile().getFacing();
            if (facing == null || getTile().level == null) return null;
            BlockPos targetPos = getTile().worldPosition.relative(facing.getClockWise());
            return buildcraft.lib.attachments.AttachmentQueries.getBlock(
                    getTile().level,
                    Attachments.Fluid.BLOCK,
                    targetPos,
                    facing.getCounterClockWise());
        }

        @Override
        void tick() {
            super.tick();
            updateProgress();
            if (getTile().level != null && !getTile().level.isClientSide()) {
                if (endSection != null) {
                    craft();
                } else if (progressState != EnumProgressState.OFF) {
                    progressState = EnumProgressState.STOPPING;
                }
                output();
                processContainerSlots();
            }
        }

        @SuppressWarnings("removal")
        private void processContainerSlots() {
            TileHeatExchange tile = getTile();
            if (tile == null || tile.level == null) return;
            if (tile.level.getGameTime() % 5 != 0) return;

            if (endSection != null) {
                drainSlotIntoTank(tile, 0, endSection.tankInput);
            }

            drainSlotIntoTank(tile, 1, this.tankInput);

            if (endSection != null) {
                fillSlotFromTank(tile, 2, endSection.tankOutput);
            }

            fillSlotFromTank(tile, 3, this.tankOutput);
        }

        private static void drainSlotIntoTank(TileHeatExchange tile, int slot, FluidStacksResourceHandler tank) {
            FactoryFluidContainers.syncDrainSlot(tile.containerSlots, slot, tank);
        }

        private static void fillSlotFromTank(TileHeatExchange tile, int slot, FluidStacksResourceHandler tank) {
            FactoryFluidContainers.syncFillSlot(tile.containerSlots, slot, tank);
        }

        @Override
        void clientTick() {
            super.clientTick();
            updateProgress();
            spawnParticles();
        }

        private void updateProgress() {
            progressLast = progress;
            switch (progressState) {
                case STOPPING -> {
                    progress--;
                    if (progress <= 0) {
                        progress = 0;
                        progressState = EnumProgressState.OFF;
                    }
                }
                case PREPARING, RUNNING -> {
                    int lag = 120;
                    progress++;
                    if (progress >= lag) {
                        progress = lag;
                        progressState = EnumProgressState.RUNNING;
                    }
                }
                default -> {}
            }
        }

        private void craft() {
            if (endSection == null) return;
            FluidStacksResourceHandler c_in = endSection.tankInput;
            OutputTank c_out = tankOutput;
            FluidStacksResourceHandler h_in = tankInput;
            OutputTank h_out = endSection.tankOutput;
            IRefineryRecipeManager reg = BuildcraftRecipeRegistry.refineryRecipes;
            if (reg == null) {
                progressState = EnumProgressState.STOPPING;
                return;
            }
            FluidStack c_in_fluid = c_in.getResource(0).toStack(c_in.getAmountAsInt(0));
            FluidStack h_in_fluid = h_in.getResource(0).toStack(h_in.getAmountAsInt(0));
            ICoolableRecipe c_recipe = reg.getCoolableRegistry().getRecipeForInput(c_in_fluid);
            IHeatableRecipe h_recipe = reg.getHeatableRegistry().getRecipeForInput(h_in_fluid);
            if (h_recipe == null || c_recipe == null) {
                progressState = EnumProgressState.STOPPING;
                return;
            }
            if (c_recipe.heatFrom() <= h_recipe.heatFrom()) {
                progressState = EnumProgressState.STOPPING;
                return;
            }
            int c_diff = c_recipe.heatFrom() - c_recipe.heatTo();
            int h_diff = h_recipe.heatTo() - h_recipe.heatFrom();
            if (h_diff < 1 || c_diff < 1) {
                progressState = EnumProgressState.STOPPING;
                return;
            }

            int max_amount = FLUID_MULT[Math.min(middleCount - 1, FLUID_MULT.length - 1)];
            FluidStack c_in_f = setAmount(c_recipe.in(), max_amount);
            FluidStack c_out_f = setAmount(c_recipe.out(), max_amount);
            FluidStack h_in_f = setAmount(h_recipe.in(), max_amount);
            FluidStack h_out_f = setAmount(h_recipe.out(), max_amount);

            int c_out_amount = c_out_f == null || c_out_f.isEmpty()
                    ? max_amount
                    : simulateInsert(c_out, c_out_f);
            int h_out_amount = h_out_f == null || h_out_f.isEmpty()
                    ? max_amount
                    : simulateInsert(h_out, h_out_f);

            int c_in_amount = simulateExtract(c_in, c_in_f);
            int h_in_amount = simulateExtract(h_in, h_in_f);

            int min_common = Math.min(Math.min(c_out_amount, h_out_amount),
                    Math.min(c_in_amount, h_in_amount));

            if (min_common > 0) {
                c_in_f = setAmount(c_recipe.in(), min_common);
                c_out_f = setAmount(c_recipe.out(), min_common);
                h_in_f = setAmount(h_recipe.in(), min_common);
                h_out_f = setAmount(h_recipe.out(), min_common);

                if (progressState == EnumProgressState.OFF) {
                    progressState = EnumProgressState.PREPARING;
                } else if (progressState == EnumProgressState.RUNNING) {

                    try (Transaction tx = Transaction.openRoot()) {
                        boolean ok = true;
                        if (c_out_f != null && !c_out_f.isEmpty()) {
                            int n = c_out.insertInternal(0, FluidResource.of(c_out_f), c_out_f.getAmount(), tx);
                            ok = ok && n == c_out_f.getAmount();
                        }
                        if (ok && h_out_f != null && !h_out_f.isEmpty()) {
                            int n = h_out.insertInternal(0, FluidResource.of(h_out_f), h_out_f.getAmount(), tx);
                            ok = ok && n == h_out_f.getAmount();
                        }
                        if (ok) {
                            int n = c_in.extract(0, FluidResource.of(c_in_f), c_in_f.getAmount(), tx);
                            ok = n == c_in_f.getAmount();
                        }
                        if (ok) {
                            int n = h_in.extract(0, FluidResource.of(h_in_f), h_in_f.getAmount(), tx);
                            ok = n == h_in_f.getAmount();
                        }
                        if (ok) tx.commit();
                    }
                }
            } else {
                progressState = EnumProgressState.STOPPING;
            }
        }

        private void spawnParticles() {
            if (progressState != EnumProgressState.RUNNING) return;
            ExchangeSectionEnd end = endSection;
            if (end == null || getTile().level == null) return;

            Vec3 from = Vec3.atCenterOf(getTile().getBlockPos());
            FluidStack c_in_f = end.tankInput.getResource(0).toStack(end.tankInput.getAmountAsInt(0));

            if (!c_in_f.isEmpty() && FluidUtilBC.areFluidsEqual(c_in_f.getFluid(), Fluids.LAVA)) {
                Direction facing = getTile().getFacing();
                if (facing != null) {
                    spewForth(from, facing.getClockWise(), true);
                }
            }

            FluidStack h_in_f = tankInput.getResource(0).toStack(tankInput.getAmountAsInt(0));
            from = Vec3.atCenterOf(end.getTile().getBlockPos());

            if (!h_in_f.isEmpty() && FluidUtilBC.areFluidsEqual(h_in_f.getFluid(), Fluids.WATER)) {
                spewForth(from, Direction.UP, false);
            }
        }

        private void spewForth(Vec3 from, Direction dir, boolean smoke) {
            Level w = getTile().getLevel();
            if (w == null) return;
            Vec3 vecDir = Vec3.atLowerCornerOf(dir.getUnitVec3i());
            from = from.add(vecDir);
            double x = from.x, y = from.y, z = from.z;
            Vec3 motion = vecDir.scale(0.4);
            for (int i = 0; i < 3; i++) {
                double dx = motion.x + (Math.random() - 0.5) * 0.1;
                double dy = motion.y + (Math.random() - 0.5) * 0.1;
                double dz = motion.z + (Math.random() - 0.5) * 0.1;
                w.addParticle(smoke ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD,
                        x, y, z, dx, dy, dz);
            }
        }

        private void output() {
            ResourceHandler<FluidResource> thisOut = getFluidAutoOutputTarget();
            if (thisOut != null) {
                moveFluid(tankOutput, thisOut, 1000);
            }
            if (endSection != null) {
                ResourceHandler<FluidResource> endOut = endSection.getFluidAutoOutputTarget();
                if (endOut != null) {
                    moveFluid(endSection.tankOutput, endOut, 1000);
                }
            }
        }

        @Override
        void getDebugInfo(List<String> left, List<String> right, Direction side) {
            super.getDebugInfo(left, right, side);
            left.add("progress = " + progress);
            left.add("state = " + progressState);
            left.add("has_end = " + (endSection != null));
        }

        @Nullable
        private static FluidStack setAmount(@Nullable FluidStack fluid, int amount) {
            if (fluid == null || fluid.isEmpty()) return null;
            return fluid.copyWithAmount(amount);
        }

        private static int simulateExtract(FluidStacksResourceHandler t, @Nullable FluidStack fluid) {
            if (fluid == null || fluid.isEmpty()) return 0;
            try (Transaction tx = Transaction.openRoot()) {
                return t.extract(0, FluidResource.of(fluid), fluid.getAmount(), tx);
            }
        }

        private static int simulateInsert(OutputTank t, @Nullable FluidStack fluid) {
            if (fluid == null || fluid.isEmpty()) return 0;
            try (Transaction tx = Transaction.openRoot()) {
                return t.insertInternal(0, FluidResource.of(fluid), fluid.getAmount(), tx);
            }
        }

        private static void moveFluid(FluidStacksResourceHandler from, ResourceHandler<FluidResource> to, int maxAmount) {
            try (Transaction tx = Transaction.openRoot()) {
                int moved = buildcraft.lib.transfer.ResourceHandlerUtil.move(
                    from, to, r -> true, maxAmount, tx
                );
                if (moved > 0) tx.commit();
            }
        }
    }

    public static class ExchangeSectionEnd extends ExchangeSection {
        ExchangeSectionEnd(TileHeatExchange tile) {
            super(tile, ExchangeSectionEnd::isCoolant);
        }

        private static boolean isCoolant(FluidStack fluid) {
            IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
            if (manager == null) return false;
            return manager.getCoolableRegistry().getRecipeForInput(fluid) != null;
        }

        @Nullable
        @Override
        ResourceHandler<FluidResource> getFluidAutoOutputTarget() {
            if (getTile().level == null) return null;
            return buildcraft.lib.attachments.AttachmentQueries.getBlock(
                    getTile().level,
                    Attachments.Fluid.BLOCK,
                    getTile().worldPosition.above(),
                    Direction.DOWN);
        }
    }
}
