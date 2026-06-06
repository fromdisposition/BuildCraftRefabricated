package buildcraft.api.events;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.fabric.event.Event;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class RobotEvent extends Event {
   public final EntityRobotBase robot;

   public RobotEvent(EntityRobotBase robot) {
      this.robot = robot;
   }

   public static class Dismantle extends RobotEvent {
      public final Player player;

      public Dismantle(EntityRobotBase robot, Player player) {
         super(robot);
         this.player = player;
      }
   }

   public static class Interact extends RobotEvent {
      public final Player player;
      public final ItemStack item;

      public Interact(EntityRobotBase robot, Player player, ItemStack item) {
         super(robot);
         this.player = player;
         this.item = item;
      }
   }

   public static class Place extends RobotEvent {
      public final Player player;

      public Place(EntityRobotBase robot, Player player) {
         super(robot);
         this.player = player;
      }
   }
}
