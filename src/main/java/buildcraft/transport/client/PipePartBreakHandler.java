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
 * server (authoritatively) to remove it via {@link MessageRemovePipePart}, then return {@code FAIL} to cancel the
 * vanilla attack without letting Fabric send a break packet -- so the client never starts breaking the whole pipe
 * (no flicker) and a creative-mode server never instant-breaks it. A plain pipe-body hit falls through to normal
 * breaking. See the return site for why {@code FAIL} rather than {@code SUCCESS}.
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
               // Ship the exact crosshair hit point: the server must remove precisely the part the player aimed at,
               // not whatever its own (lagging) re-raytrace would land on -- see MessageRemovePipePart.
               BcPacketDistributor.sendToServer(new MessageRemovePipePart(pos, (float)lx, (float)ly, (float)lz));
               player.swing(hand);
               // FAIL, not SUCCESS: Fabric's AttackBlockCallback (fabric_fireAttackBlockCallback) sends the vanilla
               // START_DESTROY_BLOCK packet whenever the result consumesAction() (SUCCESS/CONSUME). A creative-mode
               // server instant-breaks the whole block on START, so SUCCESS destroyed the entire pipe in creative.
               // FAIL still cancels the vanilla attack (setReturnValue(false)) but sends no break packet, leaving only
               // our MessageRemovePipePart -- correct in both survival and creative, and no break-crack in either.
               return InteractionResult.FAIL;
            }
         }

         return InteractionResult.PASS;
      });
   }
}
