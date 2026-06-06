package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeFlowStructure extends PipeFlow {
   public PipeFlowStructure(IPipe pipe) {
      super(pipe);
   }

   public PipeFlowStructure(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @Override
   public boolean canConnect(Direction face, PipeFlow other) {
      return other instanceof PipeFlowStructure;
   }

   @Override
   public boolean canConnect(Direction face, BlockEntity oTile) {
      return false;
   }
}
