package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.ai.AIRobotUseToolOnBlock;
import buildcraft.robotics.path.IBlockFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoardRobotFarmer extends RedstoneBoardRobot {
   private BlockPos blockFound;

   public BoardRobotFarmer(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("farmer");
   }

   @Override
   public void update() {
      IWorldProperty isDirt = BuildCraftAPI.getWorldProperty("dirt");
      if (this.robot.getHeldItem().isEmpty()) {
         this.startDelegateAI(new AIRobotFetchAndEquipItemStack(this.robot, (IStackFilter)stack -> !stack.isEmpty() && stack.is(ItemTags.HOES)));
      } else {
         this.startDelegateAI(new AIRobotSearchAndGotoBlock(this.robot, false, new IBlockFilter() {
            @Override
            public boolean matches(Level world, BlockPos pos) {
               return isDirt.get(world, pos)
                  && !BoardRobotFarmer.this.robot.getRegistry().isTaken(new ResourceIdBlock(pos))
                  && world.getBlockState(pos.above()).isAir();
            }
         }));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoBlock search) {
         if (ai.success()) {
            this.blockFound = search.getBlockFound();
            this.startDelegateAI(new AIRobotUseToolOnBlock(this.robot, this.blockFound));
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotFetchAndEquipItemStack) {
         if (!ai.success()) {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotUseToolOnBlock) {
         this.releaseBlockFound();
      }
   }

   private void releaseBlockFound() {
      if (this.blockFound != null) {
         this.robot.getRegistry().release(new ResourceIdBlock(this.blockFound));
         this.blockFound = null;
      }
   }

   @Override
   public void end() {
      this.releaseBlockFound();
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
}
