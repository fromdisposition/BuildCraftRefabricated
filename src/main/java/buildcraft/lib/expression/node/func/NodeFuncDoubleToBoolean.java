/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import java.util.Objects;

public class NodeFuncDoubleToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncDoubleToBoolean.IFuncDoubleToBoolean function;
   private final StringFunctionBi stringFunction;

   public NodeFuncDoubleToBoolean(String name, NodeFuncDoubleToBoolean.IFuncDoubleToBoolean function) {
      this(function, a -> "[ double -> boolean ] " + name + "(" + a + ")");
   }

   public NodeFuncDoubleToBoolean(NodeFuncDoubleToBoolean.IFuncDoubleToBoolean function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncDoubleToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a);
   }

   public NodeFuncDoubleToBoolean.FuncDoubleToBoolean create(IExpressionNode.INodeDouble argA) {
      return new FuncDoubleToBoolean(argA);
   }

   public class FuncDoubleToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;

      public FuncDoubleToBoolean(IExpressionNode.INodeDouble argA) {
         this.argA = argA;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncDoubleToBoolean.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncDoubleToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncDoubleToBoolean.this.new FuncDoubleToBoolean(a), a -> NodeFuncDoubleToBoolean.this.new FuncDoubleToBoolean(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncDoubleToBoolean.this.new FuncDoubleToBoolean(a),
               a -> NodeConstantBoolean.of(NodeFuncDoubleToBoolean.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleToBoolean.this.canInline) {
            if (NodeFuncDoubleToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleToBoolean.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleToBoolean.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncDoubleToBoolean.FuncDoubleToBoolean other = (NodeFuncDoubleToBoolean.FuncDoubleToBoolean)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleToBoolean {
      boolean apply(double var1);
   }
}
