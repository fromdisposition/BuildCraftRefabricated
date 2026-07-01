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

public class NodeFuncObjectBooleanObjectToObject<A, C, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, C, R> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<C> argTypeC;
   private final Class<R> returnType;

   public NodeFuncObjectBooleanObjectToObject(
      String name,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, C, R> function
   ) {
      this(
         argTypeA,
         argTypeC,
         returnType,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", boolean, "
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

   public NodeFuncObjectBooleanObjectToObject(
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, C, R> function,
      StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
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

   public NodeFuncObjectBooleanObjectToObject<A, C, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectBooleanObjectToObject<A, C, R>.FuncObjectBooleanObjectToObject create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeObject<C> argC
   ) {
      return new FuncObjectBooleanObjectToObject(argA, argB, argC);
   }

   public class FuncObjectBooleanObjectToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeObject<C> argC;

      public FuncObjectBooleanObjectToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeObject<C> argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectBooleanObjectToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectBooleanObjectToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectBooleanObjectToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectBooleanObjectToObject.this.new FuncObjectBooleanObjectToObject(a, b, c),
               (a, b, c) -> NodeFuncObjectBooleanObjectToObject.this.new FuncObjectBooleanObjectToObject(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectBooleanObjectToObject.this.new FuncObjectBooleanObjectToObject(a, b, c),
               (a, b, c) -> new NodeConstantObject<>(
                  NodeFuncObjectBooleanObjectToObject.this.returnType,
                  NodeFuncObjectBooleanObjectToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectBooleanObjectToObject.this.canInline) {
            if (NodeFuncObjectBooleanObjectToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectBooleanObjectToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectBooleanObjectToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectBooleanObjectToObject.this;
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
            NodeFuncObjectBooleanObjectToObject<A, C, R>.FuncObjectBooleanObjectToObject other = (NodeFuncObjectBooleanObjectToObject.FuncObjectBooleanObjectToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectBooleanObjectToObject<A, C, R> {
      R apply(A var1, boolean var2, C var3);
   }
}
