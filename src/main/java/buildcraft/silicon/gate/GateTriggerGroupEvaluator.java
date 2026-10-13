/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.TriggerWrapper;
import java.util.ArrayList;
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
      List<PendingAction> pending = new ArrayList<>();

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
               if (action == null) {
                  actionOn[actionIndex] = allActionsActive;
               } else if (allActionsActive) {
                  pending.add(new PendingAction(actionIndex, action, fullAction.action.getParameters().clone()));
               } else {
                  actionOn[actionIndex] = false;
                  if (prevActions[actionIndex]) {
                     action.actionDeactivated(gate, fullAction.action.getParameters());
                  }
               }
            }

            groupActive = 0;
            groupCount = 0;
         }
      }

      for (int i = 0; i < pending.size(); i++) {
         PendingAction current = pending.get(i);
         boolean overridden = false;

         for (int j = i + 1; j < pending.size(); j++) {
            if (targetsSameSetting(current.action, pending.get(j).action)) {
               overridden = true;
               break;
            }
         }

         if (overridden) {
            actionOn[current.actionIndex] = false;
            if (prevActions[current.actionIndex]) {
               current.action.actionDeactivated(gate, current.parameters);
            }

            continue;
         }

         actionOn[current.actionIndex] = true;
         state.isOn = true;
         StatementSlot slot = new StatementSlot();
         slot.statement = current.action.delegate;
         slot.parameters = current.parameters;
         slot.part = current.action.sourcePart;
         activeActions.add(slot);
         current.action.actionActivate(gate, slot.parameters);
         PipeEvent evt = new PipeEventActionActivate(pipeHolder, current.action.getDelegate(), slot.parameters, current.action.sourcePart);
         pipeHolder.fireEvent(evt);
      }
   }

   private static boolean targetsSameSetting(ActionWrapper a, ActionWrapper b) {
      if (a.sourcePart != b.sourcePart) {
         return false;
      }

      IStatement mine = a.getDelegate();
      IStatement theirs = b.getDelegate();
      if (mine == theirs || mine.getUniqueTag().equals(theirs.getUniqueTag())) {
         return true;
      }

      return containsVariant(mine.getPossible(), theirs) && containsVariant(theirs.getPossible(), mine);
   }

   private static boolean containsVariant(IStatement[] variants, IStatement candidate) {
      if (variants == null) {
         return false;
      }

      String tag = candidate.getUniqueTag();

      for (IStatement variant : variants) {
         if (variant != null && (variant == candidate || variant.getUniqueTag().equals(tag))) {
            return true;
         }
      }

      return false;
   }

   private record PendingAction(int actionIndex, ActionWrapper action, IStatementParameter[] parameters) {
   }

   public static final class StateHolder {
      public boolean isOn;
   }
}
