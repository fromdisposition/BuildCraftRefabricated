/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.pipe.IFlowPowerLike;
import buildcraft.api.transport.pipe.IPipe;
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
      if (source instanceof IGate gate) {
         IPipe pipe = gate.getPipeHolder().getPipe();
         return pipe != null && pipe.getFlow() instanceof IFlowPowerLike powerLike && powerLike.getPowerRequested() > 0L;
      }

      return false;
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
