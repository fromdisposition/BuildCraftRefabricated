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
 * Client-side: left-clicking a pluggable (gate, lens, plug, ...) or wire on a pipe removes just that part, not the
 * whole pipe. Vanilla has no "break a sub-part" hook, so we detect the crosshair sub-part on the client and ask the
 * server (authoritatively) to remove it via {@link MessageRemovePipePart}, then return {@code SUCCESS} to cancel the
 * vanilla attack -- so the client never starts breaking the whole pipe and there is no break-crack flicker. A plain
 * pipe-body hit falls through to normal breaking.
 *
 * <p>Client-only (touches {@link Minecraft}); registered from {@code BCTransportFabricClient} and never loaded on a
 * dedicated server.
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
               BcPacketDistributor.sendToServer(new MessageRemovePipePart(pos));
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.PASS;
      });
   }
}
