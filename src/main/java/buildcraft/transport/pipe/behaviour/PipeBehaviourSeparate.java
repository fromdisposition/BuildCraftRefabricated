package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public abstract class PipeBehaviourSeparate extends PipeBehaviour {
   public PipeBehaviourSeparate(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourSeparate(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @Override
   public boolean canConnect(Direction face, PipeBehaviour other) {
      return other instanceof PipeBehaviourSeparate ? other.getClass() == this.getClass() : true;
   }
}
