package buildcraft.api.statements.containers;

import net.minecraft.core.Direction;

public interface IRedstoneStatementContainer {

    int getRedstoneInput(Direction side);

    boolean setRedstoneOutput(Direction side, int value);
}
