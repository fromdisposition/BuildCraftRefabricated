/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeCastLongToDouble implements IExpressionNode.INodeDouble, IDependantNode {
   private final IExpressionNode.INodeLong from;

   public NodeCastLongToDouble(IExpressionNode.INodeLong from) {
      this.from = from;
   }

   @Override
   public double evaluate() {
      return this.from.evaluate();
   }

   @Override
   public IExpressionNode.INodeDouble inline() {
      return NodeInliningHelper.tryInline(this, this.from, NodeCastLongToDouble::new, f -> new NodeConstantDouble(f.evaluate()));
   }

   @Override
   public void visitDependants(IDependancyVisitor visitor) {
      visitor.dependOn(this.from);
   }

   @Override
   public String toString() {
      return "_long_to_double( " + this.from + " )";
   }
}
