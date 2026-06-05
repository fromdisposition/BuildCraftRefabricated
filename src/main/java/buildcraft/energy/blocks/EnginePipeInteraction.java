/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.energy.blocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pipe.PipeFlowType;

final class EnginePipeInteraction {
    private EnginePipeInteraction() {}

    @Nullable
    static InteractionResult tryPlacePipe(IItemPipe pipe, ItemStack stack, Level level, Player player,
            InteractionHand hand, BlockHitResult hitResult,
            PipeFlowType fullFamily, PipeFlowType extractionOnlyFamily) {
        if (!accepts(pipe.getDefinition(), fullFamily, extractionOnlyFamily)) {
            return null;
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }

        InteractionResult result = blockItem.place(new BlockPlaceContext(level, player, hand, stack, hitResult));
        return result.consumesAction() ? result : null;
    }

    static boolean accepts(PipeDefinition def, PipeFlowType fullFamily, PipeFlowType extractionOnlyFamily) {
        if (def.flowType == fullFamily) {
            return true;
        }
        return def.flowType == extractionOnlyFamily && isExtractionPipe(def);
    }

    static boolean isExtractionPipe(PipeDefinition def) {
        String id = def.identifier;
        if (id == null) {
            return false;
        }
        int colon = id.indexOf(':');
        String path = colon >= 0 ? id.substring(colon + 1) : id;
        return path.startsWith("wood_") || path.startsWith("diamond_wood_");
    }
}
