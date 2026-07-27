/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.lib.client.fluid.BcFluidVertexEmitter;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeEnergyDisplaySupport;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.EnumMap;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

public final class PipeFlowRendererEnergy {
   private static final ThreadLocal<double[]> TL_POWER = ThreadLocal.withInitial(() -> new double[6]);
   private static final ThreadLocal<double[]> TL_BOUNDS = ThreadLocal.withInitial(() -> new double[6]);
   /** The power-flow sprites are already coloured (cyan / overload red), so the cuboid vertices are plain white. */
   private static final float[] WHITE = {1.0F, 1.0F, 1.0F, 1.0F};
   /** power_flow / _overload are 32x512 vertical strips = 16 frames of 32x32. */
   private static final int FRAMES = 16;

   private PipeFlowRendererEnergy() {
   }

   public static void render(
      IPipe pipe,
      EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections,
      Vec3 centreLast,
      Vec3 centre,
      boolean overloadSprite,
      float partialTicks,
      VertexConsumer buffer,
      Pose pose
   ) {
      render(pipe, sections, centreLast, centre, overloadSprite, partialTicks, buffer, pose, PipeRenderContext.getPackedLight());
   }

   public static void render(
      IPipe pipe,
      EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections,
      Vec3 centreLast,
      Vec3 centre,
      boolean overloadSprite,
      float partialTicks,
      VertexConsumer buffer,
      Pose pose,
      int packedLight
   ) {
      double centrePower = 0.0;
      double[] power = TL_POWER.get();

      for (Direction side : Direction.values()) {
         PipeEnergyDisplaySupport.DisplaySection section = sections.get(side);
         int i = side.ordinal();
         power[i] = (double)section.getDisplayPower() / MjAPI.MJ;
         centrePower = Math.max(centrePower, power[i]);
      }

      if (centrePower <= 0.0) {
         return;
      }

      TextureAtlasSprite sprite = overloadSprite ? BCTransportSprites.POWER_FLOW_OVERLOAD.getSprite() : BCTransportSprites.POWER_FLOW.getSprite();
      if (sprite == null) {
         return;
      }

      // Drive the flow animation ourselves. MC 26.2's per-tick atlas animation blit is not reflected through this
      // BER submit path (the fluid sprites are static for the same reason), so relying on an animated .mcmeta gave a
      // frozen frame. Instead the sprite is the full 32x512, 16-frame strip (its .mcmeta was removed) and we pick
      // the current frame from client game time, sampling only that frame's 1/16 V-band. Still allocation-free
      // (ThreadLocal scratch): the geometry is unchanged, only the sampled UV band shifts per frame.
      int frame = (int) ((pipe.getHolder().getPipeWorld().getGameTime() / 2L) % (long) FRAMES);
      double[] bounds = TL_BOUNDS.get();

      for (Direction side : Direction.values()) {
         if (pipe.isConnected(side) && power[side.ordinal()] > 0.0) {
            sideFlowBounds(side, power[side.ordinal()], centrePower, bounds);
            // Skip the inner face (toward the pipe centre) — it is hidden by the centre cuboid.
            emitFrameCuboid(pose, buffer, sprite, bounds, 1 << side.getOpposite().ordinal(), frame, packedLight);
         }
      }

      centreFlowBounds(centrePower, bounds);
      emitFrameCuboid(pose, buffer, sprite, bounds, 0, frame, packedLight);
   }

   /**
    * An arm cuboid running from the pipe centre out to {@code side}. The cross-section scales with this side's
    * power (a thin core at low throughput, near the bore at high), capped at 0.24 = the fluid ARM_RADIUS so it
    * stays inside the pipe wall; the centre end is pulled back as the centre cuboid grows. Writes minX,minY,minZ,
    * maxX,maxY,maxZ into {@code out}.
    */
   private static void sideFlowBounds(Direction side, double power, double centrePower, double[] out) {
      double p = Math.min(Math.max(power, 0.0), 1.0);
      double c = Math.min(Math.max(centrePower, 0.0), 1.0);
      double radius = 0.24 * p;
      double centreRadius = 0.252 - 0.24 * c;
      double axisCentreOff = 0.375 - centreRadius / 2.0;
      double axisRadius = 0.125 + centreRadius / 2.0;
      double cx = 0.5 + side.getStepX() * axisCentreOff;
      double cy = 0.5 + side.getStepY() * axisCentreOff;
      double cz = 0.5 + side.getStepZ() * axisCentreOff;
      double rx = side.getAxis() == Axis.X ? axisRadius : radius;
      double ry = side.getAxis() == Axis.Y ? axisRadius : radius;
      double rz = side.getAxis() == Axis.Z ? axisRadius : radius;
      out[0] = cx - rx;
      out[1] = cy - ry;
      out[2] = cz - rz;
      out[3] = cx + rx;
      out[4] = cy + ry;
      out[5] = cz + rz;
   }

