package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.PathFinding;
import java.util.LinkedList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

/**
 * Walks the robot to a target block, navigating around obstacles with A*. The path search runs synchronously on the
 * server thread in {@link PathFinding#PATH_ITERATIONS}-sized increments, which is Fabric-safe (no off-thread world
 * access, unlike the original 1.7.10 background job).
 */
public class AIRobotGotoBlock extends AIRobotGoto {
   private PathFinding pathSearch;
   private LinkedList<BlockPos> path;
   private int finalX;
   private int finalY;
   private int finalZ;
   private double maxDistance;
   private BlockPos lastBlockInPath;
   private boolean loadedFromNBT;

   public AIRobotGotoBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z) {
      this(robot);
      this.finalX = x;
      this.finalY = y;
      this.finalZ = z;
   }

   public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z, double maxDistance) {
      this(robot, x, y, z);
      this.maxDistance = maxDistance;
   }

   public AIRobotGotoBlock(EntityRobotBase robot, LinkedList<BlockPos> path) {
      this(robot);
      this.path = path;
      BlockPos last = path.getLast();
      this.finalX = last.getX();
      this.finalY = last.getY();
      this.finalZ = last.getZ();
      this.setNextInPath();
   }

   @Override
   public void start() {
      this.robot.undock();
   }

   @Override
   public void update() {
      if (this.loadedFromNBT) {
         this.setNextInPath();
         this.loadedFromNBT = false;
      }

      if (this.path == null && this.pathSearch == null) {
         BlockPos start = this.robot.blockPosition();
         BlockPos goal = new BlockPos(this.finalX, this.finalY, this.finalZ);
         this.pathSearch = new PathFinding(this.robot.level(), start, goal, this.maxDistance, 96.0F);
      } else if (this.path != null) {
         if (!this.robot.isMoving()) {
            if (!this.path.isEmpty()) {
               this.path.removeFirst();
            }

            this.setNextInPath();
         }
      } else {
         this.pathSearch.iterate(PathFinding.PATH_ITERATIONS);
         if (this.pathSearch.isDone()) {
            this.path = this.pathSearch.getResult();
            if (this.path.isEmpty()) {
               this.setSuccess(false);
               this.terminate();
               return;
            }

            this.lastBlockInPath = this.path.getLast();
            this.setNextInPath();
         }
      }

      if (this.path != null && this.path.isEmpty()) {
         this.clearDestination(this.robot);
         if (this.lastBlockInPath != null) {
            this.robot.setPos(this.lastBlockInPath.getX() + 0.5, this.lastBlockInPath.getY() + 0.5, this.lastBlockInPath.getZ() + 0.5);
         }

         this.terminate();
      }
   }

   private void setNextInPath() {
      if (this.path != null && !this.path.isEmpty()) {
         BlockPos next = this.path.getFirst();
         if (PathFinding.isSoftBlock(this.robot.level(), next) || next.equals(this.robot.blockPosition())) {
            this.setDestination(this.robot, next.getX() + 0.5, next.getY() + 0.5, next.getZ() + 0.5);
            this.robot.aimItemAt(next);
         } else {
            this.path = null;
            this.pathSearch = null;
            this.clearDestination(this.robot);
         }
      }
   }

   @Override
   public void end() {
      this.clearDestination(this.robot);
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      nbt.putInt("finalX", this.finalX);
      nbt.putInt("finalY", this.finalY);
      nbt.putInt("finalZ", this.finalZ);
      nbt.putDouble("maxDistance", this.maxDistance);
      if (this.path != null) {
         ListTag pathList = new ListTag();
         for (BlockPos pos : this.path) {
            CompoundTag sub = new CompoundTag();
            sub.putInt("x", pos.getX());
            sub.putInt("y", pos.getY());
            sub.putInt("z", pos.getZ());
            pathList.add(sub);
         }

         nbt.put("path", pathList);
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      this.finalX = nbt.getInt("finalX").orElse(0);
      this.finalY = nbt.getInt("finalY").orElse(0);
      this.finalZ = nbt.getInt("finalZ").orElse(0);
      this.maxDistance = nbt.getDouble("maxDistance").orElse(0.0);
      if (nbt.contains("path")) {
         ListTag pathList = nbt.getListOrEmpty("path");
         this.path = new LinkedList<>();
         for (int i = 0; i < pathList.size(); i++) {
            if (pathList.get(i) instanceof CompoundTag sub) {
               this.path.add(new BlockPos(sub.getInt("x").orElse(0), sub.getInt("y").orElse(0), sub.getInt("z").orElse(0)));
            }
         }
      }

      this.loadedFromNBT = true;
   }
}
