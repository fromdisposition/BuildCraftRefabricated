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
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import net.minecraft.world.item.ItemStack;

public class TriggerItemsTraversing extends BCStatement implements ITriggerInternal {
   public TriggerItemsTraversing() {
      super("buildcraft:pipe_contains_items");
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.pipe.containsItems");
   }

   @Override
   public ISprite getSprite() {
      return BCTransportSprites.TRIGGER_ITEMS_TRAVERSING;
   }

   @Override
   public int maxParameters() {
      return 1;
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return StatementParameterItemStack.EMPTY;
   }

   @Override
   public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
      if (source instanceof IGate gate) {
         IPipe pipe = gate.getPipeHolder().getPipe();
         if (pipe != null && pipe.getFlow() instanceof PipeFlowItems itemFlow) {
            ItemStack filter = getParam(0, parameters, StatementParameterItemStack.EMPTY).getItemStack();
            return itemFlow.containsItemMatching(filter);
         }
      }

      return false;
   }
}
