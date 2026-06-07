package buildcraft.robotics.ai;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.crops.CropManager;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/** Harvests a mature crop, dropping its produce into the world for collection. */
public class AIRobotHarvest extends AIRobot {
   private BlockPos blockFound;
   private int delay;

   public AIRobotHarvest(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotHarvest(EntityRobotBase robot, BlockPos blockFound) {
      super(robot);
      this.blockFound = blockFound;
   }

   @Override
   public void update() {
      if (this.blockFound == null) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      if (this.delay++ <= 20) {
         return;
      }

      if (!BuildCraftAPI.getWorldProperty("harvestable").get(this.robot.level(), this.blockFound)) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      NonNullList<ItemStack> drops = NonNullList.create();
      if (!CropManager.harvestCrop(this.robot.level(), this.blockFound, drops)) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      if (this.robot.level() instanceof ServerLevel serverLevel) {
         for (ItemStack stack : drops) {
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, this.robot.getX(), this.robot.getY(), this.robot.getZ(), stack));
         }
      }

      this.setSuccess(true);
      this.terminate();
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
      return MjAPI.MJ / 2L;
   }
}
