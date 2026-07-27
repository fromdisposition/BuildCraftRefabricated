/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.filter;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public class StatementParameterStackFilter extends ArrayStackFilter {
   public StatementParameterStackFilter(IStatementParameter... parameters) {
      super(collect(parameters));
   }

   private static ItemStack[] collect(IStatementParameter... parameters) {
      List<ItemStack> tmp = new ArrayList<>();
      if (parameters != null) {
         for (IStatementParameter param : parameters) {
            if (param instanceof StatementParameterItemStack stackParam) {
               tmp.add(stackParam.getItemStack());
            }
         }
      }

      return tmp.toArray(new ItemStack[0]);
   }
}
