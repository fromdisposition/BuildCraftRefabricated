package buildcraft.api.statements.containers;

import net.minecraft.core.Direction;

import buildcraft.api.statements.IStatementContainer;

public interface ISidedStatementContainer extends IStatementContainer {
    Direction getSide();
}
