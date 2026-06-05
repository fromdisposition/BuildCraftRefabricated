/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.client.render;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import buildcraft.fabric.event.SubscribeEvent;
import buildcraft.fabric.client.event.RenderLevelStageEvent;

import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;

import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.tile.TileLaser;

public class RenderLaser {
    private static final int MAX_POWER = BuildCraftLaserManager.POWERS.length - 1;

    private static final Set<TileLaser> ACTIVE_LASERS = Collections.newSetFromMap(new WeakHashMap<>());

    public static void addLaser(TileLaser laser) {
        ACTIVE_LASERS.add(laser);
    }

    public static void removeLaser(TileLaser laser) {
        ACTIVE_LASERS.remove(laser);
    }

    public static int getActiveCount() {
        return ACTIVE_LASERS.size();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (ACTIVE_LASERS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ACTIVE_LASERS.removeIf(laser -> laser.isRemoved() || laser.getLevel() != mc.level);

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;

        for (TileLaser laser : ACTIVE_LASERS) {
            Vec3 target = laser.laserPos;
            if (target == null) continue;

            long avg = laser.getAverageClient();
            if (avg <= 200_000) continue;
            avg += 200_000;

            Direction side = laser.getBlockState().getValue(BlockLaser.FACING);
            Vec3 offset = new Vec3(0.5, 0.5, 0.5).add(
                Vec3.atLowerCornerOf(side.getUnitVec3i()).scale(4 / 16D));
            Vec3 start = Vec3.atLowerCornerOf(laser.getBlockPos()).add(offset);

            int index = (int) (avg * MAX_POWER / laser.getMaxPowerPerTick());
            if (index > MAX_POWER) index = MAX_POWER;

            LaserData_BC8 data = new LaserData_BC8(
                BuildCraftLaserManager.POWERS[index], start, target, 1.0 / 16.0,
                false, false, 15);

            LaserRenderer_BC8.renderLaserStatic(poseStack, data, cameraPos);
        }
    }
}

