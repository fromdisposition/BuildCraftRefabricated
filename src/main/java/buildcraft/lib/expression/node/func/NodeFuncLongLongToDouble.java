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

public class NodeFuncLongLongToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncLongLongToDouble.IFuncLongLongToDouble function;
   private final StringFunctionTri stringFunction;

   public NodeFuncLongLongToDouble(String name, NodeFuncLongLongToDouble.IFuncLongLongToDouble function) {
      this(function, (a, b) -> "[ long, long -> double ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncLongLongToDouble(NodeFuncLongLongToDouble.IFuncLongLongToDouble function, StringFunctionTri stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncLongLongToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a, b);
   }

   public NodeFuncLongLongToDouble.FuncLongLongToDouble create(IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB) {
      return new FuncLongLongToDouble(argA, argB);
   }

   public class FuncLongLongToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;
      public final IExpressionNode.INodeLong argB;

      public FuncLongLongToDouble(IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public double evaluate() {
         return NodeFuncLongLongToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncLongLongToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncLongLongToDouble.this.new FuncLongLongToDouble(a, b),
               (a, b) -> NodeFuncLongLongToDouble.this.new FuncLongLongToDouble(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncLongLongToDouble.this.new FuncLongLongToDouble(a, b),
               (a, b) -> NodeConstantDouble.of(NodeFuncLongLongToDouble.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongLongToDouble.this.canInline) {
            if (NodeFuncLongLongToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongLongToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncLongLongToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongLongToDouble.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncLongLongToDouble.FuncLongLongToDouble other = (NodeFuncLongLongToDouble.FuncLongLongToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncLongLongToDouble {
      double apply(long var1, long var3);
   }
}
