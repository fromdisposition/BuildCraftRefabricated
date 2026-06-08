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
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;

public class TriggerPipeEmpty extends BCStatement implements ITriggerInternal {
   public TriggerPipeEmpty() {
      super("buildcraft:pipe_empty");
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.pipe.empty");
   }

   @Override
   public ISprite getSprite() {
      return BCTransportSprites.TRIGGER_PIPE_EMPTY;
   }

   @Override
   public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
      if (source instanceof IGate gate) {
         PipeFlow flow = gate.getPipeHolder().getPipe().getFlow();
         if (flow instanceof PipeFlowItems itemFlow) {
            return !itemFlow.doesContainItems();
         } else {
            return flow instanceof PipeFlowFluids fluidFlow ? !fluidFlow.doesContainFluid() : false;
         }
      } else {
         return false;
      }
   }
}
