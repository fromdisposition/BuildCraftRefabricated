package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourVoid extends PipeBehaviour {
   public PipeBehaviourVoid(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourVoid(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @PipeEventHandler
   public static void reachCentre(PipeEventItem.ReachCenter reachCenter) {
      reachCenter.getStack().setCount(0);
   }

   @PipeEventHandler
   public static void moveFluidToCentre(PipeEventFluid.OnMoveToCentre move) {
      Arrays.fill(move.fluidEnteringCentre, 0);
   }
}
