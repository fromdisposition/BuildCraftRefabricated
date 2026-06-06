package buildcraft.builders;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public enum BCBuildersActionProvider implements IActionProvider {
   INSTANCE;

   @Override
   public void addInternalActions(Collection<IActionInternal> res, IStatementContainer container) {
   }

   @Override
   public void addInternalSidedActions(Collection<IActionInternalSided> actions, IStatementContainer container, @Nonnull Direction side) {
   }

   @Override
   public void addExternalActions(Collection<IActionExternal> res, @Nonnull Direction side, BlockEntity tile) {
      if (tile instanceof IFillerStatementContainer) {
         Collections.addAll(res, BCBuildersStatements.PATTERNS);
      }
   }
}
