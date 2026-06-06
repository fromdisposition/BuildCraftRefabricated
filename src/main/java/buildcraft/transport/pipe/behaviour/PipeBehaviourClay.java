package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourClay extends PipeBehaviour {
   public PipeBehaviourClay(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourClay(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @PipeEventHandler
   public void orderSides(PipeEventItem.SideCheck ordering) {
      for (Direction face : Direction.values()) {
         IPipe.ConnectedType type = this.pipe.getConnectedType(face);
         if (type == IPipe.ConnectedType.TILE) {
            ordering.increasePriority(face, 100);
         }
      }
   }

   @PipeEventHandler
   public void orderSides(PipeEventFluid.SideCheck ordering) {
      for (Direction face : Direction.values()) {
         IPipe.ConnectedType type = this.pipe.getConnectedType(face);
         if (type == IPipe.ConnectedType.TILE) {
            ordering.increasePriority(face, 100);
         }
      }
   }
}
