/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.core.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8.LaserType;

import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;

public class VolumeBoxRenderer {

    public static void renderAll() {
        Player player = Minecraft.getInstance().player;
        PoseStack poseStack = MarkerRenderer.getPoseStack();
        Vec3 cameraPos = MarkerRenderer.getCameraPos();
        if (poseStack == null || cameraPos == null) return;

        for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
            LaserType type = pickLaserType(volumeBox, player);
            LaserBoxRenderer.renderLaserBoxStatic(
                    poseStack,
                    volumeBox.box,
                    type,
                    false, false,
                    cameraPos);
        }

        if (player == null) return;

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
            for (Addon addon : volumeBox.addons.values()) {
                if (addon == null) continue;
                @SuppressWarnings("unchecked")
                IFastAddonRenderer<Addon> renderer = (IFastAddonRenderer<Addon>) addon.getRenderer();
                renderer.renderAddonFast(addon, player, 1.0f, poseStack, bufferSource);
            }
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private static LaserType pickLaserType(VolumeBox volumeBox, Player player) {
        if (player != null && volumeBox.isEditingBy(player)) {
            return BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
        }
        return volumeBox.getLockTargetsStream()
                .filter(Lock.Target.TargetUsedByMachine.class::isInstance)
                .map(Lock.Target.TargetUsedByMachine.class::cast)
                .map(target -> target.type.getLaserType())
                .findFirst()
                .orElse(BuildCraftLaserManager.MARKER_VOLUME_CONNECTED);
    }
}
