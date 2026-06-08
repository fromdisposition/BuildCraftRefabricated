/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.debug.ClientDebuggables;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.net.MessageDebugRequest;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class DebugOverlayHelper {
   private static final String DIFF_START = "" + ChatFormatting.RED + ChatFormatting.BOLD + "!" + ChatFormatting.RESET;
   private static final String DIFF_HEADER_FORMATTING = "" + ChatFormatting.AQUA + ChatFormatting.BOLD;
   private static final List<String> CLIENT_LEFT = new ArrayList<>();
   private static final List<String> CLIENT_RIGHT = new ArrayList<>();
   private static Direction lastSide = Direction.UP;
   private static boolean hasData = false;
   private static BlockPos lastRequestPos = null;
   private static Direction lastRequestSide = Direction.UP;
   private static int ticksSinceRequest = 0;
   private static final int REQUEST_INTERVAL = 10;

   public static void onClientTick() {
      Minecraft mc = Minecraft.getInstance();
      HitResult mouseOver = mc.hitResult;
      IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mouseOver);
      if (debuggable == null) {
         if (hasData) {
            CLIENT_LEFT.clear();
            CLIENT_RIGHT.clear();
            ClientDebuggables.SERVER_LEFT.clear();
            ClientDebuggables.SERVER_RIGHT.clear();
            hasData = false;
         }

         lastRequestPos = null;
         ticksSinceRequest = 0;
      } else {
         hasData = true;
         Direction side = ClientDebuggables.getHitSide(mouseOver);
         if (side == null) {
            side = Direction.UP;
         }

         lastSide = side;
         CLIENT_LEFT.clear();
         CLIENT_RIGHT.clear();
         debuggable.getDebugInfo(CLIENT_LEFT, CLIENT_RIGHT, side);
         if (mouseOver instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            boolean targetChanged = lastRequestPos == null || !lastRequestPos.equals(pos) || lastRequestSide != side;
            ticksSinceRequest++;
            if (targetChanged || ticksSinceRequest >= 10) {
               BcPacketDistributor.sendToServer(new MessageDebugRequest(pos, side));
               lastRequestPos = pos.immutable();
               lastRequestSide = side;
               ticksSinceRequest = 0;
            }
         }
      }
   }

   public static List<String> getLeftLines() {
      if (!hasData) {
         return List.of();
      }

      List<String> result = new ArrayList<>();
      appendDiff(result, ClientDebuggables.SERVER_LEFT, CLIENT_LEFT, DIFF_HEADER_FORMATTING + "SERVER:", DIFF_HEADER_FORMATTING + "CLIENT:");
      Minecraft mc = Minecraft.getInstance();
      IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mc.hitResult);
      if (debuggable != null) {
         debuggable.getClientDebugInfo(result, new ArrayList<>(), lastSide);
      }

      return result;
   }

   public static List<String> getRightLines() {
      if (!hasData) {
         return List.of();
      }

      List<String> result = new ArrayList<>();
      appendDiff(result, ClientDebuggables.SERVER_RIGHT, CLIENT_RIGHT, DIFF_HEADER_FORMATTING + "SERVER:", DIFF_HEADER_FORMATTING + "CLIENT:");
      Minecraft mc = Minecraft.getInstance();
      IDebuggable debuggable = ClientDebuggables.getDebuggableObject(mc.hitResult);
      if (debuggable != null) {
         List<String> leftDummy = new ArrayList<>();
         debuggable.getClientDebugInfo(leftDummy, result, lastSide);
      }

      return result;
   }

   private static void appendDiff(List<String> dest, List<String> first, List<String> second, String headerFirst, String headerSecond) {
      dest.add("");
      dest.add(headerFirst);
      dest.addAll(first);
      dest.add("");
      dest.add(headerSecond);
      if (first.size() != second.size()) {
         dest.addAll(second);
      } else {
         for (int l = 0; l < first.size(); l++) {
            String shownLine = first.get(l);
            String diffLine = second.get(l);
            if (shownLine.equals(diffLine)) {
               dest.add(diffLine);
            } else if (diffLine.startsWith(" ")) {
               dest.add(DIFF_START + diffLine.substring(1));
            } else {
               dest.add(DIFF_START + diffLine);
            }
         }
      }
   }
}
