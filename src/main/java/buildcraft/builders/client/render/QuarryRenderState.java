/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.client.render.tile.BcBerState;
import net.minecraft.core.Direction;

public class QuarryRenderState extends BcBerState<TileQuarry> {
   public Direction rear;
   public int greenColour;
   public int redColour;
}
