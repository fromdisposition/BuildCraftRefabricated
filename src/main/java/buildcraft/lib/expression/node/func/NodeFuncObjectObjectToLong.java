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
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import java.util.Objects;

public class NodeFuncObjectObjectToLong<A, B> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<A, B> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;
   private final Class<B> argTypeB;

   public NodeFuncObjectObjectToLong(String name, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<A, B> function) {
      this(
         argTypeA,
         argTypeB,
         function,
         (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", " + NodeTypes.getName(argTypeB) + " -> long ] " + name + "(" + a + ", " + b + ")"
      );
   }

   public NodeFuncObjectObjectToLong(
      Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<A, B> function, StringFunctionTri stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.argTypeB = argTypeB;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectObjectToLong<A, B> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<B> b = stack.popObject(this.argTypeB);
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectObjectToLong<A, B>.FuncObjectObjectToLong create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB) {
      return new FuncObjectObjectToLong(argA, argB);
   }

   public class FuncObjectObjectToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeObject<B> argB;

      public FuncObjectObjectToLong(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectObjectToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectObjectToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectObjectToLong.this.new FuncObjectObjectToLong(a, b),
               (a, b) -> NodeFuncObjectObjectToLong.this.new FuncObjectObjectToLong(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectObjectToLong.this.new FuncObjectObjectToLong(a, b),
               (a, b) -> NodeConstantLong.of(NodeFuncObjectObjectToLong.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectObjectToLong.this.canInline) {
            if (NodeFuncObjectObjectToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectObjectToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectObjectToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectObjectToLong.this;
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
            NodeFuncObjectObjectToLong<A, B>.FuncObjectObjectToLong other = (NodeFuncObjectObjectToLong.FuncObjectObjectToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectObjectToLong<A, B> {
      long apply(A var1, B var2);
   }
}
