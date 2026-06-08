/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeConditionalDouble implements IExpressionNode.INodeDouble, IDependantNode {
   private final IExpressionNode.INodeBoolean condition;
   private final IExpressionNode.INodeDouble ifTrue;
   private final IExpressionNode.INodeDouble ifFalse;

   public NodeConditionalDouble(IExpressionNode.INodeBoolean condition, IExpressionNode.INodeDouble ifTrue, IExpressionNode.INodeDouble ifFalse) {
      this.condition = condition;
      this.ifTrue = ifTrue;
      this.ifFalse = ifFalse;
   }

   @Override
   public double evaluate() {
      return this.condition.evaluate() ? this.ifTrue.evaluate() : this.ifFalse.evaluate();
   }

   @Override
   public IExpressionNode.INodeDouble inline() {
      IExpressionNode.INodeBoolean c = this.condition.inline();
      IExpressionNode.INodeDouble t = this.ifTrue.inline();
      IExpressionNode.INodeDouble f = this.ifFalse.inline();
      if (c instanceof NodeConstantBoolean && t instanceof NodeConstantDouble && f instanceof NodeConstantDouble) {
         return new NodeConstantDouble(((NodeConstantBoolean)c).value ? ((NodeConstantDouble)t).value : ((NodeConstantDouble)f).value);
      } else if (c != this.condition || t != this.ifTrue || f != this.ifFalse) {
         return new NodeConditionalDouble(c, t, f);
      } else if (c instanceof NodeConstantBoolean) {
         return ((NodeConstantBoolean)c).value ? t : f;
      } else {
         return this;
      }
   }

   @Override
   public void visitDependants(IDependancyVisitor visitor) {
      visitor.dependOn(this.condition, this.ifTrue, this.ifFalse);
   }

   @Override
   public String toString() {
      return "(" + this.condition + ") ? (" + this.ifTrue + ") : (" + this.ifFalse + ")";
   }
}
