package buildcraft.factory.client.render;

import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.client.fluid.BcFluidBerHelper;
import buildcraft.lib.client.fluid.BcFluidQuadEmitter;
import buildcraft.lib.client.fluid.FluidClientCache;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RenderHeatExchange extends BcBlockEntityRenderer<TileHeatExchange, HeatExchangeRenderState> {
   private static final Map<Direction, RenderHeatExchange.TankSideData> TANK_SIDES = new EnumMap<>(Direction.class);
   private static final BcFluidBerHelper.TankBounds TANK_BOTTOM = new BcFluidBerHelper.TankBounds(2.0F, 0.0F, 2.0F, 14.0F, 2.0F, 14.0F);
   private static final BcFluidBerHelper.TankBounds TANK_TOP = new BcFluidBerHelper.TankBounds(2.0F, 14.0F, 2.0F, 14.0F, 16.0F, 14.0F);

   public RenderHeatExchange(Context context) {
   }

   public HeatExchangeRenderState createRenderState() {
      return new HeatExchangeRenderState();
   }

   public void submit(HeatExchangeRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      TileHeatExchange tile = renderState.tile;
      if (tile != null) {
         Level level = tile.getLevel();
         if (level != null) {
            BlockPos pos = renderState.blockPos;
            if (tile.isStart()) {
               TileHeatExchange.ExchangeSectionStart section = (TileHeatExchange.ExchangeSectionStart)tile.getSection();
               if (section != null) {
                  TileHeatExchange.ExchangeSectionEnd sectionEnd = section.getEndSection();
                  if (sectionEnd == null) {
                     BlockState st = tile.getBlockState();
                     if (st.getBlock() instanceof BlockHeatExchange) {
                        Direction dir = ((Direction)st.getValue(BlockHeatExchange.FACING)).getCounterClockWise();

                        for (int i = 1; i < 6; i++) {
                           BlockEntity neighbor = level.getBlockEntity(pos.relative(dir, i));
                           if (neighbor instanceof TileHeatExchange other && other.isEnd()) {
                              sectionEnd = (TileHeatExchange.ExchangeSectionEnd)other.getSection();
                              tile.markCheckNeighbours();
                              break;
                           }

                           if (!(neighbor instanceof TileHeatExchange)) {
                              break;
                           }
                        }
                     }
                  }

                  BlockState state = tile.getBlockState();
                  if (state.getBlock() instanceof BlockHeatExchange) {
                     Direction facing = (Direction)state.getValue(BlockHeatExchange.FACING);
                     Direction face = facing.getCounterClockWise();
                     RenderHeatExchange.TankSideData sideTank = TANK_SIDES.get(face);
                     if (sideTank != null) {
                        int light = renderState.light;
                        poseStack.pushPose();
                        BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                        float partialTicks = renderState.partialTick;
                        BcFluidBerHelper.renderSmoothedFluid(section.smoothedTankInput, TANK_BOTTOM, poseStack, bufferSource, light, partialTicks);
                        BcFluidBerHelper.renderSmoothedFluid(section.smoothedTankOutput, sideTank.start, poseStack, bufferSource, light, partialTicks);
                        if (sectionEnd != null) {
                           BlockPos diff = sectionEnd.getTile().getBlockPos().subtract(tile.getBlockPos());
                           poseStack.translate(diff.getX(), diff.getY(), diff.getZ());
                           BcFluidBerHelper.renderSmoothedFluid(sectionEnd.smoothedTankOutput, TANK_TOP, poseStack, bufferSource, light, partialTicks);
                           BcFluidBerHelper.renderSmoothedFluid(sectionEnd.smoothedTankInput, sideTank.end, poseStack, bufferSource, light, partialTicks);
                           poseStack.translate(-diff.getX(), -diff.getY(), -diff.getZ());
                        }

                        int middles = section.middleCount;
                        if (middles > 0 && sectionEnd != null) {
                           TileHeatExchange.EnumProgressState progressState = section.getProgressState();
                           double progress = section.getProgress(partialTicks);
                           if (progress > 0.0) {
                              double length = middles + 2 - 0.25 - 0.02;
                              double p0 = 0.135;
                              double p1 = p0 + length - 0.01;
                              double progressStart = p0;
                              double progressEnd = p0 + length * progress;
                              boolean flip = progressState == TileHeatExchange.EnumProgressState.PREPARING;
                              flip ^= face.getAxisDirection() == AxisDirection.NEGATIVE;
                              if (flip) {
                                 progressStart = p1 - length * progress;
                                 progressEnd = p1;
                              }

                              BlockPos diff = BlockPos.ZERO;
                              if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
                                 diff = diff.relative(face, middles + 1);
                              }

                              double otherStart = flip ? p0 : p1 - length * progress;
                              double otherEnd = flip ? p0 + length * progress : p1;
                              Vec3 vDiff = Vec3.atLowerCornerOf(diff);
                              FluidStack coolantFluid = sectionEnd.smoothedTankInput.getFluid();
                              FluidStack heatantFluid = section.smoothedTankInput.getFluid();
                              if (!coolantFluid.isEmpty()) {
                                 renderFlow(
                                    vDiff, face, poseStack, bufferSource, progressStart + 0.01, progressEnd - 0.01, coolantFluid, 4, partialTicks, light
                                 );
                              }

                              if (!heatantFluid.isEmpty()) {
                                 renderFlow(vDiff, face.getOpposite(), poseStack, bufferSource, otherStart, otherEnd, heatantFluid, 2, partialTicks, light);
                              }
                           }
                        }

                        poseStack.popPose();
                     }
                  }
               }
            }
         }
      }
   }

   public boolean shouldRender(TileHeatExchange blockEntity, Vec3 cameraPos) {
      return blockEntity.isStart();
   }

   private static void renderFlow(
      Vec3 diff, Direction face, PoseStack poseStack, BufferSource bufferSource, double s, double e, FluidStack fluid, int point, float partialTicks, int light
   ) {
      if (!fluid.isEmpty()) {
         FluidClientCache.Appearance appearance = FluidClientCache.get(fluid);
         if (appearance != null) {
            TextureAtlasSprite sprite = appearance.sprite();
            float[] rgba = FluidUtilBC.vertexRgba(fluid);
            float r = rgba[0];
            float g = rgba[1];
            float b = rgba[2];
            float a = rgba[3];
            VertexConsumer buffer = bufferSource.getBuffer(FluidClientCache.renderType(appearance));
            int overlay = OverlayTexture.NO_OVERLAY;
            Level level = Minecraft.getInstance().level;
            double tickTime = level != null ? level.getGameTime() : 0.0;
            double offset = (tickTime + partialTicks) % 31.0 / 31.0;
            Direction renderFace = face;
            if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
               offset = -offset;
               renderFace = face.getOpposite();
            }

            Vec3 dirVec = Vec3.atLowerCornerOf(renderFace.getUnitVec3i());
            double ds = (point + 0.1) / 16.0;
            float minCross = (float)ds;
            float maxCross = (float)(1.0 - ds);
            diff = diff.subtract(dirVec.scale(offset));
            s += offset;
            e += offset;
            if (s < 0.0) {
               s++;
               e++;
               diff = diff.subtract(dirVec);
            }

            for (int i = 0; i <= e; i++) {
               if (i < s - 1.0) {
                  diff = diff.add(dirVec);
               } else {
                  poseStack.pushPose();
                  poseStack.translate(diff.x, diff.y, diff.z);
                  Pose pose = poseStack.last();
                  diff = diff.add(dirVec);
                  double s1 = s < i ? 0.0 : s % 1.0;
                  double e1 = e > i + 1 ? 1.0 : e % 1.0;
                  float flowMinX = minCross;
                  float flowMaxX = maxCross;
                  float flowMinY = minCross;
                  float flowMaxY = maxCross;
                  float flowMinZ = minCross;
                  float flowMaxZ = maxCross;
                  switch (renderFace.getAxis()) {
                     case X:
                        flowMinX = (float)s1;
                        flowMaxX = (float)e1;
                        break;
                     case Y:
                        flowMinY = (float)s1;
                        flowMaxY = (float)e1;
                        break;
                     case Z:
                        flowMinZ = (float)s1;
                        flowMaxZ = (float)e1;
                  }

                  boolean[] sides = new boolean[6];
                  Arrays.fill(sides, true);
                  if (s < i) {
                     sides[renderFace.getOpposite().ordinal()] = false;
                  }

                  if (e > i + 1) {
                     sides[renderFace.ordinal()] = false;
                  }

                  if (sides[Direction.NORTH.ordinal()]) {
                     quad(
                        pose,
                        buffer,
                        sprite,
                        flowMinX,
                        flowMaxY,
                        flowMinZ,
                        flowMaxX,
                        flowMaxY,
                        flowMinZ,
                        flowMaxX,
                        flowMinY,
                        flowMinZ,
                        flowMinX,
                        flowMinY,
                        flowMinZ,
                        0.0F,
                        0.0F,
                        -1.0F,
                        r,
                        g,
                        b,
                        a,
                        light,
                        overlay
                     );
                  }

                  if (sides[Direction.SOUTH.ordinal()]) {
                     quad(
                        pose,
                        buffer,
                        sprite,
                        flowMinX,
                        flowMinY,
                        flowMaxZ,
                        flowMaxX,
                        flowMinY,
                        flowMaxZ,
                        flowMaxX,
                        flowMaxY,
                        flowMaxZ,
                        flowMinX,
                        flowMaxY,
                        flowMaxZ,
                        0.0F,
                        0.0F,
                        1.0F,
                        r,
                        g,
                        b,
                        a,
                        light,
                        overlay
                     );
                  }

                  if (sides[Direction.WEST.ordinal()]) {
                     quad(
                        pose,
                        buffer,
                        sprite,
                        flowMinX,
                        flowMinY,
                        flowMinZ,
                        flowMinX,
                        flowMinY,
                        flowMaxZ,
                        flowMinX,
                        flowMaxY,
                        flowMaxZ,
                        flowMinX,
                        flowMaxY,
                        flowMinZ,
                        -1.0F,
                        0.0F,
                        0.0F,
                        r,
                        g,
                        b,
                        a,
                        light,
                        overlay
                     );
                  }

                  if (sides[Direction.EAST.ordinal()]) {
                     quad(
                        pose,
                        buffer,
                        sprite,
                        flowMaxX,
                        flowMaxY,
                        flowMinZ,
                        flowMaxX,
                        flowMaxY,
                        flowMaxZ,
                        flowMaxX,
                        flowMinY,
                        flowMaxZ,
                        flowMaxX,
                        flowMinY,
                        flowMinZ,
                        1.0F,
                        0.0F,
                        0.0F,
                        r,
                        g,
                        b,
                        a,
                        light,
                        overlay
                     );
                  }

                  if (sides[Direction.UP.ordinal()]) {
                     quadHorizontal(pose, buffer, sprite, flowMinX, flowMaxX, flowMaxZ, flowMinZ, flowMaxY, 0.0F, 1.0F, 0.0F, r, g, b, a, light, overlay);
                  }

                  if (sides[Direction.DOWN.ordinal()]) {
                     quadHorizontal(pose, buffer, sprite, flowMinX, flowMaxX, flowMaxZ, flowMinZ, flowMinY, 0.0F, -1.0F, 0.0F, r, g, b, a, light, overlay);
                  }

                  poseStack.popPose();
               }
            }
         }
      }
   }

   private static void quad(
      Pose pose,
      VertexConsumer builder,
      TextureAtlasSprite sprite,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float nx,
      float ny,
      float nz,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      BcFluidQuadEmitter.emitTankQuad(pose, builder, sprite, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, nx, ny, nz, r, g, b, a, light, overlay);
   }

   private static void quadHorizontal(
      Pose pose,
      VertexConsumer builder,
      TextureAtlasSprite sprite,
      float x1,
      float x2,
      float z1,
      float z2,
      float y,
      float nx,
      float ny,
      float nz,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      BcFluidQuadEmitter.emitTankHorizontal(pose, builder, sprite, x1, x2, z1, z2, y, nx, ny, nz, r, g, b, a, light, overlay);
   }

   static {
      BcFluidBerHelper.TankBounds start = new BcFluidBerHelper.TankBounds(0.0F, 4.0F, 4.0F, 2.0F, 12.0F, 12.0F);
      BcFluidBerHelper.TankBounds end = new BcFluidBerHelper.TankBounds(14.0F, 4.0F, 4.0F, 16.0F, 12.0F, 12.0F);
      RenderHeatExchange.TankSideData sides = new RenderHeatExchange.TankSideData(start, end);
      Direction face = Direction.EAST;

      for (int i = 0; i < 4; i++) {
         TANK_SIDES.put(face, sides);
         face = face.getClockWise();
         sides = sides.rotateY();
      }
   }

   static class TankSideData {
      final BcFluidBerHelper.TankBounds start;
      final BcFluidBerHelper.TankBounds end;

      TankSideData(BcFluidBerHelper.TankBounds start, BcFluidBerHelper.TankBounds end) {
         this.start = start;
         this.end = end;
      }

      RenderHeatExchange.TankSideData rotateY() {
         return new RenderHeatExchange.TankSideData(this.start.rotateY(), this.end.rotateY());
      }
   }
}
