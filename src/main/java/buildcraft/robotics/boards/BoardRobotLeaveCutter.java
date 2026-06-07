package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;

public class BoardRobotLeaveCutter extends BoardRobotGenericBreakBlock {
   public BoardRobotLeaveCutter(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("leaveCutter");
   }

   @Override
   public boolean isExpectedTool(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof ShearsItem;
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      return BuildCraftAPI.getWorldProperty("leaves").get(world, pos);
   }
}
