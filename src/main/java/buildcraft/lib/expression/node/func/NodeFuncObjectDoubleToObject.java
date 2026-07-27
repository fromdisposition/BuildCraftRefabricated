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

public class NodeFuncObjectDoubleToObject<A, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<A, R> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;
   private final Class<R> returnType;

   public NodeFuncObjectDoubleToObject(
      String name, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<A, R> function
   ) {
      this(
         argTypeA,
         returnType,
         function,
         (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", double -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ")"
      );
   }

   public NodeFuncObjectDoubleToObject(
      Class<A> argTypeA, Class<R> returnType, NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<A, R> function, StringFunctionTri stringFunction
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

   public NodeFuncObjectDoubleToObject<A, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectDoubleToObject<A, R>.FuncObjectDoubleToObject create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB) {
      return new FuncObjectDoubleToObject(argA, argB);
   }

   public class FuncObjectDoubleToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;

      public FuncObjectDoubleToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectDoubleToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectDoubleToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectDoubleToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectDoubleToObject.this.new FuncObjectDoubleToObject(a, b),
               (a, b) -> NodeFuncObjectDoubleToObject.this.new FuncObjectDoubleToObject(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectDoubleToObject.this.new FuncObjectDoubleToObject(a, b),
               (a, b) -> new NodeConstantObject<>(
                  NodeFuncObjectDoubleToObject.this.returnType, NodeFuncObjectDoubleToObject.this.function.apply(a.evaluate(), b.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleToObject.this.canInline) {
            if (NodeFuncObjectDoubleToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleToObject.this;
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
            NodeFuncObjectDoubleToObject<A, R>.FuncObjectDoubleToObject other = (NodeFuncObjectDoubleToObject.FuncObjectDoubleToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleToObject<A, R> {
      R apply(A var1, double var2);
   }
}
