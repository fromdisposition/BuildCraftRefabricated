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
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.api.NodeTypes;

public class NodeVariableObject<T> extends NodeVariable implements IVariableNode.IVariableNodeObject<T>, IDependantNode {
   public final Class<T> type;
   public T value;
   private IExpressionNode.INodeObject<T> src;

   public NodeVariableObject(String name, Class<T> type) {
      super(name);
      this.type = type;
      NodeType<T> nodeType = NodeTypes.getType(type);
      if (nodeType == null) {
         throw new IllegalArgumentException("Unknown NodeType " + type);
      }

      this.value = nodeType.defaultValue;
   }

   @Override
   public Class<T> getType() {
      return this.type;
   }

   @Override
   public T evaluate() {
      return this.src != null ? this.src.evaluate() : this.value;
   }

   @Override
   public IExpressionNode.INodeObject<T> inline() {
      if (this.isConst) {
         return new NodeConstantObject<>(this.getType(), this.value);
      } else {
         return this.src != null ? this.src.inline() : this;
      }
   }

   @Override
   public void set(T value) {
      this.value = value;
   }

   @Override
   public void setConstantSource(IExpressionNode source) {
      if (this.src != null) {
         throw new IllegalStateException("Already have a constant source");
      }

      IExpressionNode.INodeObject<?> obj = (IExpressionNode.INodeObject<?>)source;
      if (obj.getType() != this.getType()) {
         throw new IllegalArgumentException("Cannot convert " + obj.getType() + " to " + this.getType());
      }

      @SuppressWarnings("unchecked")
      IExpressionNode.INodeObject<T> typedSource = (IExpressionNode.INodeObject<T>)source;
      this.src = typedSource;
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
