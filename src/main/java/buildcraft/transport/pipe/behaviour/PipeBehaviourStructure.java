package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import net.minecraft.nbt.CompoundTag;

public class PipeBehaviourStructure extends PipeBehaviour {
   public PipeBehaviourStructure(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   public PipeBehaviourStructure(IPipe pipe) {
      super(pipe);
   }
}
