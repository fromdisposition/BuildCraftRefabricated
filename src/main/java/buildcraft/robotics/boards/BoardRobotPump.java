package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraft.robotics.ai.AIRobotPumpBlock;
import buildcraft.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraft.robotics.path.IBlockFilter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class BoardRobotPump extends RedstoneBoardRobot {
   private BlockPos blockFound;

   public BoardRobotPump(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("pump");
   }

   @Override
   public void update() {
      if (this.robotHasFluid()) {
         this.startDelegateAI(new AIRobotGotoStationAndUnloadFluids(this.robot));
      } else {
         final IWorldProperty isFluidSource = BuildCraftAPI.getWorldProperty("fluidSource");
         this.startDelegateAI(new AIRobotSearchAndGotoBlock(this.robot, false, new IBlockFilter() {
            @Override
            public boolean matches(Level world, BlockPos pos) {
               return isFluidSource.get(world, pos) && !BoardRobotPump.this.robot.getRegistry().isTaken(new ResourceIdBlock(pos));
            }
         }));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoBlock search) {
         if (ai.success()) {
            this.blockFound = search.getBlockFound();
            this.startDelegateAI(new AIRobotPumpBlock(this.robot, this.blockFound));
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotPumpBlock) {
         this.releaseBlockFound();
      } else if (ai instanceof AIRobotGotoStationAndUnloadFluids && !ai.success()) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }

   @Override
   public void end() {
      this.releaseBlockFound();
   }

   private void releaseBlockFound() {
      if (this.blockFound != null) {
         this.robot.getRegistry().release(new ResourceIdBlock(this.blockFound));
         this.blockFound = null;
      }
   }

   private boolean robotHasFluid() {
      for (StorageView<FluidVariant> view : this.robot.getFluidStorage()) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return true;
         }
      }

      return false;
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
