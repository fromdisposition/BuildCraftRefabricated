/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.TriggerWrapper;
import java.util.List;

public final class GateTriggerGroupEvaluator {
   private GateTriggerGroupEvaluator() {
   }

   public static void evaluateGroups(
      IGate gate,
      IPipeHolder pipeHolder,
      GateVariant variant,
      GateLogic.StatementPair[] statements,
      boolean[] connections,
      boolean[] prevActions,
      boolean[] triggerOn,
      boolean[] actionOn,
      List<StatementSlot> activeActions,
      GateTriggerGroupEvaluator.StateHolder state
   ) {
      int groupCount = 0;
      int groupActive = 0;
      state.isOn = false;

      for (int triggerIndex = 0; triggerIndex < statements.length; triggerIndex++) {
         GateLogic.StatementPair pair = statements[triggerIndex];
         TriggerWrapper trigger = pair.trigger.get();
         groupCount++;
         if (trigger == null) {
            groupActive++;
         } else {
            IStatementParameter[] params = new IStatementParameter[pair.trigger.getParamCount()];

            for (int p = 0; p < pair.trigger.getParamCount(); p++) {
               params[p] = pair.trigger.getParamRef(p).get();
            }

            if (trigger.isTriggerActive(gate, params)) {
               groupActive++;
               triggerOn[triggerIndex] = true;
            }
         }

         if (connections.length == triggerIndex || !connections[triggerIndex]) {
            boolean allActionsActive;
            if (variant.logic == EnumGateLogic.AND) {
               allActionsActive = groupActive == groupCount;
            } else {
               allActionsActive = groupActive > 0;
            }

            for (int i = groupCount - 1; i >= 0; i--) {
               int actionIndex = triggerIndex - i;
               GateLogic.StatementPair fullAction = statements[actionIndex];
               ActionWrapper action = fullAction.action.get();
               actionOn[actionIndex] = allActionsActive;
               if (action != null) {
                  if (allActionsActive) {
                     state.isOn = true;
                     StatementSlot slot = new StatementSlot();
                     slot.statement = action.delegate;
                     slot.parameters = (IStatementParameter[])fullAction.action.getParameters().clone();
                     slot.part = action.sourcePart;
                     activeActions.add(slot);
                     if (!prevActions[actionIndex]) {
                        action.actionActivate(gate, slot.parameters);
                        PipeEvent evt = new PipeEventActionActivate(pipeHolder, action.getDelegate(), slot.parameters, action.sourcePart);
                        pipeHolder.fireEvent(evt);
                     }
                  } else if (prevActions[actionIndex]) {
                     action.actionDeactivated(gate, fullAction.action.getParameters());
                  }
               }
            }

            groupActive = 0;
            groupCount = 0;
         }
      }
   }

   public static final class StateHolder {
      public boolean isOn;
   }
}
