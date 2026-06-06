package buildcraft.api.statements.containers;

import net.minecraft.core.Direction;

public interface IRedstoneStatementContainer {
   int getRedstoneInput(Direction var1);

   boolean setRedstoneOutput(Direction var1, int var2);
}
