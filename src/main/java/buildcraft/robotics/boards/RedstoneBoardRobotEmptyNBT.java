package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.LocaleUtil;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class RedstoneBoardRobotEmptyNBT extends RedstoneBoardRobotNBT {
   public static final RedstoneBoardRobotEmptyNBT INSTANCE = new RedstoneBoardRobotEmptyNBT();

   private RedstoneBoardRobotEmptyNBT() {
   }

   @Override
   public RedstoneBoardRobot create(EntityRobotBase robot) {
      return new BoardRobotEmpty(robot);
   }

   @Override
   public Identifier getRobotTexture() {
      return Identifier.fromNamespaceAndPath("buildcraftrobotics", "entities/robot_base");
   }

   @Override
   public String getID() {
      return "buildcraft:boardRobotEmpty";
   }

   @Override
   public void addInformation(ItemStack stack, Player player, List<String> tooltip, boolean advanced) {
   }

   @Override
   public String getItemModelLocation() {
      return "buildcraftrobotics:board/clean";
   }

   @Override
   public String getDisplayName() {
      return LocaleUtil.localize("buildcraft.boardRobotClean");
   }
}
