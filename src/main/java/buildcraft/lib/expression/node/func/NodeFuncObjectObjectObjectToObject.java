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
import buildcraft.lib.expression.node.value.NodeConstantObject;
import java.util.Objects;

public class NodeFuncObjectObjectObjectToObject<A, B, C, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, C, R> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<B> argTypeB;
   private final Class<C> argTypeC;
   private final Class<R> returnType;

   public NodeFuncObjectObjectObjectToObject(
      String name,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, C, R> function
   ) {
      this(
         argTypeA,
         argTypeB,
         argTypeC,
         returnType,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", "
            + NodeTypes.getName(argTypeB)
            + ", "
            + NodeTypes.getName(argTypeC)
            + " -> "
            + NodeTypes.getName(returnType)
            + " ] "
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

   public NodeFuncObjectObjectObjectToObject(
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, C, R> function,
      StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.argTypeB = argTypeB;
      this.argTypeC = argTypeC;
      this.returnType = returnType;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public Class<R> getType() {
      return this.returnType;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectObjectObjectToObject<A, B, C, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeObject<B> b = stack.popObject(this.argTypeB);
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectObjectObjectToObject<A, B, C, R>.FuncObjectObjectObjectToObject create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC
   ) {
      return new FuncObjectObjectObjectToObject(argA, argB, argC);
   }

   public class FuncObjectObjectObjectToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeObject<B> argB;
      public final IExpressionNode.INodeObject<C> argC;

      public FuncObjectObjectObjectToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectObjectObjectToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectObjectObjectToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectObjectObjectToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectObjectObjectToObject.this.new FuncObjectObjectObjectToObject(a, b, c),
               (a, b, c) -> NodeFuncObjectObjectObjectToObject.this.new FuncObjectObjectObjectToObject(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectObjectObjectToObject.this.new FuncObjectObjectObjectToObject(a, b, c),
               (a, b, c) -> new NodeConstantObject<>(
                  NodeFuncObjectObjectObjectToObject.this.returnType,
                  NodeFuncObjectObjectObjectToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectObjectObjectToObject.this.canInline) {
            if (NodeFuncObjectObjectObjectToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectObjectObjectToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectObjectObjectToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectObjectObjectToObject.this;
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
            NodeFuncObjectObjectObjectToObject<A, B, C, R>.FuncObjectObjectObjectToObject other = (NodeFuncObjectObjectObjectToObject.FuncObjectObjectObjectToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectObjectObjectToObject<A, B, C, R> {
      R apply(A var1, B var2, C var3);
   }
}
