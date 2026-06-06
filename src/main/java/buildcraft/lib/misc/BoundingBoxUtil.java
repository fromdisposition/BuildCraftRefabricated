package buildcraft.lib.misc;

import buildcraft.api.core.IBox;
import buildcraft.lib.misc.data.Box;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BoundingBoxUtil {
   public static AABB makeFrom(BlockPos additional, @Nullable IBox box) {
      if (box == null) {
         return new AABB(additional);
      }

      BlockPos min = VecUtil.min(box.min(), additional);
      BlockPos max = VecUtil.max(box.max(), additional);
      return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
   }

   public static AABB makeFrom(BlockPos primary, BlockPos... additional) {
      Box box = new Box(primary, primary);

      for (BlockPos a : additional) {
         box.extendToEncompass(a);
      }

      return box.getBoundingBox();
   }

   public static AABB makeFrom(BlockPos additional, @Nullable IBox box1, @Nullable IBox box2) {
      if (box1 == null) {
         return makeFrom(additional, box2);
      }

      if (box2 == null) {
         return makeFrom(additional, box1);
      }

      BlockPos min = VecUtil.min(box1.min(), box2.min(), additional);
      BlockPos max = VecUtil.max(box1.max(), box2.max(), additional);
      return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
   }

   public static AABB makeFrom(Vec3 from, Vec3 to) {
      return new AABB(from.x, from.y, from.z, to.x, to.y, to.z);
   }

   public static AABB makeFrom(Vec3 from, Vec3 to, double radius) {
      return makeFrom(from, to).inflate(radius);
   }

   public static AABB makeAround(Vec3 around, double radius) {
      return new AABB(around.x, around.y, around.z, around.x, around.y, around.z).inflate(radius);
   }

   public static AABB makeFrom(BlockPos pos, @Nullable IBox box, @Nullable Collection<BlockPos> additional) {
      BlockPos min = box == null ? pos : VecUtil.min(box.min(), pos);
      BlockPos max = box == null ? pos : VecUtil.max(box.max(), pos);
      if (additional != null) {
         for (BlockPos p : additional) {
            min = VecUtil.min(min, p);
            max = VecUtil.max(max, p);
         }
      }

      return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
   }

   public static AABB extrudeFace(BlockPos pos, Direction face, double depth) {
      Vec3 from = Vec3.atLowerCornerOf(pos);
      Vec3 to = Vec3.atLowerCornerOf(pos).add(1.0, 1.0, 1.0);
      Axis axis = face.getAxis();
      if (face.getAxisDirection() == AxisDirection.POSITIVE) {
         from = VecUtil.replaceValue(from, axis, VecUtil.getValue(from, axis) + 1.0);
         to = VecUtil.replaceValue(to, axis, VecUtil.getValue(to, axis) + depth);
      } else {
         to = VecUtil.replaceValue(to, axis, VecUtil.getValue(to, axis) - 1.0);
         from = VecUtil.replaceValue(from, axis, VecUtil.getValue(from, axis) - depth);
      }

      return makeFrom(from, to);
   }
}
