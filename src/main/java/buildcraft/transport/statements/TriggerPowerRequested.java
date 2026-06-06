package buildcraft.transport.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.IFlowPowerLike;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import javax.annotation.Nullable;

public class TriggerPowerRequested extends BCStatement implements ITriggerInternal {
   public TriggerPowerRequested() {
      super("buildcraft:powerRequested");
   }

   @Override
   public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
      return !(source instanceof IGate)
         ? false
         : ((IGate)source).getPipeHolder().getPipe().getFlow() instanceof IFlowPowerLike powerLike && powerLike.getPowerRequested() > 0L;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.pipe.requestsEnergy");
   }

   @Nullable
   @Override
   public ISprite getSprite() {
      return BCTransportSprites.TRIGGER_POWER_REQUESTED;
   }
}
