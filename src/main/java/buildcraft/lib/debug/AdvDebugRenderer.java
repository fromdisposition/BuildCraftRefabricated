/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.debug;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class AdvDebugRenderer {
    private AdvDebugRenderer() {}

    public static void register() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {
            BlockPos target = BCAdvDebugging.INSTANCE.getClientTarget();
            if (target == null) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                return;
            }
            BlockEntity be = mc.level.getBlockEntity(target);
            if (!(be instanceof IAdvDebugTarget)) {
                BCAdvDebugging.INSTANCE.clear();
                return;
            }
            renderOptionalOverlay(be, context.poseStack(), context.levelState().cameraRenderState.pos);
        });
    }

    private static void renderOptionalOverlay(BlockEntity be, com.mojang.blaze3d.vertex.PoseStack poseStack, Vec3 cameraPos) {
        try {
            Class<?> quarryClass = Class.forName("buildcraft.builders.tile.TileQuarry");
            Class<?> laserClass = Class.forName("buildcraft.silicon.tile.TileLaser");
            if (quarryClass.isInstance(be)) {
                Class<?> renderer = Class.forName("buildcraft.builders.client.render.AdvDebuggerQuarry");
                renderer.getMethod("render", quarryClass, poseStack.getClass(),
                                net.minecraft.client.renderer.MultiBufferSource.BufferSource.class, Vec3.class)
                        .invoke(null, be, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), cameraPos);
            } else if (laserClass.isInstance(be)) {
                Class<?> renderer = Class.forName("buildcraft.silicon.client.render.AdvDebuggerLaser");
                renderer.getMethod("render", laserClass, poseStack.getClass(),
                                net.minecraft.client.renderer.MultiBufferSource.BufferSource.class, Vec3.class)
                        .invoke(null, be, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), cameraPos);
            }
        } catch (ReflectiveOperationException ignored) {

        }
    }
}
