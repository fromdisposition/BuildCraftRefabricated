package buildcraft.api.statements;

import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ITriggerProvider {
   void addInternalTriggers(Collection<ITriggerInternal> var1, IStatementContainer var2);

   void addInternalSidedTriggers(Collection<ITriggerInternalSided> var1, IStatementContainer var2, @Nonnull Direction var3);

   void addExternalTriggers(Collection<ITriggerExternal> var1, @Nonnull Direction var2, BlockEntity var3);
}
