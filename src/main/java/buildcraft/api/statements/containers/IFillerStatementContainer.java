package buildcraft.api.statements.containers;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

public interface IFillerStatementContainer extends IStatementContainer {

    @Override
    @Nullable
    BlockEntity getTile();

    Level getFillerWorld();

    boolean hasBox();

    IBox getBox() throws IllegalStateException;

    void setPattern(IFillerPattern pattern, IStatementParameter[] params);
}
