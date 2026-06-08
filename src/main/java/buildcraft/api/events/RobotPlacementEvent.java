package buildcraft.api.events;

import buildcraft.fabric.event.Event;
import net.minecraft.world.entity.player.Player;

/** @deprecated Not fired by Refabricated yet; reserved for a future event bus. */
@Deprecated(forRemoval = true)
public class RobotPlacementEvent extends Event {
   public Player player;
   public String robotProgram;

   public RobotPlacementEvent(Player player, String robotProgram) {
      this.player = player;
      this.robotProgram = robotProgram;
   }
}
