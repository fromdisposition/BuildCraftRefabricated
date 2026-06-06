package buildcraft.api.statements;

import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IActionProvider {
   void addInternalActions(Collection<IActionInternal> var1, IStatementContainer var2);

   void addInternalSidedActions(Collection<IActionInternalSided> var1, IStatementContainer var2, @Nonnull Direction var3);

   void addExternalActions(Collection<IActionExternal> var1, @Nonnull Direction var2, BlockEntity var3);
}
