package buildcraft.api.events;

import buildcraft.fabric.event.Event;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** @deprecated Not fired by Refabricated yet; reserved for a future event bus. */
@Deprecated(forRemoval = true)
public class BlockInteractionEvent extends Event {
   public final Player player;
   public final BlockState state;

   public BlockInteractionEvent(Player player, BlockState state) {
      this.player = player;
      this.state = state;
   }
}
