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

public class NodeFuncBooleanBooleanBooleanToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanToBoolean function;
   private final StringFunctionQuad stringFunction;

   public NodeFuncBooleanBooleanBooleanToBoolean(String name, NodeFuncBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanToBoolean function) {
      this(function, (a, b, c) -> "[ boolean, boolean, boolean -> boolean ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncBooleanBooleanBooleanToBoolean(
      NodeFuncBooleanBooleanBooleanToBoolean.IFuncBooleanBooleanBooleanToBoolean function, StringFunctionQuad stringFunction
   ) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncBooleanBooleanBooleanToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b, c);
   }

   public NodeFuncBooleanBooleanBooleanToBoolean.FuncBooleanBooleanBooleanToBoolean create(
      IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC
   ) {
      return new FuncBooleanBooleanBooleanToBoolean(argA, argB, argC);
   }

   public class FuncBooleanBooleanBooleanToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;

      public FuncBooleanBooleanBooleanToBoolean(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncBooleanBooleanBooleanToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncBooleanBooleanBooleanToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToBoolean.this.new FuncBooleanBooleanBooleanToBoolean(a, b, c),
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToBoolean.this.new FuncBooleanBooleanBooleanToBoolean(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToBoolean.this.new FuncBooleanBooleanBooleanToBoolean(a, b, c),
               (a, b, c) -> NodeConstantBoolean.of(NodeFuncBooleanBooleanBooleanToBoolean.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanBooleanToBoolean.this.canInline) {
            if (NodeFuncBooleanBooleanBooleanToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanBooleanToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanBooleanToBoolean.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanBooleanToBoolean.this;
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
            NodeFuncBooleanBooleanBooleanToBoolean.FuncBooleanBooleanBooleanToBoolean other = (NodeFuncBooleanBooleanBooleanToBoolean.FuncBooleanBooleanBooleanToBoolean)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanBooleanToBoolean {
      boolean apply(boolean var1, boolean var2, boolean var3);
   }
}
