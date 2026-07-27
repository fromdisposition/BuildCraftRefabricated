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

public class NodeFuncBooleanBooleanBooleanToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanToDouble function;
   private final StringFunctionQuad stringFunction;

   public NodeFuncBooleanBooleanBooleanToDouble(String name, NodeFuncBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanToDouble function) {
      this(function, (a, b, c) -> "[ boolean, boolean, boolean -> double ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncBooleanBooleanBooleanToDouble(
      NodeFuncBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanToDouble function, StringFunctionQuad stringFunction
   ) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncBooleanBooleanBooleanToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b, c);
   }

   public NodeFuncBooleanBooleanBooleanToDouble.FuncBooleanBooleanBooleanToDouble create(
      IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC
   ) {
      return new FuncBooleanBooleanBooleanToDouble(argA, argB, argC);
   }

   public class FuncBooleanBooleanBooleanToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;

      public FuncBooleanBooleanBooleanToDouble(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public double evaluate() {
         return NodeFuncBooleanBooleanBooleanToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncBooleanBooleanBooleanToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToDouble.this.new FuncBooleanBooleanBooleanToDouble(a, b, c),
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToDouble.this.new FuncBooleanBooleanBooleanToDouble(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToDouble.this.new FuncBooleanBooleanBooleanToDouble(a, b, c),
               (a, b, c) -> NodeConstantDouble.of(NodeFuncBooleanBooleanBooleanToDouble.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanBooleanToDouble.this.canInline) {
            if (NodeFuncBooleanBooleanBooleanToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanBooleanToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanBooleanToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanBooleanToDouble.this;
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
            NodeFuncBooleanBooleanBooleanToDouble.FuncBooleanBooleanBooleanToDouble other = (NodeFuncBooleanBooleanBooleanToDouble.FuncBooleanBooleanBooleanToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanBooleanToDouble {
      double apply(boolean var1, boolean var2, boolean var3);
   }
}
