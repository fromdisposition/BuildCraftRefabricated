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

public class NodeFuncObjectObjectToObject<A, B, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, B, R> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;
   private final Class<B> argTypeB;
   private final Class<R> returnType;

   public NodeFuncObjectObjectToObject(
      String name, Class<A> argTypeA, Class<B> argTypeB, Class<R> returnType, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, B, R> function
   ) {
      this(
         argTypeA,
         argTypeB,
         returnType,
         function,
         (a, b) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", "
            + NodeTypes.getName(argTypeB)
            + " -> "
            + NodeTypes.getName(returnType)
            + " ] "
            + name
            + "("
            + a
            + ", "
            + b
            + ")"
      );
   }

   public NodeFuncObjectObjectToObject(
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, B, R> function,
      StringFunctionTri stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.argTypeB = argTypeB;
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
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectObjectToObject<A, B, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<B> b = stack.popObject(this.argTypeB);
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectObjectToObject<A, B, R>.FuncObjectObjectToObject create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB) {
      return new FuncObjectObjectToObject(argA, argB);
   }

   public class FuncObjectObjectToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeObject<B> argB;

      public FuncObjectObjectToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectObjectToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectObjectToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectObjectToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectObjectToObject.this.new FuncObjectObjectToObject(a, b),
               (a, b) -> NodeFuncObjectObjectToObject.this.new FuncObjectObjectToObject(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectObjectToObject.this.new FuncObjectObjectToObject(a, b),
               (a, b) -> new NodeConstantObject<>(
                  NodeFuncObjectObjectToObject.this.returnType, NodeFuncObjectObjectToObject.this.function.apply(a.evaluate(), b.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectObjectToObject.this.canInline) {
            if (NodeFuncObjectObjectToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectObjectToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectObjectToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectObjectToObject.this;
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
            NodeFuncObjectObjectToObject<A, B, R>.FuncObjectObjectToObject other = (NodeFuncObjectObjectToObject.FuncObjectObjectToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectObjectToObject<A, B, R> {
      R apply(A var1, B var2);
   }
}
