/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client;

import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.item.ItemGoggles;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.debug.DebugRenderHelper;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.zone.ZonePlan;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
//? if >= 26.1.3 {
//?} else {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class GogglesZoneRenderer {
   private static final int OVERLAY_ALPHA = 0x50;
   private static final int DEFAULT_COLOUR = 0xFF00AA00;
   private static final double ROBOT_SCAN_RADIUS = 64.0;

   private GogglesZoneRenderer() {
   }

   public static void render(PoseStack poseStack, Vec3 cameraPos, ClientLevel level) {
      Player player = Minecraft.getInstance().player;
      if (player == null || !ItemGoggles.isWearing(player)) {
         return;
      }

      List<ZoneOverlay> overlays = collectOverlays(level, player);
      if (overlays.isEmpty()) {
         return;
      }

      //? if >= 26.1.3 {
      /*Set<Long> drawn = new HashSet<>();

      for (ZoneOverlay overlay : overlays) {
         if (!(overlay.zone instanceof ZonePlan plan)) {
            continue;
         }

         int colour = overlay.colour;
         int r = colour >> 16 & 0xFF;
         int g = colour >> 8 & 0xFF;
         int b = colour & 0xFF;
         int argb = OVERLAY_ALPHA << 24 | r << 16 | g << 8 | b;

         for (int[] cell : plan.getAll()) {
            int wx = cell[0];
            int wz = cell[1];
            long key = BlockPos.asLong(wx, 0, wz);
            if (!drawn.add(key)) {
               continue;
            }

            if (player.distanceToSqr(wx + 0.5, player.getY(), wz + 0.5) > 128.0 * 128.0) {
               continue;
            }

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, wx, wz);
            AABB box = new AABB(wx, y + 1.02, wz, wx + 1.0, y + 2.0, wz + 1.0);
            DebugRenderHelper.renderTranslucentBox(poseStack, box, cameraPos, argb);
         }
      }*/
      //?} else {
      MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
      Set<Long> drawn = new HashSet<>();

      for (ZoneOverlay overlay : overlays) {
         if (!(overlay.zone instanceof ZonePlan plan)) {
            continue;
         }

         int colour = overlay.colour;
         int r = colour >> 16 & 0xFF;
         int g = colour >> 8 & 0xFF;
         int b = colour & 0xFF;
         int argb = OVERLAY_ALPHA << 24 | r << 16 | g << 8 | b;

         for (int[] cell : plan.getAll()) {
            int wx = cell[0];
            int wz = cell[1];
            long key = BlockPos.asLong(wx, 0, wz);
            if (!drawn.add(key)) {
               continue;
            }

            if (player.distanceToSqr(wx + 0.5, player.getY(), wz + 0.5) > 128.0 * 128.0) {
               continue;
            }

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, wx, wz);
            AABB box = new AABB(wx, y + 1.02, wz, wx + 1.0, y + 2.0, wz + 1.0);
            DebugRenderHelper.renderTranslucentBox(poseStack, bufferSource, box, cameraPos, argb);
         }
      }

      bufferSource.endBatch(BCLibRenderTypes.debugFilled());
      //?}
   }

   private static List<ZoneOverlay> collectOverlays(Level level, Player player) {
      List<ZoneOverlay> overlays = new ArrayList<>();
      Set<Integer> seenMaps = new HashSet<>();

      collectFromInventory(player, overlays, seenMaps);

      AABB scan = player.getBoundingBox().inflate(ROBOT_SCAN_RADIUS);
      for (EntityRobot robot : level.getEntitiesOfClass(EntityRobot.class, scan)) {
         collectFromRobot(robot, overlays, seenMaps);
      }

      return overlays;
   }

   private static void collectFromInventory(Player player, List<ZoneOverlay> overlays, Set<Integer> seenMaps) {
      for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
         addMapStack(player.getInventory().getItem(i), overlays, seenMaps);
      }
   }

   private static void collectFromRobot(EntityRobot robot, List<ZoneOverlay> overlays, Set<Integer> seenMaps) {
      IZone work = robot.getZoneToWork();
      if (work != null) {
         addZone(work, zoneColourFromRobot(robot, "buildcraft:robot.work_in_area"), overlays, seenMaps);
      }

      IZone loadUnload = robot.getZoneToLoadUnload();
      if (loadUnload != null && loadUnload != work) {
         addZone(loadUnload, zoneColourFromRobot(robot, "buildcraft:robot.load_unload_area"), overlays, seenMaps);
      }
   }

   private static int zoneColourFromRobot(EntityRobotBase robot, String actionTag) {
      if (robot.getLinkedStation() == null) {
         return DEFAULT_COLOUR;
      }

      for (buildcraft.api.statements.StatementSlot slot : robot.getLinkedStation().getActiveActions()) {
         if (slot.statement != null
            && actionTag.equals(slot.statement.getUniqueTag())
            && slot.parameters.length > 0
            && slot.parameters[0] != null) {
            ItemStack stack = slot.parameters[0].getItemStack();
            if (!stack.isEmpty()) {
               return colourForMap(stack);
            }
         }
      }

      return DEFAULT_COLOUR;
   }

   private static void addMapStack(ItemStack stack, List<ZoneOverlay> overlays, Set<Integer> seenMaps) {
      if (stack.isEmpty() || !(stack.getItem() instanceof IMapLocation map)) {
         return;
      }

      IZone zone = map.getZone(stack);
      if (zone != null) {
         addZone(zone, colourForMap(stack), overlays, seenMaps);
      }
   }

   private static void addZone(IZone zone, int colour, List<ZoneOverlay> overlays, Set<Integer> seenMaps) {
      int id = System.identityHashCode(zone);
      if (seenMaps.add(id)) {
         overlays.add(new ZoneOverlay(zone, colour));
      }
   }

   private static int colourForMap(ItemStack stack) {
      CustomData data = stack.get(DataComponents.CUSTOM_DATA);
      if (data != null) {
         CompoundTag tag = data.copyTag();
         if (tag.contains("layerColour")) {
            return tag.getInt("layerColour").orElse(DEFAULT_COLOUR) | 0xFF000000;
         }

         int layer = tag.getInt("layer").orElse(-1);
         if (layer >= 0 && layer < 16) {
            return 0xFF000000 | DyeColor.byId(layer).getTextureDiffuseColor() & 0xFFFFFF;
         }
      }

      return DEFAULT_COLOUR;
   }

   private record ZoneOverlay(IZone zone, int colour) {
   }
}
