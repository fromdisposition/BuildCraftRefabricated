/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;

import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.InventoryUtil;

public class TileMiningWell extends TileMiner {
    private boolean shouldCheck = true;
    private int recheckCooldown = 0;

    private IMjReceiver mjReceiver;

    public TileMiningWell(BlockPos pos, BlockState state) {
        super(BCFactoryBlockEntities.MINING_WELL, pos, state);
    }

    @Override
    protected void mine() {
        if (currentPos != null && canBreak()) {
            shouldCheck = true;
            long target = BlockUtil.computeBlockBreakPower(level, currentPos);
            progress += (int) battery.extractPower(0, target - progress);
            if (progress >= target) {
                progress = 0;
                level.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
                if (level instanceof ServerLevel serverLevel) {

                    BlockUtil.breakBlockAndGetDropsWithXp(
                        serverLevel,
                        currentPos,
                        new ItemStack(Items.IRON_PICKAXE),
                        getOwner()
                    ).ifPresent(result -> {
                        result.drops().forEach(stack ->
                            InventoryUtil.addToBestAcceptor(level, worldPosition, null, stack));

                        if (result.xp() > 0) {
                            net.minecraft.world.entity.ExperienceOrb.award(
                                    serverLevel,
                                    net.minecraft.world.phys.Vec3.atCenterOf(worldPosition),
                                    result.xp());
                        }
                    });
                }
                nextPos();
            } else {
                if (!level.isEmptyBlock(currentPos)) {
                    level.destroyBlockProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                }
            }
        } else if (currentPos != null && !canBreak()) {

            progress = 0;
            nextPos();
        } else if (shouldCheck || recheckCooldown <= 0) {
            nextPos();
            if (currentPos == null) {
                shouldCheck = false;
            }
            recheckCooldown = 256;
        } else {
            recheckCooldown--;
        }
    }

    private boolean canBreak() {
        if (level.isEmptyBlock(currentPos) || BlockUtil.isUnbreakableBlock(level, currentPos, getOwner())) {
            return false;
        }

        BlockState state = level.getBlockState(currentPos);
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        }

        Fluid fluid = BlockUtil.getFluidWithFlowing(level, currentPos);
        if (fluid == null) {
            return true;
        }

        return buildcraft.lib.fluids.FluidTypes.of(fluid).getViscosity() <= 1000;
    }

    private void nextPos() {
        currentPos = worldPosition;
        while (true) {
            currentPos = currentPos.below();
            if (level.isOutsideBuildHeight(currentPos)) {
                break;
            }
            if (worldPosition.getY() - currentPos.getY() > BCCoreConfig.miningMaxDepth.get()) {
                break;
            }
            if (canBreak()) {

                if (level instanceof ServerLevel serverLevel
                        && !BlockUtil.canMachineBreak(serverLevel, currentPos, getOwner())) {
                    continue;
                }
                updateLength();
                return;
            }

            FluidState fluidState = level.getFluidState(currentPos);
            boolean isPassable = !fluidState.isEmpty()
                    && buildcraft.lib.fluids.FluidTypes.of(fluidState.getType()).getViscosity() <= 1000;
            if (level.isEmptyBlock(currentPos)
                    || level.getBlockState(currentPos).is(BCFactoryBlocks.TUBE)
                    || isPassable) {
                continue;
            } else {

                break;
            }
        }
        currentPos = null;
        updateLength();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {

            if (currentPos != null) {
                level.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
            }
        }
        super.setRemoved();
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        if (mjReceiver == null) {
            mjReceiver = new IMjReceiver() {
                @Override
                public long getPowerRequested() {
                    return battery.getCapacity() - battery.getStored();
                }

                @Override
                public long receivePower(long microJoules, boolean simulate) {
                    return battery.addPowerChecking(microJoules, simulate);
                }

                @Override
                public boolean canConnect(@Nonnull IMjConnector other) {
                    return true;
                }
            };
        }
        return mjReceiver;
    }
}