   /** The centre cuboid, sized by the maximum power passing through the pipe (clamped inside the pipe wall). */
   private static void centreFlowBounds(double centrePower, double[] out) {
      double radius = 0.24 * Math.min(Math.max(centrePower, 0.0), 1.0);
      out[0] = 0.5 - radius;
      out[1] = 0.5 - radius;
      out[2] = 0.5 - radius;
      out[3] = 0.5 + radius;
      out[4] = 0.5 + radius;
      out[5] = 0.5 + radius;
   }

   /**
    * Cuboid emit mirroring {@code BcFluidBoxQuads.emitCuboid}, but sampling only the current animation frame's
    * 1/16 V-band of the strip sprite. Kept local so the shared fluid emitter (used by tanks / heat exchange) is
    * untouched. Vertices go straight through {@link BcFluidVertexEmitter}; no per-face allocation.
    */
   private static void emitFrameCuboid(Pose pose, VertexConsumer buffer, TextureAtlasSprite s, double[] b, int skipFaceMask, int frame, int packedLight) {
      float minX = (float)b[0], minY = (float)b[1], minZ = (float)b[2];
      float maxX = (float)b[3], maxY = (float)b[4], maxZ = (float)b[5];
      int overlay = OverlayTexture.NO_OVERLAY;
      if ((skipFaceMask & 1 << Direction.DOWN.ordinal()) == 0) {
         emitHFrame(pose, buffer, s, minX, maxX, minZ, maxZ, minY, 0.0F, -1.0F, 0.0F, frame, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.UP.ordinal()) == 0) {
         emitHFrame(pose, buffer, s, minX, maxX, maxZ, minZ, maxY, 0.0F, 1.0F, 0.0F, frame, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.NORTH.ordinal()) == 0) {
         emitDFrame(pose, buffer, s, Direction.NORTH, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, frame, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.SOUTH.ordinal()) == 0) {
         emitDFrame(pose, buffer, s, Direction.SOUTH, minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, frame, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.WEST.ordinal()) == 0) {
         emitDFrame(pose, buffer, s, Direction.WEST, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, frame, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.EAST.ordinal()) == 0) {
         emitDFrame(pose, buffer, s, Direction.EAST, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, frame, packedLight, overlay);
      }
   }

   private static void emitHFrame(
      Pose pose, VertexConsumer buffer, TextureAtlasSprite s, float x0, float x1, float z0, float z1, float y, float nx, float ny, float nz, int frame, int light, int overlay
   ) {
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
         pose, buffer, s,
         x0, y, z0, s.getU(x0), frameV(s, z0, frame),
         x1, y, z0, s.getU(x1), frameV(s, z0, frame),
         x1, y, z1, s.getU(x1), frameV(s, z1, frame),
         x0, y, z1, s.getU(x0), frameV(s, z1, frame),
         nx, ny, nz, WHITE[0], WHITE[1], WHITE[2], WHITE[3], light, overlay
      );
   }

   private static void emitDFrame(
      Pose pose, VertexConsumer buffer, TextureAtlasSprite s, Direction face,
      float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int frame, int light, int overlay
   ) {
      float nx = face.getStepX();
      float ny = face.getStepY();
      float nz = face.getStepZ();
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
         pose, buffer, s,
         x1, y1, z1, faceU(s, face, x1, z1), faceV(s, face, y1, z1, frame),
         x2, y2, z2, faceU(s, face, x2, z2), faceV(s, face, y2, z2, frame),
         x3, y3, z3, faceU(s, face, x3, z3), faceV(s, face, y3, z3, frame),
         x4, y4, z4, faceU(s, face, x4, z4), faceV(s, face, y4, z4, frame),
         nx, ny, nz, WHITE[0], WHITE[1], WHITE[2], WHITE[3], light, overlay
      );
   }

   /** Sample only the current frame's 1/16 V-band (the sprite is the full 16-frame strip). */
   private static float frameV(TextureAtlasSprite s, float coord, int frame) {
      return s.getV((coord + frame) / FRAMES);
   }

   private static float faceU(TextureAtlasSprite s, Direction face, float x, float z) {
      return switch (face) {
         case EAST, WEST -> s.getU(z);
         default -> s.getU(x);
      };
   }

   private static float faceV(TextureAtlasSprite s, Direction face, float y, float z, int frame) {
      float coord = switch (face) {
         case UP, DOWN -> z;
         default -> y;
      };
      return frameV(s, coord, frame);
   }
}
