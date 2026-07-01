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

public class NodeFuncDoubleDoubleToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncDoubleDoubleToDouble.IFuncDoubleDoubleToDouble function;
   private final StringFunctionTri stringFunction;

   public NodeFuncDoubleDoubleToDouble(String name, NodeFuncDoubleDoubleToDouble.IFuncDoubleDoubleToDouble function) {
      this(function, (a, b) -> "[ double, double -> double ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncDoubleDoubleToDouble(NodeFuncDoubleDoubleToDouble.IFuncDoubleDoubleToDouble function, StringFunctionTri stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncDoubleDoubleToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b);
   }

   public NodeFuncDoubleDoubleToDouble.FuncDoubleDoubleToDouble create(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB) {
      return new FuncDoubleDoubleToDouble(argA, argB);
   }

   public class FuncDoubleDoubleToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;

      public FuncDoubleDoubleToDouble(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public double evaluate() {
         return NodeFuncDoubleDoubleToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncDoubleDoubleToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncDoubleDoubleToDouble.this.new FuncDoubleDoubleToDouble(a, b),
               (a, b) -> NodeFuncDoubleDoubleToDouble.this.new FuncDoubleDoubleToDouble(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncDoubleDoubleToDouble.this.new FuncDoubleDoubleToDouble(a, b),
               (a, b) -> NodeConstantDouble.of(NodeFuncDoubleDoubleToDouble.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleToDouble.this.canInline) {
            if (NodeFuncDoubleDoubleToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleToDouble.this;
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
            NodeFuncDoubleDoubleToDouble.FuncDoubleDoubleToDouble other = (NodeFuncDoubleDoubleToDouble.FuncDoubleDoubleToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleDoubleToDouble {
      double apply(double var1, double var3);
   }
}
