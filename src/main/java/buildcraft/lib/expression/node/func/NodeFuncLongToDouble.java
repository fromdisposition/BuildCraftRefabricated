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
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import java.util.Objects;

public class NodeFuncLongToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncLongToDouble.IFuncLongToDouble function;
   private final StringFunctionBi stringFunction;

   public NodeFuncLongToDouble(String name, NodeFuncLongToDouble.IFuncLongToDouble function) {
      this(function, a -> "[ long -> double ] " + name + "(" + a + ")");
   }

   public NodeFuncLongToDouble(NodeFuncLongToDouble.IFuncLongToDouble function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncLongToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a);
   }

   public NodeFuncLongToDouble.FuncLongToDouble create(IExpressionNode.INodeLong argA) {
      return new FuncLongToDouble(argA);
   }

   public class FuncLongToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;

      public FuncLongToDouble(IExpressionNode.INodeLong argA) {
         this.argA = argA;
      }

      @Override
      public double evaluate() {
         return NodeFuncLongToDouble.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncLongToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncLongToDouble.this.new FuncLongToDouble(a), a -> NodeFuncLongToDouble.this.new FuncLongToDouble(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncLongToDouble.this.new FuncLongToDouble(a),
               a -> NodeConstantDouble.of(NodeFuncLongToDouble.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongToDouble.this.canInline) {
            if (NodeFuncLongToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncLongToDouble.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongToDouble.this;
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
            NodeFuncLongToDouble.FuncLongToDouble other = (NodeFuncLongToDouble.FuncLongToDouble)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncLongToDouble {
      double apply(long var1);
   }
}
