package buildcraft.api.statements.containers;

import buildcraft.api.statements.IStatementContainer;
import net.minecraft.core.Direction;

public interface ISidedStatementContainer extends IStatementContainer {
   Direction getSide();
}
