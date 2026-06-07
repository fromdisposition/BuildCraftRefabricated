package buildcraft.robotics.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.IEntityFilter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/** Finds the nearest living/world entity matching a filter within range (and optional zone). */
public class AIRobotSearchEntity extends AIRobot {
   public Entity target;
   private float maxRange;
   private IZone zone;
   private IEntityFilter filter;

   public AIRobotSearchEntity(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchEntity(EntityRobotBase robot, IEntityFilter filter, float maxRange, IZone zone) {
      this(robot);
      this.filter = filter;
      this.maxRange = maxRange;
      this.zone = zone;
   }

   @Override
   public void start() {
      double best = Double.MAX_VALUE;
      AABB box = this.robot.getBoundingBox().inflate(this.maxRange);

      for (Entity e : this.robot.level().getEntitiesOfClass(Entity.class, box, e -> e.isAlive() && this.filter.matches(e))) {
         if (this.robot.isKnownUnreachable(e)) {
            continue;
         }

         if (this.zone != null && !this.zone.contains(e.position())) {
            continue;
         }

         double distance = this.robot.position().distanceToSqr(e.position());
         if (distance < this.maxRange * this.maxRange && distance < best) {
            best = distance;
            this.target = e;
         }
      }

      this.terminate();
   }

   @Override
   public boolean success() {
      return this.target != null;
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 5L;
   }
}
