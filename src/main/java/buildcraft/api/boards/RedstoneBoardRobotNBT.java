package buildcraft.api.boards;

import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.nbt.CompoundTag;

public abstract class RedstoneBoardRobotNBT extends RedstoneBoardNBT<EntityRobotBase> {
   public RedstoneBoardRobot create(CompoundTag nbt, EntityRobotBase robot) {
      return this.create(robot);
   }

   public abstract RedstoneBoardRobot create(EntityRobotBase var1);

   public abstract Object getRobotTexture();
}
