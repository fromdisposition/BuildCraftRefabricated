/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport;

import buildcraft.api.transport.pipe.IPipeHolder;
import net.minecraft.world.item.DyeColor;

public interface IWireManager {
   IPipeHolder getHolder();

   void updateBetweens(boolean var1);

   DyeColor getColorOfPart(EnumWirePart var1);

   DyeColor removePart(EnumWirePart var1);

   boolean addPart(EnumWirePart var1, DyeColor var2);

   boolean hasPartOfColor(DyeColor var1);

   boolean isPowered(EnumWirePart var1);

   boolean isAnyPowered(DyeColor var1);
}
