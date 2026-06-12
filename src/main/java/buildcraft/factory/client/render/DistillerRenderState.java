/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class DistillerRenderState extends BlockEntityRenderState {
   public float partialTick;
   public Direction facing;
   @Nullable
   public RenderDistiller.TankSizes sizes;
   @Nullable
   public DistillerFluidSnapshot fluidIn;
   @Nullable
   public DistillerFluidSnapshot fluidGasOut;
   @Nullable
   public DistillerFluidSnapshot fluidLiquidOut;
   public float powerY1;
   public float powerY2;
   public int powerTexIndex;
   public boolean powerTopHalf;
}
