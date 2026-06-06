package buildcraft.builders.client.render.pip;

import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.Template;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

final class TemplateGhostGeometry {
   private TemplateGhostGeometry() {
   }

   static boolean cellFilled(Template template, BlockPos size, int x, int y, int z) {
      return x >= 0 && y >= 0 && z >= 0 && x < size.getX() && y < size.getY() && z < size.getZ()
         ? template.data != null && template.data.get(Snapshot.posToIndex(size, x, y, z))
         : false;
   }

   static EnumSet<Direction> visibleFaces(Template template, BlockPos size, int x, int y, int z) {
      EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);

      for (Direction face : Direction.values()) {
         if (!cellFilled(template, size, x + face.getStepX(), y + face.getStepY(), z + face.getStepZ())) {
            faces.add(face);
         }
      }

      return faces;
   }
}
