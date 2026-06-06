package buildcraft.transport.pipe;

import buildcraft.api.transport.pipe.ICustomPipeConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum DefaultPipeConnection implements ICustomPipeConnection {
   INSTANCE;

   @Override
   public float getExtension(Level world, BlockPos pos, Direction face, BlockState state) {
      VoxelShape shape = state.getCollisionShape(world, pos);
      if (shape.isEmpty()) {
         return 0.0F;
      }

      AABB bb = shape.bounds();
      switch (face) {
         case DOWN:
            return (float)bb.minY;
         case UP:
            return 1.0F - (float)bb.maxY;
         case NORTH:
            return (float)bb.minZ;
         case SOUTH:
            return 1.0F - (float)bb.maxZ;
         case WEST:
            return (float)bb.minX;
         case EAST:
            return 1.0F - (float)bb.maxX;
         default:
            return 0.0F;
      }
   }
}
