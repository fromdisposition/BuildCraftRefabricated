package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourGold extends PipeBehaviour {
   private static final double SPEED_DELTA = 0.07;
   private static final double SPEED_TARGET = 0.25;

   public PipeBehaviourGold(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourGold(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @PipeEventHandler
   public static void modifySpeed(PipeEventItem.ModifySpeed event) {
      event.modifyTo(0.25, 0.07);
   }
}
