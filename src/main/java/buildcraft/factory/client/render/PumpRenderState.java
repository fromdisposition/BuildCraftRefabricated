/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

//? if >= 1.21.10 {
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
//?}

//? if >= 1.21.10 {
public class PumpRenderState extends BlockEntityRenderState {
//?} else {
/*public class PumpRenderState {
*///?}
   public int powerColour;
   public int statusColour;
   public double shaftLength;
}
