/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
import buildcraft.lib.client.render.laser.LaserBatch;
import net.minecraft.client.Minecraft;
//? if >= 26.1.3 {
/*import net.minecraft.client.renderer.SubmitNodeStorage;*/
//?} else {
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class AdvDebugRenderer {
   private static boolean lookupsResolved;
   private static Class<?> quarryClass;
   private static Class<?> laserClass;
   private static MethodHandle quarryRender;
   private static MethodHandle laserRender;

   private AdvDebugRenderer() {
   }

   private static void resolveLookups() {
      if (lookupsResolved) {
         return;
      }

      lookupsResolved = true;
      MethodHandles.Lookup lookup = MethodHandles.publicLookup();

      try {
         quarryClass = Class.forName("buildcraft.builders.tile.TileQuarry");
         Class<?> renderer = Class.forName("buildcraft.builders.client.render.AdvDebuggerQuarry");
         //? if >= 26.1.3 {
         /*quarryRender = lookup.unreflect(renderer.getMethod("render", quarryClass, PoseStack.class, Vec3.class));*/
         //?} else {
         quarryRender = lookup.unreflect(renderer.getMethod("render", quarryClass, PoseStack.class, BufferSource.class, Vec3.class));
         //?}
      } catch (ReflectiveOperationException ignored) {
         quarryClass = null;
         quarryRender = null;
      }

      try {
         laserClass = Class.forName("buildcraft.silicon.tile.TileLaser");
         Class<?> renderer = Class.forName("buildcraft.silicon.client.render.AdvDebuggerLaser");
         //? if >= 26.1.3 {
         /*laserRender = lookup.unreflect(renderer.getMethod("render", laserClass, PoseStack.class, Vec3.class));*/
         //?} else {
         laserRender = lookup.unreflect(renderer.getMethod("render", laserClass, PoseStack.class, BufferSource.class, Vec3.class));
         //?}
      } catch (ReflectiveOperationException ignored) {
         laserClass = null;
         laserRender = null;
      }
   }

   public static void register() {
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register((AfterTranslucentFeatures)context -> {
         //? if >= 26.1.3 {
         /*LaserBatch.setNodeStorage((SubmitNodeStorage) context.submitNodeCollector());*/
         //?}
         BlockPos target = BCAdvDebugging.INSTANCE.getClientTarget();
         if (target != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
               BlockEntity be = mc.level.getBlockEntity(target);
               if (!(be instanceof IAdvDebugTarget)) {
                  BCAdvDebugging.INSTANCE.clear();
               } else {
                     renderOptionalOverlay(be, context.poseStack(), context.levelState().cameraRenderState.pos);
               }
            }
         }
      });
   }

   private static void renderOptionalOverlay(BlockEntity be, PoseStack poseStack, Vec3 cameraPos) {
      resolveLookups();
      try {
         //? if >= 26.1.3 {
         /*if (quarryRender != null && quarryClass != null && quarryClass.isInstance(be)) {
            quarryRender.invoke(be, poseStack, cameraPos);
         } else if (laserRender != null && laserClass != null && laserClass.isInstance(be)) {
            laserRender.invoke(be, poseStack, cameraPos);
         }*/
         //?} else {
         BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
         if (quarryRender != null && quarryClass != null && quarryClass.isInstance(be)) {
            quarryRender.invoke(be, poseStack, buffers, cameraPos);
         } else if (laserRender != null && laserClass != null && laserClass.isInstance(be)) {
            laserRender.invoke(be, poseStack, buffers, cameraPos);
         }
         //?}
      } catch (Throwable ignored) {
      }
   }
}
