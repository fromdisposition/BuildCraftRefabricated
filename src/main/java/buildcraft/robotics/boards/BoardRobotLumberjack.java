package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoardRobotLumberjack extends BoardRobotGenericBreakBlock {
   public BoardRobotLumberjack(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("lumberjack");
   }

   @Override
   public boolean isExpectedTool(ItemStack stack) {
      return !stack.isEmpty() && stack.is(ItemTags.AXES);
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      return BuildCraftAPI.getWorldProperty("wood").get(world, pos);
   }
}
