package buildcraft.transport.statements;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.api.transport.pipe.IFlowPowerLike;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportStatements;
import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

public enum TriggerProviderPipes implements ITriggerProvider {
   INSTANCE;

   @Override
   public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container) {
      if (container instanceof IGate gate) {
         IPipeHolder holder = gate.getPipeHolder();
         holder.fireEvent(new PipeEventStatement.AddTriggerInternal(holder, triggers));

         for (DyeColor colour : ColourUtil.COLOURS) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, colour)) {
               triggers.add(BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 0]);
               triggers.add(BCTransportStatements.TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 1]);
            }
         }

         if (holder.getPipe().getFlow() instanceof IFlowPowerLike) {
            triggers.add(BCTransportStatements.TRIGGER_POWER_REQUESTED);
         }
      }
   }

   @Override
   public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, @Nonnull Direction side) {
      if (container instanceof IGate gate) {
         IPipeHolder holder = gate.getPipeHolder();
         holder.fireEvent(new PipeEventStatement.AddTriggerInternalSided(holder, triggers, side));
      }
   }

   @Override
   public void addExternalTriggers(Collection<ITriggerExternal> triggers, @Nonnull Direction side, BlockEntity tile) {
   }
}
