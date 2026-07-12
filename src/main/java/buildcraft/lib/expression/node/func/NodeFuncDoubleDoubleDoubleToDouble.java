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

public class NodeFuncDoubleDoubleDoubleToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleToDouble function;
   private final StringFunctionQuad stringFunction;

   public NodeFuncDoubleDoubleDoubleToDouble(String name, NodeFuncDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleToDouble function) {
      this(function, (a, b, c) -> "[ double, double, double -> double ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncDoubleDoubleDoubleToDouble(NodeFuncDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleToDouble function, StringFunctionQuad stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncDoubleDoubleDoubleToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b, c);
   }

   public NodeFuncDoubleDoubleDoubleToDouble.FuncDoubleDoubleDoubleToDouble create(
      IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC
   ) {
      return new FuncDoubleDoubleDoubleToDouble(argA, argB, argC);
   }

   public class FuncDoubleDoubleDoubleToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;

      public FuncDoubleDoubleDoubleToDouble(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public double evaluate() {
         return NodeFuncDoubleDoubleDoubleToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncDoubleDoubleDoubleToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncDoubleDoubleDoubleToDouble.this.new FuncDoubleDoubleDoubleToDouble(a, b, c),
               (a, b, c) -> NodeFuncDoubleDoubleDoubleToDouble.this.new FuncDoubleDoubleDoubleToDouble(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncDoubleDoubleDoubleToDouble.this.new FuncDoubleDoubleDoubleToDouble(a, b, c),
               (a, b, c) -> NodeConstantDouble.of(NodeFuncDoubleDoubleDoubleToDouble.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleDoubleToDouble.this.canInline) {
            if (NodeFuncDoubleDoubleDoubleToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleDoubleToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleDoubleToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleDoubleToDouble.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncDoubleDoubleDoubleToDouble.FuncDoubleDoubleDoubleToDouble other = (NodeFuncDoubleDoubleDoubleToDouble.FuncDoubleDoubleDoubleToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleDoubleDoubleToDouble {
      double apply(double var1, double var3, double var5);
   }
}
