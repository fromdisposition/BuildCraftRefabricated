/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;

public class NodeVariableBoolean extends NodeVariable implements IVariableNode.IVariableNodeBoolean, IDependantNode {
   public boolean value;
   private IExpressionNode.INodeBoolean src = null;

   public NodeVariableBoolean(String name) {
      super(name);
   }

   @Override
   public boolean evaluate() {
      return this.src != null ? this.src.evaluate() : this.value;
   }

   @Override
   public IExpressionNode.INodeBoolean inline() {
      if (this.isConst) {
         return NodeConstantBoolean.of(this.value);
      } else {
         return this.src != null ? this.src.inline() : this;
      }
   }

   @Override
   public void set(boolean value) {
      this.value = value;
   }

   @Override
   public void setConstantSource(IExpressionNode source) {
      if (this.src != null) {
         throw new IllegalStateException("Already have a constant source");
      }

      this.src = (IExpressionNode.INodeBoolean)source;
   }

   @Override
   public void visitDependants(IDependancyVisitor visitor) {
      if (this.src != null) {
         visitor.dependOn(this.src);
      } else {
         visitor.dependOnExplictly(this);
      }
   }
}
