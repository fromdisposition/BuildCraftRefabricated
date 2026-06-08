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

public class NodeConditionalBoolean implements IExpressionNode.INodeBoolean, IDependantNode {
   private final IExpressionNode.INodeBoolean condition;
   private final IExpressionNode.INodeBoolean ifTrue;
   private final IExpressionNode.INodeBoolean ifFalse;

   public NodeConditionalBoolean(IExpressionNode.INodeBoolean condition, IExpressionNode.INodeBoolean ifTrue, IExpressionNode.INodeBoolean ifFalse) {
      this.condition = condition;
      this.ifTrue = ifTrue;
      this.ifFalse = ifFalse;
   }

   @Override
   public boolean evaluate() {
      return this.condition.evaluate() ? this.ifTrue.evaluate() : this.ifFalse.evaluate();
   }

   @Override
   public IExpressionNode.INodeBoolean inline() {
      IExpressionNode.INodeBoolean c = this.condition.inline();
      IExpressionNode.INodeBoolean t = this.ifTrue.inline();
      IExpressionNode.INodeBoolean f = this.ifFalse.inline();
      if (c instanceof NodeConstantBoolean) {
         return ((NodeConstantBoolean)c).value ? t : f;
      } else if (c != this.condition || t != this.ifTrue || f != this.ifFalse) {
         return new NodeConditionalBoolean(c, t, f);
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
