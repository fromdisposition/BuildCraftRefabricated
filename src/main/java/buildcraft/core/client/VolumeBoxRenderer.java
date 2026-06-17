/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client;

import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.client.render.laser.LaserBoxRenderer;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
//? if >= 26.1.3 {
//?} else {
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class VolumeBoxRenderer {
   private static final Map<UUID, LaserData_BC8.LaserType> LASER_TYPE_CACHE = new HashMap<>();
   private static final Map<UUID, Integer> LASER_TYPE_CACHE_KEY = new HashMap<>();

   public static void renderAll() {
      Player player = Minecraft.getInstance().player;
      PoseStack poseStack = MarkerRenderer.getPoseStack();
      Vec3 cameraPos = MarkerRenderer.getCameraPos();
      if (poseStack != null && cameraPos != null) {
         for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
            LaserData_BC8.LaserType type = pickLaserType(volumeBox, player);
            LaserBoxRenderer.renderLaserBoxStatic(poseStack, volumeBox.box, type, false, false, cameraPos);
         }

         if (player != null) {
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            //? if >= 26.1.3 {
            /*for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
               for (Addon addon : volumeBox.addons.values()) {
                  if (addon != null) {
                     @SuppressWarnings("unchecked")
                     IFastAddonRenderer<Addon> renderer = (IFastAddonRenderer<Addon>)addon.getRenderer();
                     renderer.renderAddonFast(addon, player, 1.0F, poseStack);
                  }
               }
            }*/
            //?} else {
            BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

            for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
               for (Addon addon : volumeBox.addons.values()) {
                  if (addon != null) {
                     @SuppressWarnings("unchecked")
                     IFastAddonRenderer<Addon> renderer = (IFastAddonRenderer<Addon>)addon.getRenderer();
                     renderer.renderAddonFast(addon, player, 1.0F, poseStack, bufferSource);
                  }
               }
            }

            bufferSource.endBatch();
            //?}
            poseStack.popPose();
         }
      }
   }

   private static LaserData_BC8.LaserType pickLaserType(VolumeBox volumeBox, Player player) {
      int cacheKey = volumeBox.locks.hashCode() + (player != null && volumeBox.isEditingBy(player) ? 31 : 0);
      Integer prevKey = LASER_TYPE_CACHE_KEY.get(volumeBox.id);
      if (prevKey != null && prevKey == cacheKey) {
         LaserData_BC8.LaserType cached = LASER_TYPE_CACHE.get(volumeBox.id);
         if (cached != null) {
            return cached;
         }
      }

      LaserData_BC8.LaserType type;
      if (player != null && volumeBox.isEditingBy(player)) {
         type = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
      } else {
         type = volumeBox.getLockTargetsStream()
            .filter(Lock.Target.TargetUsedByMachine.class::isInstance)
            .map(Lock.Target.TargetUsedByMachine.class::cast)
            .map(target -> target.type.getLaserType())
            .findFirst()
            .orElse(BuildCraftLaserManager.MARKER_VOLUME_CONNECTED);
      }

      LASER_TYPE_CACHE.put(volumeBox.id, type);
      LASER_TYPE_CACHE_KEY.put(volumeBox.id, cacheKey);
      return type;
   }
}
