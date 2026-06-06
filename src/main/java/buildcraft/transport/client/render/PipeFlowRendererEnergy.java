package buildcraft.transport.client.render;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeEnergyDisplaySupport;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.EnumMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class PipeFlowRendererEnergy {
   private static final ThreadLocal<double[]> TL_POWER = ThreadLocal.withInitial(() -> new double[6]);
   private static final ThreadLocal<ModelUtil.UvFaceData> TL_UVS = ThreadLocal.withInitial(ModelUtil.UvFaceData::new);
   private static final ThreadLocal<MutableQuad> TL_QUAD = ThreadLocal.withInitial(MutableQuad::new);
   private static final ThreadLocal<Vector3f> TL_FROM = ThreadLocal.withInitial(Vector3f::new);
   private static final ThreadLocal<Vector3f> TL_TO = ThreadLocal.withInitial(Vector3f::new);

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

      if (!(centrePower <= 0.0)) {
         TextureAtlasSprite sprite = overloadSprite ? BCTransportSprites.POWER_FLOW_OVERLOAD.getSprite() : BCTransportSprites.POWER_FLOW.getSprite();
         if (sprite != null) {
            for (Direction side : Direction.values()) {
               if (pipe.isConnected(side)) {
                  PipeEnergyDisplaySupport.DisplaySection section = sections.get(side);
                  double offset = computeOffset(section.getClientDisplayFlowLast(), section.getClientDisplayFlow(), partialTicks);
                  renderSidePower(side, power[side.ordinal()], centrePower, offset, sprite, buffer, pose, packedLight);
               }
            }

            double offsetX = computeOffset(centreLast.x, centre.x, partialTicks);
            double offsetY = computeOffset(centreLast.y, centre.y, partialTicks);
            double offsetZ = computeOffset(centreLast.z, centre.z, partialTicks);
            renderCentrePower(centrePower, offsetX, offsetY, offsetZ, sprite, buffer, pose, packedLight);
         }
      }
   }

   static double computeOffset(double tick0, double tick1, float partialTicks) {
      if (tick0 + 8.0 < tick1) {
         tick0 += 16.0;
      } else if (tick1 + 8.0 < tick0) {
         tick1 += 16.0;
      }

      double offset = MathUtil.interp(partialTicks, tick0, tick1);
      if (offset >= 16.0) {
         offset -= 16.0;
      }

      return offset;
   }

   private static void renderSidePower(
      Direction side, double power, double centrePower, double offset, TextureAtlasSprite sprite, VertexConsumer buffer, Pose pose, int packedLight
   ) {
      if (!(power < 0.0)) {
         AABB box = sideFlowBox(side, power, centrePower);
         Vec3 offsetVec = VecUtil.offset(Vec3.ZERO, side, offset * side.getAxisDirection().getStep() / 32.0);
         ModelUtil.UvFaceData uvs = TL_UVS.get();

         for (Direction face : Direction.values()) {
            if (face != side.getOpposite()) {
               renderScrollingBox(box, offsetVec, face, uvs, sprite, buffer, pose, packedLight);
            }
         }
      }
   }

   static AABB sideFlowBox(Direction side, double power, double centrePower) {
      double p = Math.min(Math.max(power, 0.0), 1.0);
      double c = Math.min(Math.max(centrePower, 0.0), 1.0);
      double radius = 0.248 * p;
      double centreRadius = 0.252 - 0.248 * c;
      Vec3 centre = VecUtil.offset(VecUtil.VEC_HALF, side, 0.375 - centreRadius / 2.0);
      Vec3 radiusV = new Vec3(radius, radius, radius);
      radiusV = VecUtil.replaceValue(radiusV, side.getAxis(), 0.125 + centreRadius / 2.0);
      return new AABB(centre.x - radiusV.x, centre.y - radiusV.y, centre.z - radiusV.z, centre.x + radiusV.x, centre.y + radiusV.y, centre.z + radiusV.z);
   }

   private static void renderCentrePower(
      double centrePower, double offsetX, double offsetY, double offsetZ, TextureAtlasSprite sprite, VertexConsumer buffer, Pose pose, int packedLight
   ) {
      double radius = 0.248 * centrePower;
      Vec3 centre = VecUtil.VEC_HALF.add(offsetX / 16.0, offsetY / 16.0, offsetZ / 16.0);
      Vec3 radiusV = new Vec3(radius, radius, radius);
      AABB box = new AABB(centre.x - radiusV.x, centre.y - radiusV.y, centre.z - radiusV.z, centre.x + radiusV.x, centre.y + radiusV.y, centre.z + radiusV.z);
      ModelUtil.UvFaceData uvs = TL_UVS.get();

      for (Direction face : Direction.values()) {
         renderScrollingBox(box, Vec3.ZERO, face, uvs, sprite, buffer, pose, packedLight);
      }
   }

   private static void renderScrollingBox(
      AABB box, Vec3 offset, Direction face, ModelUtil.UvFaceData uvs, TextureAtlasSprite sprite, VertexConsumer buffer, Pose pose, int packedLight
   ) {
      Vector3f from = TL_FROM.get();
      from.set((float)(box.minX + offset.x), (float)(box.minY + offset.y), (float)(box.minZ + offset.z));
      Vector3f to = TL_TO.get();
      to.set((float)(box.maxX + offset.x), (float)(box.maxY + offset.y), (float)(box.maxZ + offset.z));
      MutableQuad quad = TL_QUAD.get();
      quad.copyFrom(ModelUtil.createFace(face, from, to, uvs));
      quad.texFromSprite(sprite);
      quad.lighti(packedLight);
      quad.render(pose, buffer);
   }
}
