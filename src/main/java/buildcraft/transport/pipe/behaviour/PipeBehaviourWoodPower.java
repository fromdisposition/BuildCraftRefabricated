package buildcraft.transport.pipe.behaviour;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraft.transport.pipe.flow.PipeNeighborEnergyAccess;
import buildcraft.transport.pipe.flow.PipeNeighborEnergyTransfers;
import buildcraft.transport.pipe.flow.PipeNeighborMjAccess;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.reborn.energy.api.EnergyStorage;

public class PipeBehaviourWoodPower extends PipeBehaviour {
   public PipeBehaviourWoodPower(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourWoodPower(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
   }

   @Override
   public boolean canConnect(Direction face, PipeBehaviour other) {
      return !(other instanceof PipeBehaviourWoodPower);
   }

   @Override
   public int getTextureIndex(Direction face) {
      if (face == null) {
         return 0;
      }

      if (this.pipe.getConnectedPipe(face) != null) {
         return 0;
      }

      BlockEntity tile = this.pipe.getConnectedTile(face);
      if (tile == null) {
         return 0;
      }

      if (this.pipe.getFlow() instanceof PipeFlowRedstoneFlux) {
         EnergyStorage storage = PipeNeighborEnergyAccess.storage(this.pipe.getHolder(), face);
         if (storage == null) {
            return 1;
         } else if (tile instanceof TileEngineRF) {
            return 0;
         } else if (tile instanceof TileDynamoMJ) {
            return 1;
         } else {
            return PipeNeighborEnergyTransfers.insert(storage, 1, true) > 0 ? 0 : 1;
         }
      } else {
         IMjReceiver recv = PipeNeighborMjAccess.receiver(this.pipe.getHolder(), face);
         return recv == null ? 1 : (recv.canReceive() ? 0 : 1);
      }
   }
}
