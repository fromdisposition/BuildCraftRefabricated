/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.client.render.tile.BcBerState;
import buildcraft.lib.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class HeatExchangeRenderState extends BcBerState<TileHeatExchange> {
   public boolean render;
   public TileHeatExchange.ExchangeSectionStart section;
   public TileHeatExchange.ExchangeSectionEnd sectionEnd;
   public Direction face;
   public int middleCount;
   public TileHeatExchange.EnumProgressState progressState;
   public double progress;
   public BlockPos endDiff;
   public FluidStack coolantFluid;
   public FluidStack heatantFluid;
}
