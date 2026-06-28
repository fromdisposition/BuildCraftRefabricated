/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.lib.client.fluid.BcFluidAppearance;
import buildcraft.lib.fluid.stack.FluidStack;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
//?}

//? if >= 1.21.10 {
public class TankRenderState extends BlockEntityRenderState {
//?} else {
/*public class TankRenderState {
*///?}
   public boolean hasFluid;
   public BcFluidAppearance appearance;
   public FluidStack fluid;
   public double amount;
   public int capacity;
   public float minY;
   public float maxYFull;
   public boolean renderTop;
   public boolean renderBottom;
}
