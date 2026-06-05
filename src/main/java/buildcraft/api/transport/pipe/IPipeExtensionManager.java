/* Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team https://mod-buildcraft.com/
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.transport.pipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraft.api.transport.IStripesActivator;

public interface IPipeExtensionManager {

    boolean requestPipeExtension(Level world, BlockPos pos, Direction dir, IStripesActivator stripes, ItemStack stack);

    void registerRetractionPipe(PipeDefinition pipeDefinition);

}
