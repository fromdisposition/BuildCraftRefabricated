/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.render.tile;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import buildcraft.api.enums.EnumPowerStage;

public class EngineRenderState extends BlockEntityRenderState {
    public BlockPos blockPos;
    public float progress;
    public EnumPowerStage powerStage = EnumPowerStage.BLUE;
    public Direction facing = Direction.UP;
}
