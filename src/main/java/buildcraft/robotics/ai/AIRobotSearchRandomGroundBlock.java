package buildcraft.robotics.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.IBlockFilter;
import net.minecraft.core.BlockPos;

/** Picks random columns and scans downward for the first matching ground block under open air. */
public class AIRobotSearchRandomGroundBlock extends AIRobot {
   private static final int MAX_ATTEMPTS = 4096;
   private static final java.util.Random RANDOM = new java.util.Random();

   public BlockPos blockFound;
   private int range;
   private IBlockFilter filter;
   private IZone zone;
   private int attempts;

   public AIRobotSearchRandomGroundBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchRandomGroundBlock(EntityRobotBase robot, int range, IBlockFilter filter, IZone zone) {
      this(robot);
      this.range = range;
      this.filter = filter;
      this.zone = zone;
   }

   @Override
   public void update() {
      if (this.filter == null) {
         this.terminate();
         return;
      }

      this.attempts++;
      if (this.attempts > MAX_ATTEMPTS) {
         this.terminate();
         return;
      }

      int x;
      int z;
      if (this.zone == null) {
         double r = this.robot.level().getRandom().nextFloat() * this.range;
         float a = this.robot.level().getRandom().nextFloat() * 2.0F * (float)Math.PI;
         x = (int)(Math.cos(a) * r + this.robot.getX());
         z = (int)(Math.sin(a) * r + this.robot.getZ());
      } else {
         BlockPos b = this.zone.getRandomBlockPos(RANDOM);
         if (b == null) {
            this.terminate();
            return;
         }

         x = b.getX();
         z = b.getZ();
      }

      for (int y = this.robot.level().getMaxY(); y >= this.robot.level().getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         if (this.filter.matches(this.robot.level(), pos)) {
            this.blockFound = pos;
            this.terminate();
            return;
         } else if (!this.robot.level().getBlockState(pos).isAir()) {
            return;
         }
      }
   }

   @Override
   public boolean success() {
      return this.blockFound != null;
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 5L;
   }
}
