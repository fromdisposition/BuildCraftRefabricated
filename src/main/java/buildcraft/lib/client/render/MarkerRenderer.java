/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.render;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.VecUtil;

public class MarkerRenderer {
    public static final MarkerRenderer INSTANCE = new MarkerRenderer();

    private static final double RENDER_SCALE = 1 / 16.05;
    private static final Vec3 VEC_HALF = new Vec3(0.5, 0.5, 0.5);

    private static PoseStack currentPoseStack;
    private static Vec3 currentCameraPos;

    public static void renderMarkers(PoseStack poseStack, Vec3 cameraPos) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        currentCameraPos = cameraPos;
        currentPoseStack = poseStack;

        for (MarkerCache<? extends MarkerSubCache<?>> cache : MarkerCache.CACHES) {
            for (MarkerConnection<?> connection : cache.getSubCache(player.level()).getConnections()) {
                connection.renderInWorld();
            }
        }

        if (holdingConnectorCheck != null && holdingConnectorCheck.test(player)) {
            renderPossibleConnections(player);
        }

        renderVolumeBoxes();

        currentPoseStack = null;
        currentCameraPos = null;
    }

    private static void renderPossibleConnections(Player player) {

        Set<Long> renderedPairs = new HashSet<>();

        for (MarkerCache<? extends MarkerSubCache<?>> cache : MarkerCache.CACHES) {
            MarkerSubCache<?> subCache = cache.getSubCache(player.level());
            LaserType laserType = subCache.getPossibleLaserType();
            if (laserType == null) continue;

            ImmutableList<BlockPos> allMarkers = subCache.getAllMarkers();
            for (BlockPos marker : allMarkers) {
                ImmutableList<BlockPos> validTargets = subCache.getValidConnections(marker);
                for (BlockPos target : validTargets) {

                    long pairKey = pairKey(marker, target);
                    if (!renderedPairs.add(pairKey)) continue;

                    Vec3 from = VecUtil.add(VEC_HALF, marker);
                    Vec3 to = VecUtil.add(VEC_HALF, target);
                    Vec3 fromOffset = offset(from, to);
                    Vec3 toOffset = offset(to, from);
                    LaserData_BC8 data = new LaserData_BC8(laserType, fromOffset, toOffset, RENDER_SCALE,
                            false, false, 15);
                    LaserRenderer_BC8.renderLaserStatic(currentPoseStack, data, currentCameraPos);
                }
            }
        }
    }

    private static Vec3 offset(Vec3 from, Vec3 to) {
        Vec3 dir = to.subtract(from).normalize();
        return from.add(VecUtil.scale(dir, 0.125));
    }

    private static long pairKey(BlockPos a, BlockPos b) {
        long ha = a.asLong();
        long hb = b.asLong();
        return ha < hb ? (ha * 31 + hb) : (hb * 31 + ha);
    }

    private static void renderVolumeBoxes() {
        if (volumeBoxRenderCallback != null) {
            volumeBoxRenderCallback.run();
        }
    }

    private static Runnable volumeBoxRenderCallback;

    private static Predicate<Player> holdingConnectorCheck;

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
