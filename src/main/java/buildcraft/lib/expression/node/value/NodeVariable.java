/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import java.util.Locale;

public abstract class NodeVariable implements IVariableNode {
   public final String name;
   protected boolean isConst = false;

   public NodeVariable(String name) {
      this.name = name.toLowerCase(Locale.ROOT);
   }

   @Override
   public void setConstant(boolean isConst) {
      this.isConst = isConst;
   }

   @Override
   public boolean isConstant() {
      return this.isConst;
   }

   public abstract void setConstantSource(IExpressionNode var1);

   @Override
   public String toString() {
      return "variable: " + this.name + " = " + this.evaluateAsString();
   }
}
