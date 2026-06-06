package buildcraft.api.events;

import buildcraft.fabric.event.Event;
import net.minecraft.world.entity.player.Player;

public class RobotPlacementEvent extends Event {
   public Player player;
   public String robotProgram;

   public RobotPlacementEvent(Player player, String robotProgram) {
      this.player = player;
      this.robotProgram = robotProgram;
   }
}
