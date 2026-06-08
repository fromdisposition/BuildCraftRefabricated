package buildcraft.api.events;

import buildcraft.fabric.event.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** @deprecated Not fired by Refabricated yet; reserved for a future event bus. */
@Deprecated(forRemoval = true)
public class PipePlacedEvent extends Event {
   public Player player;
   public Item pipeType;
   public BlockPos pos;

   public PipePlacedEvent(Player player, Item pipeType, BlockPos pos) {
      this.player = player;
      this.pipeType = pipeType;
      this.pos = pos;
   }
}
