/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import buildcraft.lib.client.render.laser.BcLaserRenderer;
import buildcraft.lib.client.render.laser.LaserBatch;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.VecUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MarkerRenderer {
   public static final MarkerRenderer INSTANCE = new MarkerRenderer();
   private static final double RENDER_SCALE = 0.06230529595015576;
   private static final Vec3 VEC_HALF = new Vec3(0.5, 0.5, 0.5);
   private static PoseStack currentPoseStack;
   private static Vec3 currentCameraPos;
   private static Runnable volumeBoxRenderCallback;
   private static Predicate<Player> holdingConnectorCheck;

   public static void renderMarkers(PoseStack poseStack, Vec3 cameraPos) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         currentCameraPos = cameraPos;
         currentPoseStack = poseStack;
         LaserBatch.begin();

         try {
            for (MarkerCache<? extends MarkerSubCache<?>> cache : MarkerCache.CACHES) {
               UnmodifiableIterator var5 = cache.getSubCache(player.level()).getConnections().iterator();

               while (var5.hasNext()) {
                  MarkerConnection<?> connection = (MarkerConnection<?>)var5.next();
                  connection.renderInWorld();
               }
            }

            if (holdingConnectorCheck != null && holdingConnectorCheck.test(player)) {
               renderPossibleConnections(player);
            }

            renderVolumeBoxes();
         } finally {
            LaserBatch.end();
            currentPoseStack = null;
            currentCameraPos = null;
         }
      }
   }

   private static void renderPossibleConnections(Player player) {
      Set<MarkerPair> renderedPairs = new HashSet<>();

      for (MarkerCache<? extends MarkerSubCache<?>> cache : MarkerCache.CACHES) {
         MarkerSubCache<?> subCache = (MarkerSubCache<?>)cache.getSubCache(player.level());
         LaserData_BC8.LaserType laserType = subCache.getPossibleLaserType();
         if (laserType != null) {
            ImmutableList<BlockPos> allMarkers = subCache.getAllMarkers();
            UnmodifiableIterator var7 = allMarkers.iterator();

            while (var7.hasNext()) {
               BlockPos marker = (BlockPos)var7.next();
               ImmutableList<BlockPos> validTargets = subCache.getValidConnections(marker);
               UnmodifiableIterator var10 = validTargets.iterator();

               while (var10.hasNext()) {
                  BlockPos target = (BlockPos)var10.next();
                  if (renderedPairs.add(MarkerPair.of(marker, target))) {
                     Vec3 from = VecUtil.add(VEC_HALF, marker);
                     Vec3 to = VecUtil.add(VEC_HALF, target);
                     Vec3 fromOffset = offset(from, to);
                     Vec3 toOffset = offset(to, from);
                     LaserData_BC8 data = new LaserData_BC8(laserType, fromOffset, toOffset, 0.06230529595015576, false, false, 15);
                     BcLaserRenderer.renderLaserStatic(currentPoseStack, data, currentCameraPos);
                  }
               }
            }
         }
      }
   }

   private static Vec3 offset(Vec3 from, Vec3 to) {
      Vec3 dir = to.subtract(from).normalize();
      return from.add(VecUtil.scale(dir, 0.125));
   }

   /** Order-independent exact key for a candidate marker line. (The previous {@code a * 31 + b} long hash could
    * collide and silently skip a line for a frame; the boxed record costs the same as the boxed Long did.) */
   private record MarkerPair(long min, long max) {
      static MarkerPair of(BlockPos a, BlockPos b) {
         long ha = a.asLong();
         long hb = b.asLong();
         return ha < hb ? new MarkerPair(ha, hb) : new MarkerPair(hb, ha);
      }
   }

   private static void renderVolumeBoxes() {
      if (volumeBoxRenderCallback != null) {
         volumeBoxRenderCallback.run();
      }
   }

   public static void setVolumeBoxRenderCallback(Runnable callback) {
      volumeBoxRenderCallback = callback;
   }

   public static void setHoldingConnectorCheck(Predicate<Player> check) {
      holdingConnectorCheck = check;
   }

   public static PoseStack getPoseStack() {
      return currentPoseStack;
   }

   public static Vec3 getCameraPos() {
      return currentCameraPos;
   }
}
