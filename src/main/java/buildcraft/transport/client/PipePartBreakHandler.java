/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client;

import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.net.MessageRemovePipePart;
import buildcraft.transport.tile.TilePipeHolder;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Detects the crosshair sub-part client-side and asks the server to remove it via {@link MessageRemovePipePart},
 * then returns {@code FAIL} (not {@code SUCCESS}) to cancel the vanilla attack without triggering a break packet --
 * {@code SUCCESS} let a creative-mode server instant-break the whole pipe. Client-only; never loaded server-side.
 */
public final class PipePartBreakHandler {
   private PipePartBreakHandler() {
   }

   public static void register() {
      AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
         if (world.isClientSide()
            && world.getBlockState(pos).getBlock() instanceof BlockPipeHolder
            && world.getBlockEntity(pos) instanceof TilePipeHolder tile
            && tile.getPipe() != null
            && Minecraft.getInstance().hitResult instanceof BlockHitResult hit
            && pos.equals(hit.getBlockPos())) {
            double lx = hit.getLocation().x - pos.getX();
            double ly = hit.getLocation().y - pos.getY();
            double lz = hit.getLocation().z - pos.getZ();
            if (BlockPipeHolder.getHitPluggable(tile, lx, ly, lz) != null
               || BlockPipeHolder.getHitWire(tile, lx, ly, lz) != null
               || BlockPipeHolder.getHitWireBetween(tile, lx, ly, lz) != null) {
               // Ship the exact crosshair hit point: the server must remove precisely the part aimed at, not whatever its own lagging re-raytrace would land on.
               BcPacketDistributor.sendToServer(new MessageRemovePipePart(pos, (float)lx, (float)ly, (float)lz));
               player.swing(hand, player.getItemInHand(hand).getOrDefault(net.minecraft.core.component.DataComponents.INTERACT_ANIMATION, net.minecraft.world.item.component.SwingAnimation.DEFAULT), false);
               // FAIL, not SUCCESS: Fabric's AttackBlockCallback sends the vanilla START_DESTROY_BLOCK packet whenever
               // the result consumesAction() (SUCCESS/CONSUME), and a creative-mode server instant-breaks the whole
               // block on START. FAIL still cancels the vanilla attack but sends no break packet.
               return InteractionResult.FAIL;
            }
         }

         return InteractionResult.PASS;
      });
   }
}
