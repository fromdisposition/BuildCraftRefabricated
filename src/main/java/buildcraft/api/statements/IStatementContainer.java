package buildcraft.api.statements;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IStatementContainer {
   BlockEntity getTile();

   @Nullable
   BlockEntity getNeighbourTile(Direction var1);
}
