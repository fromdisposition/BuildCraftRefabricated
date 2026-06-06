package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeBehaviourSandstone extends PipeBehaviour {
   private static final double SPEED_DELTA = 0.008;
   private static final double SPEED_TARGET = 0.01;

   public PipeBehaviourSandstone(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourSandstone(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @Override
   public boolean canConnect(Direction face, PipeBehaviour other) {
      return true;
   }

   @Override
   public boolean canConnect(Direction face, BlockEntity oTile) {
      return false;
   }

   @PipeEventHandler
   public static void modifySpeed(PipeEventItem.ModifySpeed event) {
      event.modifyTo(0.01, 0.008);
   }
}
