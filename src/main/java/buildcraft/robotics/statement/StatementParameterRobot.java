package buildcraft.robotics.statement;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementSlot;
import buildcraft.lib.misc.StackUtil;
import buildcraft.robotics.item.ItemRobot;
import net.minecraft.world.item.ItemStack;

public class StatementParameterRobot extends buildcraft.api.statements.StatementParameterItemStack {
   @Override
   public String getUniqueTag() {
      return "buildcraft:robot";
   }

   public static boolean matches(IStatementParameter param, EntityRobotBase robot) {
      if (robot == null || param == null) {
         return false;
      }

      ItemStack stack = param.getItemStack();
      if (stack.isEmpty()) {
         return false;
      }

      if (stack.getItem() instanceof ItemRobot) {
         return ItemRobot.getRobotNBT(stack) == robot.getBoard().getNBTHandler();
      }

      return StackUtil.isMatchingItem(stack, ItemRobot.createRobotStack(robot.getBoard().getNBTHandler(), robot.getBattery().getStored()));
   }

   public static boolean matchesAny(StatementSlot slot, EntityRobotBase robot) {
      for (IStatementParameter param : slot.parameters) {
         if (param != null && matches(param, robot)) {
            return true;
         }
      }

      return false;
   }
}
