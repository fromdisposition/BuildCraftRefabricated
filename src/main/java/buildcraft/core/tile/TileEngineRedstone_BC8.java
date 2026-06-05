/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;

import buildcraft.core.BCCoreBlockEntities;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.AdvancementUtil;

public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
    private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:free_power");
    private boolean givenAdvancement = false;

    public TileEngineRedstone_BC8(BlockPos pos, BlockState state) {
        super(BCCoreBlockEntities.ENGINE_REDSTONE, pos, state);
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(true);
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered;
    }

    @Override
    protected void engineUpdate() {
        if (isRedstonePowered) {
            power = getMaxPower();
            if (level != null && level.getGameTime() % 16 == 0) {
                if (getHeatLevel() < 0.8f) {
                    heat += 4;
                }
                if (isPumping && !givenAdvancement && getOwner() != null) {
                    givenAdvancement = AdvancementUtil.unlockAdvancement(
                        getOwner().id(), level, ADVANCEMENT);
                }
            }
        } else {
            power = 0;
        }
    }

    @Override
    public double getPistonSpeed() {
        return super.getPistonSpeed() / 2;
    }

    @Override
    public void updateHeatLevel() {
        if (heat > MIN_HEAT) {
            heat -= 0.2f;
            if (heat < MIN_HEAT) {
                heat = MIN_HEAT;
            }
        }
    }

    @Override
    protected int getMaxChainLength() {
        return 0;
    }

    @Override
    public long getMaxPower() {
        return MjAPI.MJ;
    }

    @Override
    public long minPowerReceived() {
        return MjAPI.MJ / 10;
    }

    @Override
    public long maxPowerReceived() {
        return 4 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 4 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 0;
    }

    @Override
    public long getCurrentOutput() {
        return MjAPI.MJ / 20;
    }

    @Override
    public void getDebugInfo(java.util.List<String> left, java.util.List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
    }

    @Override
    public void getClientDebugInfo(java.util.List<String> left, java.util.List<String> right, Direction side) {
        super.getClientDebugInfo(left, right, side);
    }
}
