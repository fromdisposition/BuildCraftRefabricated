package buildcraft.core.statements;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.BCCoreStatements;
import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public enum CoreActionProvider implements IActionProvider {
   INSTANCE;

   @Override
   public void addInternalActions(Collection<IActionInternal> res, IStatementContainer container) {
      if (container instanceof IRedstoneStatementContainer) {
         res.add(BCCoreStatements.ACTION_REDSTONE);
      }
   }

   @Override
   public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, @Nonnull Direction side) {
   }

   @Override
   public void addExternalActions(Collection<IActionExternal> res, @Nonnull Direction side, BlockEntity tile) {
      if (tile instanceof IControllable controllable) {
         for (ActionMachineControl action : BCCoreStatements.ACTION_MACHINE_CONTROL) {
            if (controllable.acceptsControlMode(action.mode)) {
               res.add(action);
            }
         }
      }
   }
}
