/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.item.ItemDebugger;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class ClientDebuggables {
   public static final List<String> SERVER_LEFT = new ArrayList<>();
   public static final List<String> SERVER_RIGHT = new ArrayList<>();

   @Nullable
   public static IDebuggable getDebuggableObject(@Nullable HitResult mouseOver) {
      Minecraft mc = Minecraft.getInstance();
      //? if >= 26.2 {
      if ((Boolean)mc.options.reducedDebugInfo().get() || mc.player == null || mc.player.isReducedDebugInfo() || !mc.debugEntries.isOverlayVisible()) {
      //?} else {
      /*if ((Boolean)mc.options.reducedDebugInfo().get() || mc.player == null || mc.player.isReducedDebugInfo() || !mc.gui.getDebugOverlay().showDebugScreen()) {
      *///?}
         return null;
      }

      // The server half (MessageDebugRequest) only sends debug data when the player holds the Debugger (or is
      // in creative); mirror that here so the client-computed getDebugInfo doesn't leak onto F3 for everyone.
      if (!ItemDebugger.isShowDebugInfo(mc.player)) {
         return null;
      }

      if (mouseOver == null) {
         return null;
      }

      ClientLevel world = mc.level;
      if (world == null) {
         return null;
      }

      if (mouseOver instanceof BlockHitResult blockHit && blockHit.getType() == Type.BLOCK) {
         BlockPos pos = blockHit.getBlockPos();
         BlockEntity tile = world.getBlockEntity(pos);
         if (tile instanceof IDebuggable) {
            return (IDebuggable)tile;
         }
      } else if (mouseOver instanceof EntityHitResult entityHit && entityHit.getType() == Type.ENTITY) {
         Entity entity = entityHit.getEntity();
         if (entity instanceof IDebuggable) {
            return (IDebuggable)entity;
         }
      }

      return null;
   }

   @Nullable
   public static Direction getHitSide(@Nullable HitResult mouseOver) {
      return mouseOver instanceof BlockHitResult blockHit ? blockHit.getDirection() : null;
   }
}
