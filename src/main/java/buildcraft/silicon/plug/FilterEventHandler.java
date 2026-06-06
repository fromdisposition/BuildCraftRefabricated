package buildcraft.silicon.plug;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pluggable.PipePluggable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class FilterEventHandler {
   @PipeEventHandler
   public static void sideCheck(PipeEventItem.SideCheck event) {
      for (Direction side : Direction.values()) {
         if (event.isAllowed(side)) {
            IPipe neighbour = event.holder.getNeighbourPipe(side);
            if (neighbour != null) {
               PipePluggable neighbourPlug = neighbour.getHolder().getPluggable(side.getOpposite());
               PipePluggable atPlug = event.holder.getPluggable(side);
               if (neighbourPlug instanceof PluggableLens) {
                  DyeColor colourAt = event.colour;
                  if (atPlug instanceof PluggableLens lens && !lens.isFilter) {
                     colourAt = lens.colour;
                  }

                  PluggableLens lens = (PluggableLens)neighbourPlug;
                  if (lens.isFilter) {
                     if (colourAt == lens.colour) {
                        event.increasePriority(side);
                     } else if (colourAt == null) {
                        event.decreasePriority(side);
                     }
                  }
               }
            }
         }
      }
   }
}
