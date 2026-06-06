package buildcraft.api.statements;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ITriggerExternal extends ITrigger {
   boolean isTriggerActive(BlockEntity var1, Direction var2, IStatementContainer var3, IStatementParameter[] var4);
}
