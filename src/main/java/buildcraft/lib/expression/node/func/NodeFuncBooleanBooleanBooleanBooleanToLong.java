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
import buildcraft.lib.expression.node.value.NodeConstantLong;
import java.util.Objects;

public class NodeFuncBooleanBooleanBooleanBooleanToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncBooleanBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanBooleanToLong function;
   private final StringFunctionPenta stringFunction;

   public NodeFuncBooleanBooleanBooleanBooleanToLong(String name, NodeFuncBooleanBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanBooleanToLong function) {
      this(function, (a, b, c, d) -> "[ boolean, boolean, boolean, boolean -> long ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")");
   }

   public NodeFuncBooleanBooleanBooleanBooleanToLong(
      NodeFuncBooleanBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanBooleanToLong function, StringFunctionPenta stringFunction
   ) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncBooleanBooleanBooleanBooleanToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean d = stack.popBoolean();
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b, c, d);
   }

   public NodeFuncBooleanBooleanBooleanBooleanToLong.FuncBooleanBooleanBooleanBooleanToLong create(
      IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC, IExpressionNode.INodeBoolean argD
   ) {
      return new FuncBooleanBooleanBooleanBooleanToLong(argA, argB, argC, argD);
   }

   public class FuncBooleanBooleanBooleanBooleanToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;
      public final IExpressionNode.INodeBoolean argD;

      public FuncBooleanBooleanBooleanBooleanToLong(
         IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC, IExpressionNode.INodeBoolean argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public long evaluate() {
         return NodeFuncBooleanBooleanBooleanBooleanToLong.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncBooleanBooleanBooleanBooleanToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToLong.this.new FuncBooleanBooleanBooleanBooleanToLong(a, b, c, d),
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToLong.this.new FuncBooleanBooleanBooleanBooleanToLong(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToLong.this.new FuncBooleanBooleanBooleanBooleanToLong(a, b, c, d),
               (a, b, c, d) -> NodeConstantLong.of(
                  NodeFuncBooleanBooleanBooleanBooleanToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanBooleanBooleanToLong.this.canInline) {
            if (NodeFuncBooleanBooleanBooleanBooleanToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanBooleanBooleanToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanBooleanBooleanToLong.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanBooleanBooleanToLong.this;
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
            NodeFuncBooleanBooleanBooleanBooleanToLong.FuncBooleanBooleanBooleanBooleanToLong other = (NodeFuncBooleanBooleanBooleanBooleanToLong.FuncBooleanBooleanBooleanBooleanToLong)obj;
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
   public interface IFuncBooleanBooleanBooleanBooleanToLong {
      long apply(boolean var1, boolean var2, boolean var3, boolean var4);
   }
}
