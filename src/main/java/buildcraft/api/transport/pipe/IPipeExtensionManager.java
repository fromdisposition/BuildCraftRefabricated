/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import buildcraft.api.transport.IStripesActivator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPipeExtensionManager {
   boolean requestPipeExtension(Level var1, BlockPos var2, Direction var3, IStripesActivator var4, ItemStack var5);

   void registerRetractionPipe(PipeDefinition var1);
}
