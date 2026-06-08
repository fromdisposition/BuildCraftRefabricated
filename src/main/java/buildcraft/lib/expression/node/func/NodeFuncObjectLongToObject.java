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

public class NodeFuncObjectLongToObject<A, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectLongToObject.IFuncObjectLongToObject<A, R> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;
   private final Class<R> returnType;

   public NodeFuncObjectLongToObject(String name, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectLongToObject.IFuncObjectLongToObject<A, R> function) {
      this(
         argTypeA,
         returnType,
         function,
         (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", long -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ")"
      );
   }

   public NodeFuncObjectLongToObject(
      Class<A> argTypeA, Class<R> returnType, NodeFuncObjectLongToObject.IFuncObjectLongToObject<A, R> function, StringFunctionTri stringFunction
   ) {
      this.argTypeA = argTypeA;
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

   public NodeFuncObjectLongToObject<A, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectLongToObject<A, R>.FuncObjectLongToObject create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB) {
      return new FuncObjectLongToObject(argA, argB);
   }

   public class FuncObjectLongToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeLong argB;

      public FuncObjectLongToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectLongToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectLongToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectLongToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectLongToObject.this.new FuncObjectLongToObject(a, b),
               (a, b) -> NodeFuncObjectLongToObject.this.new FuncObjectLongToObject(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectLongToObject.this.new FuncObjectLongToObject(a, b),
               (a, b) -> new NodeConstantObject<>(
                  NodeFuncObjectLongToObject.this.returnType, NodeFuncObjectLongToObject.this.function.apply(a.evaluate(), b.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectLongToObject.this.canInline) {
            if (NodeFuncObjectLongToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectLongToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectLongToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectLongToObject.this;
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
            NodeFuncObjectLongToObject<A, R>.FuncObjectLongToObject other = (NodeFuncObjectLongToObject.FuncObjectLongToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectLongToObject<A, R> {
      R apply(A var1, long var2);
   }
}
