package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.model.MutableVertex;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.core.Direction;

public class RenderPartCube {
   public final MutableVertex center = new MutableVertex();
   public double sizeX = 0.0625;
   public double sizeY = 0.0625;
   public double sizeZ = 0.0625;

   public RenderPartCube() {
      this(0.0625, 0.0625, 0.0625);
   }

   public RenderPartCube(double x, double y, double z) {
      this.center.positiond(x, y, z);
   }

   public void render(Pose pose, VertexConsumer consumer) {
      this.render(pose, consumer, null);
   }

   public void render(Pose pose, VertexConsumer consumer, Direction skipFace) {
      float x = this.center.position_x;
      float y = this.center.position_y;
      float z = this.center.position_z;
      float rX = (float)(this.sizeX / 2.0);
      float rY = (float)(this.sizeY / 2.0);
      float rZ = (float)(this.sizeZ / 2.0);
      int r = this.center.colour_r;
      int g = this.center.colour_g;
      int b = this.center.colour_b;
      int a = this.center.colour_a;
      if (skipFace != Direction.UP) {
         emit(pose, consumer, x - rX, y + rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y + rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y + rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x - rX, y + rY, z - rZ, r, g, b, a);
      }

      if (skipFace != Direction.DOWN) {
         emit(pose, consumer, x - rX, y - rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y - rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y - rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x - rX, y - rY, z + rZ, r, g, b, a);
      }

      if (skipFace != Direction.WEST) {
         emit(pose, consumer, x - rX, y - rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x - rX, y + rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x - rX, y + rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x - rX, y - rY, z - rZ, r, g, b, a);
      }

      if (skipFace != Direction.EAST) {
         emit(pose, consumer, x + rX, y - rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y + rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y + rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y - rY, z + rZ, r, g, b, a);
      }

      if (skipFace != Direction.NORTH) {
         emit(pose, consumer, x - rX, y - rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x - rX, y + rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y + rY, z - rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y - rY, z - rZ, r, g, b, a);
      }

      if (skipFace != Direction.SOUTH) {
         emit(pose, consumer, x + rX, y - rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x + rX, y + rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x - rX, y + rY, z + rZ, r, g, b, a);
         emit(pose, consumer, x - rX, y - rY, z + rZ, r, g, b, a);
      }
   }

   private static void emit(Pose pose, VertexConsumer consumer, float x, float y, float z, int r, int g, int b, int a) {
      consumer.addVertex(pose, x, y, z).setColor(r, g, b, a);
   }
}
