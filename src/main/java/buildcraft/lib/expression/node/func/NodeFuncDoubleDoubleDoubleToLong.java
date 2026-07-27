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

public class NodeFuncDoubleDoubleDoubleToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleToLong function;
   private final StringFunctionQuad stringFunction;

   public NodeFuncDoubleDoubleDoubleToLong(String name, NodeFuncDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleToLong function) {
      this(function, (a, b, c) -> "[ double, double, double -> long ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncDoubleDoubleDoubleToLong(NodeFuncDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleToLong function, StringFunctionQuad stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncDoubleDoubleDoubleToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b, c);
   }

   public NodeFuncDoubleDoubleDoubleToLong.FuncDoubleDoubleDoubleToLong create(
      IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC
   ) {
      return new FuncDoubleDoubleDoubleToLong(argA, argB, argC);
   }

   public class FuncDoubleDoubleDoubleToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;

      public FuncDoubleDoubleDoubleToLong(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public long evaluate() {
         return NodeFuncDoubleDoubleDoubleToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncDoubleDoubleDoubleToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncDoubleDoubleDoubleToLong.this.new FuncDoubleDoubleDoubleToLong(a, b, c),
               (a, b, c) -> NodeFuncDoubleDoubleDoubleToLong.this.new FuncDoubleDoubleDoubleToLong(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncDoubleDoubleDoubleToLong.this.new FuncDoubleDoubleDoubleToLong(a, b, c),
               (a, b, c) -> NodeConstantLong.of(NodeFuncDoubleDoubleDoubleToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleDoubleToLong.this.canInline) {
            if (NodeFuncDoubleDoubleDoubleToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleDoubleToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleDoubleToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleDoubleToLong.this;
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
            NodeFuncDoubleDoubleDoubleToLong.FuncDoubleDoubleDoubleToLong other = (NodeFuncDoubleDoubleDoubleToLong.FuncDoubleDoubleDoubleToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleDoubleDoubleToLong {
      long apply(double var1, double var3, double var5);
   }
}
