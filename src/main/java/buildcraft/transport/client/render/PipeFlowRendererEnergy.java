/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.lib.client.fluid.BcFluidBoxQuads;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeEnergyDisplaySupport;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.EnumMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.Vec3;

public final class PipeFlowRendererEnergy {
   private static final ThreadLocal<double[]> TL_POWER = ThreadLocal.withInitial(() -> new double[6]);
   private static final ThreadLocal<double[]> TL_BOUNDS = ThreadLocal.withInitial(() -> new double[6]);
   /** The power-flow sprites are already coloured (cyan / overload red), so the cuboid vertices are plain white. */
   private static final float[] WHITE = {1.0F, 1.0F, 1.0F, 1.0F};

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

      // Emit the flow as flat cuboids straight into the buffer via the shared, allocation-free BcFluidBoxQuads
      // emitter (the same path the fluid-flow renderer uses). No MutableQuad / Vector3f / Vec3 / AABB are
      // allocated per face, so a screen full of powered pipes does not churn the GC on low-end machines. The
      // animation comes from the power_flow sprite itself being an animated atlas texture (frame strip + .mcmeta).
      double[] bounds = TL_BOUNDS.get();

      for (Direction side : Direction.values()) {
         if (pipe.isConnected(side) && power[side.ordinal()] > 0.0) {
            sideFlowBounds(side, power[side.ordinal()], centrePower, bounds);
            // Skip the inner face (toward the pipe centre) — it is hidden by the centre cuboid.
            emit(pose, buffer, sprite, bounds, 1 << side.getOpposite().ordinal(), packedLight);
         }
      }

      centreFlowBounds(centrePower, bounds);
      emit(pose, buffer, sprite, bounds, 0, packedLight);
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

   private static void emit(Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite, double[] b, int skipFaceMask, int packedLight) {
      BcFluidBoxQuads.emitCuboid(
         pose, buffer, sprite, (float)b[0], (float)b[1], (float)b[2], (float)b[3], (float)b[4], (float)b[5], skipFaceMask, WHITE, packedLight
      );
   }
}
