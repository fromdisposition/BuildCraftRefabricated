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
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowFluids;

public class TriggerFluidsTraversing extends BCStatement implements ITriggerInternal {
   public TriggerFluidsTraversing() {
      super("buildcraft:pipe_contains_fluids");
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.pipe.containsFluids");
   }

   @Override
   public ISprite getSprite() {
      return BCTransportSprites.TRIGGER_FLUIDS_TRAVERSING;
   }

   @Override
   public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
      if (source instanceof IGate gate) {
         IPipe pipe = gate.getPipeHolder().getPipe();
         if (pipe != null && pipe.getFlow() instanceof PipeFlowFluids fluids) {
            return fluids.doesContainFluid();
         }
      }

      return false;
   }
}
