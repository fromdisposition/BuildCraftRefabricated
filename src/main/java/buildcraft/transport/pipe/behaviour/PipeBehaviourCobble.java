package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourCobble extends PipeBehaviourSeparate {
   private static final double SPEED_DELTA = 0.02;
   private static final double SPEED_TARGET = 0.01;

   public PipeBehaviourCobble(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourCobble(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @PipeEventHandler
   public static void modifySpeed(PipeEventItem.ModifySpeed event) {
      event.modifyTo(0.01, 0.02);
   }
}
