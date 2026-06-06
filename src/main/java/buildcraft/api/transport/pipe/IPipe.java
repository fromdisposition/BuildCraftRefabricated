package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPipe {
   IPipeHolder getHolder();

   PipeDefinition getDefinition();

   PipeBehaviour getBehaviour();

   PipeFlow getFlow();

   DyeColor getColour();

   void setColour(DyeColor var1);

   void markForUpdate();

   BlockEntity getConnectedTile(Direction var1);

   IPipe getConnectedPipe(Direction var1);

   boolean isConnected(Direction var1);

   IPipe.ConnectedType getConnectedType(Direction var1);

   enum ConnectedType {
      TILE,
      PIPE;
   }
}
