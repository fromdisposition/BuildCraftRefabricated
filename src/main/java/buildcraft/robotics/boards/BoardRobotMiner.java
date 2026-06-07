package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoardRobotMiner extends BoardRobotGenericBreakBlock {
   private static final int MAX_HARVEST_LEVEL = 3;
   private int harvestLevel;

   public BoardRobotMiner(EntityRobotBase robot) {
      super(robot);
      this.detectHarvestLevel();
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      super.delegateAIEnded(ai);
      if (ai instanceof AIRobotFetchAndEquipItemStack && ai.success()) {
         this.detectHarvestLevel();
      }
   }

   private void detectHarvestLevel() {
      ItemStack stack = this.robot.getHeldItem();
      if (!stack.isEmpty() && stack.is(ItemTags.PICKAXES)) {
         this.harvestLevel = tierOf(stack);
      }
   }

   private static int tierOf(ItemStack stack) {
      String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
      if (path.contains("netherite") || path.contains("diamond")) {
         return 3;
      } else if (path.contains("iron")) {
         return 2;
      } else {
         return 1;
      }
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("miner");
   }

   @Override
   public boolean isExpectedTool(ItemStack stack) {
      return !stack.isEmpty() && stack.is(ItemTags.PICKAXES);
   }

   @Override
   public boolean isExpectedBlock(Level world, BlockPos pos) {
      return BuildCraftAPI.getWorldProperty("ore@hardness=" + Math.min(MAX_HARVEST_LEVEL, this.harvestLevel)).get(world, pos);
   }
}
