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

public class NodeFuncBooleanBooleanBooleanBooleanToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncBooleanBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanBooleanToBoolean function;
   private final StringFunctionPenta stringFunction;

   public NodeFuncBooleanBooleanBooleanBooleanToBoolean(
      String name, NodeFuncBooleanBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanBooleanToBoolean function
   ) {
      this(function, (a, b, c, d) -> "[ boolean, boolean, boolean, boolean -> boolean ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")");
   }

   public NodeFuncBooleanBooleanBooleanBooleanToBoolean(
      NodeFuncBooleanBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanBooleanToBoolean function, StringFunctionPenta stringFunction
   ) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncBooleanBooleanBooleanBooleanToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean d = stack.popBoolean();
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b, c, d);
   }

   public NodeFuncBooleanBooleanBooleanBooleanToBoolean.FuncBooleanBooleanBooleanBooleanToBoolean create(
      IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC, IExpressionNode.INodeBoolean argD
   ) {
      return new FuncBooleanBooleanBooleanBooleanToBoolean(argA, argB, argC, argD);
   }

   public class FuncBooleanBooleanBooleanBooleanToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;
      public final IExpressionNode.INodeBoolean argD;

      public FuncBooleanBooleanBooleanBooleanToBoolean(
         IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC, IExpressionNode.INodeBoolean argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.new FuncBooleanBooleanBooleanBooleanToBoolean(a, b, c, d),
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.new FuncBooleanBooleanBooleanBooleanToBoolean(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.new FuncBooleanBooleanBooleanBooleanToBoolean(a, b, c, d),
               (a, b, c, d) -> NodeConstantBoolean.of(
                  NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.canInline) {
            if (NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanBooleanBooleanToBoolean.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanBooleanBooleanToBoolean.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncBooleanBooleanBooleanBooleanToBoolean.FuncBooleanBooleanBooleanBooleanToBoolean other = (NodeFuncBooleanBooleanBooleanBooleanToBoolean.FuncBooleanBooleanBooleanBooleanToBoolean)obj;
            return Objects.equals(this.argA, other.argA)
               && Objects.equals(this.argB, other.argB)
               && Objects.equals(this.argC, other.argC)
               && Objects.equals(this.argD, other.argD);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanBooleanBooleanToBoolean {
      boolean apply(boolean var1, boolean var2, boolean var3, boolean var4);
   }
}
