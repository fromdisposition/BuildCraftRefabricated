/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import java.util.Objects;

public final class NodeConstantObject<T> implements IExpressionNode.INodeObject<T>, IConstantNode {
   public static final NodeConstantObject<String> EMPTY_STRING = new NodeConstantObject<>(String.class, "");
   public final Class<T> type;
   public final T value;

   public NodeConstantObject(Class<T> type, T value) {
      this.type = type;
      this.value = value;
   }

   @Override
   public Class<T> getType() {
      return this.type;
   }

   @Override
   public T evaluate() {
      return this.value;
   }

   @Override
   public IExpressionNode.INodeObject<T> inline() {
      return this;
   }

   @Override
   public String toString() {
      return this.value instanceof String ? "'" + this.value + "'" : this.value.toString();
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.value);
   }

   @Override
      @SuppressWarnings("unchecked")
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         NodeConstantObject<?> other = (NodeConstantObject<?>)obj;
         return this.getType() == other.getType() && Objects.equals(this.value, other.value);
      } else {
         return false;
      }
   }
}
