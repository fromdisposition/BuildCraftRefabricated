package buildcraft.robotics.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.path.BlockScannerExpanding;
import buildcraft.robotics.path.IBlockFilter;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/**
 * Scans for a block matching a filter, expanding outwards (or randomly within the work zone). The scan runs
 * synchronously on the server thread in a per-tick budget (Fabric-safe replacement for the 1.7.10 background job).
 */
public class AIRobotSearchBlock extends AIRobot {
   private static final int SCAN_BUDGET = 200;
   private static final java.util.Random RANDOM = new java.util.Random();

   public BlockPos blockFound;
   private IBlockFilter filter;
   private IZone zone;
   private Iterator<BlockPos> blockIter;
   private BlockPos origin;
   private boolean random;
   private double maxDistanceToEnd;
   private int randomAttempts;

   public AIRobotSearchBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchBlock(EntityRobotBase robot, boolean random, IBlockFilter filter, double maxDistanceToEnd) {
      super(robot);
      this.filter = filter;
      this.random = random;
      this.maxDistanceToEnd = maxDistanceToEnd;
      this.zone = robot.getZoneToWork();
   }

   @Override
   public void start() {
      this.origin = this.robot.blockPosition();
      if (!this.random) {
         this.blockIter = new BlockScannerExpanding().iterator();
      }
   }

   @Override
   public void update() {
      if (this.filter == null) {
         this.terminate();
         return;
      }

      if (this.random) {
         this.scanRandom();
      } else {
         this.scanExpanding();
      }
   }

   private void scanExpanding() {
      for (int i = 0; i < SCAN_BUDGET; i++) {
         if (this.blockIter == null || !this.blockIter.hasNext()) {
            this.terminate();
            return;
         }

         BlockPos candidate = this.origin.offset(this.blockIter.next());
         if (this.accept(candidate)) {
            this.blockFound = candidate;
            this.terminate();
            return;
         }
      }
   }

   private void scanRandom() {
      for (int i = 0; i < SCAN_BUDGET; i++) {
         this.randomAttempts++;
         if (this.randomAttempts > 4096) {
            this.terminate();
            return;
         }

         BlockPos candidate;
         if (this.zone != null) {
            candidate = this.zone.getRandomBlockPos(RANDOM);
            if (candidate == null) {
               this.terminate();
               return;
            }
         } else {
            double r = this.robot.level().getRandom().nextFloat() * 64.0;
            float a = this.robot.level().getRandom().nextFloat() * 2.0F * (float)Math.PI;
            int x = (int)(Math.cos(a) * r + this.origin.getX());
            int z = (int)(Math.sin(a) * r + this.origin.getZ());
            candidate = new BlockPos(x, this.origin.getY(), z);
         }

         if (this.accept(candidate)) {
            this.blockFound = candidate;
            this.terminate();
            return;
         }
      }
   }

   private boolean accept(BlockPos pos) {
      if (this.zone != null && !this.zone.contains(Vec3.atCenterOf(pos))) {
         return false;
      }

      if (this.maxDistanceToEnd > 0.0 && this.robot.position().distanceToSqr(Vec3.atCenterOf(pos)) > this.maxDistanceToEnd * this.maxDistanceToEnd) {
         return false;
      }

      if (this.robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
         return false;
      }

      return this.filter.matches(this.robot.level(), pos);
   }

   public boolean takeResource() {
      return this.robot.getRegistry().take(new ResourceIdBlock(this.blockFound), this.robot);
   }

   public void releaseResource() {
      if (this.blockFound != null) {
         this.robot.getRegistry().release(new ResourceIdBlock(this.blockFound));
      }
   }

   @Override
   public boolean success() {
      return this.blockFound != null;
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.blockFound != null) {
         nbt.putIntArray("blockFound", new int[]{this.blockFound.getX(), this.blockFound.getY(), this.blockFound.getZ()});
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = nbt.getIntArray("blockFound").orElse(new int[0]);
      if (arr.length == 3) {
         this.blockFound = new BlockPos(arr[0], arr[1], arr[2]);
      }
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 5L;
   }
}
