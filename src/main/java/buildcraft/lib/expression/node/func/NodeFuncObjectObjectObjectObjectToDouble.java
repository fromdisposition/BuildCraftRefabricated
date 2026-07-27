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
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import java.util.Objects;

public class NodeFuncObjectObjectObjectObjectToDouble<A, B, C, D> extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, C, D> function;
   private final StringFunctionPenta stringFunction;
   private final Class<A> argTypeA;
   private final Class<B> argTypeB;
   private final Class<C> argTypeC;
   private final Class<D> argTypeD;

   public NodeFuncObjectObjectObjectObjectToDouble(
      String name,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, C, D> function
   ) {
      this(
         argTypeA,
         argTypeB,
         argTypeC,
         argTypeD,
         function,
         (a, b, c, d) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", "
            + NodeTypes.getName(argTypeB)
            + ", "
            + NodeTypes.getName(argTypeC)
            + ", "
            + NodeTypes.getName(argTypeD)
            + " -> double ] "
            + name
            + "("
            + a
            + ", "
            + b
            + ", "
            + c
            + ", "
            + d
            + ")"
      );
   }

   public NodeFuncObjectObjectObjectObjectToDouble(
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, C, D> function,
      StringFunctionPenta stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.argTypeB = argTypeB;
      this.argTypeC = argTypeC;
      this.argTypeD = argTypeD;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncObjectObjectObjectObjectToDouble<A, B, C, D> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<D> d = stack.popObject(this.argTypeD);
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeObject<B> b = stack.popObject(this.argTypeB);
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c, d);
   }

   public NodeFuncObjectObjectObjectObjectToDouble<A, B, C, D>.FuncObjectObjectObjectObjectToDouble create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC, IExpressionNode.INodeObject<D> argD
   ) {
      return new FuncObjectObjectObjectObjectToDouble(argA, argB, argC, argD);
   }

   public class FuncObjectObjectObjectObjectToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeObject<B> argB;
      public final IExpressionNode.INodeObject<C> argC;
      public final IExpressionNode.INodeObject<D> argD;

      public FuncObjectObjectObjectObjectToDouble(
         IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC, IExpressionNode.INodeObject<D> argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public double evaluate() {
         return NodeFuncObjectObjectObjectObjectToDouble.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncObjectObjectObjectObjectToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToDouble.this.new FuncObjectObjectObjectObjectToDouble(a, b, c, d),
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToDouble.this.new FuncObjectObjectObjectObjectToDouble(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToDouble.this.new FuncObjectObjectObjectObjectToDouble(a, b, c, d),
               (a, b, c, d) -> NodeConstantDouble.of(
                  NodeFuncObjectObjectObjectObjectToDouble.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectObjectObjectObjectToDouble.this.canInline) {
            if (NodeFuncObjectObjectObjectObjectToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectObjectObjectObjectToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncObjectObjectObjectObjectToDouble.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectObjectObjectObjectToDouble.this;
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
            NodeFuncObjectObjectObjectObjectToDouble<A, B, C, D>.FuncObjectObjectObjectObjectToDouble other = (NodeFuncObjectObjectObjectObjectToDouble.FuncObjectObjectObjectObjectToDouble)obj;
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
   public interface IFuncObjectObjectObjectObjectToDouble<A, B, C, D> {
      double apply(A var1, B var2, C var3, D var4);
   }
}
