/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode;

public final class NodeConstantDouble implements IExpressionNode.INodeDouble, IConstantNode {
   public static final NodeConstantDouble ZERO = new NodeConstantDouble(0.0);
   public static final NodeConstantDouble ONE = new NodeConstantDouble(1.0);
   public final double value;

   public NodeConstantDouble(double value) {
      this.value = value;
   }

   public static NodeConstantDouble of(double value) {
      return new NodeConstantDouble(value);
   }

   @Override
   public double evaluate() {
      return this.value;
   }

   @Override
   public IExpressionNode.INodeDouble inline() {
      return this;
   }

   @Override
   public String toString() {
      return Double.toString(this.value) + "D";
   }

   @Override
   public int hashCode() {
      return Double.hashCode(this.value);
   }

   @Override
      @SuppressWarnings("unchecked")
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return this.getClass() != obj.getClass() ? false : this.value == ((NodeConstantDouble)obj).value;
      }
   }
}
