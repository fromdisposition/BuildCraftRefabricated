/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode;

public final class NodeConstantLong implements IExpressionNode.INodeLong, IConstantNode {
   public static final NodeConstantLong ZERO = new NodeConstantLong(0L);
   public final long value;

   public NodeConstantLong(long value) {
      this.value = value;
   }

   public static NodeConstantLong of(long value) {
      return new NodeConstantLong(value);
   }

   @Override
   public long evaluate() {
      return this.value;
   }

   @Override
   public IExpressionNode.INodeLong inline() {
      return this;
   }

   @Override
   public String toString() {
      return this.value + "L";
   }

   @Override
   public int hashCode() {
      return Long.hashCode(this.value);
   }

   @Override
      @SuppressWarnings("unchecked")
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         NodeConstantLong other = (NodeConstantLong)obj;
         return this.value == other.value;
      } else {
         return false;
      }
   }
}
