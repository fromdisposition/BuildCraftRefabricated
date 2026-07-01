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

public class NodeFuncObjectObjectObjectToLong<A, B, C> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, B, C> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<B> argTypeB;
   private final Class<C> argTypeC;

   public NodeFuncObjectObjectObjectToLong(
      String name, Class<A> argTypeA, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, B, C> function
   ) {
      this(
         argTypeA,
         argTypeB,
         argTypeC,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", "
            + NodeTypes.getName(argTypeB)
            + ", "
            + NodeTypes.getName(argTypeC)
            + " -> long ] "
            + name
            + "("
            + a
            + ", "
            + b
            + ", "
            + c
            + ")"
      );
   }

   public NodeFuncObjectObjectObjectToLong(
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, B, C> function,
      StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.argTypeB = argTypeB;
      this.argTypeC = argTypeC;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectObjectObjectToLong<A, B, C> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeObject<B> b = stack.popObject(this.argTypeB);
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectObjectObjectToLong<A, B, C>.FuncObjectObjectObjectToLong create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC
   ) {
      return new FuncObjectObjectObjectToLong(argA, argB, argC);
   }

   public class FuncObjectObjectObjectToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeObject<B> argB;
      public final IExpressionNode.INodeObject<C> argC;

      public FuncObjectObjectObjectToLong(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectObjectObjectToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectObjectObjectToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectObjectObjectToLong.this.new FuncObjectObjectObjectToLong(a, b, c),
               (a, b, c) -> NodeFuncObjectObjectObjectToLong.this.new FuncObjectObjectObjectToLong(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectObjectObjectToLong.this.new FuncObjectObjectObjectToLong(a, b, c),
               (a, b, c) -> NodeConstantLong.of(NodeFuncObjectObjectObjectToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectObjectObjectToLong.this.canInline) {
            if (NodeFuncObjectObjectObjectToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectObjectObjectToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectObjectObjectToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectObjectObjectToLong.this;
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
            NodeFuncObjectObjectObjectToLong<A, B, C>.FuncObjectObjectObjectToLong other = (NodeFuncObjectObjectObjectToLong.FuncObjectObjectObjectToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectObjectObjectToLong<A, B, C> {
      long apply(A var1, B var2, C var3);
   }
}
