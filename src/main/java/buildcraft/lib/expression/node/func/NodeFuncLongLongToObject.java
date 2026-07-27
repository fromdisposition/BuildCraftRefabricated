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

public class NodeFuncLongLongToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncLongLongToObject.IFuncLongLongToObject<R> function;
   private final StringFunctionTri stringFunction;
   private final Class<R> returnType;

   public NodeFuncLongLongToObject(String name, Class<R> returnType, NodeFuncLongLongToObject.IFuncLongLongToObject<R> function) {
      this(returnType, function, (a, b) -> "[ long, long -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncLongLongToObject(Class<R> returnType, NodeFuncLongLongToObject.IFuncLongLongToObject<R> function, StringFunctionTri stringFunction) {
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

   public NodeFuncLongLongToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a, b);
   }

   public NodeFuncLongLongToObject<R>.FuncLongLongToObject create(IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB) {
      return new FuncLongLongToObject(argA, argB);
   }

   public class FuncLongLongToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;
      public final IExpressionNode.INodeLong argB;

      public FuncLongLongToObject(IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncLongLongToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncLongLongToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncLongLongToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncLongLongToObject.this.new FuncLongLongToObject(a, b),
               (a, b) -> NodeFuncLongLongToObject.this.new FuncLongLongToObject(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncLongLongToObject.this.new FuncLongLongToObject(a, b),
               (a, b) -> new NodeConstantObject<>(
                  NodeFuncLongLongToObject.this.returnType, NodeFuncLongLongToObject.this.function.apply(a.evaluate(), b.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongLongToObject.this.canInline) {
            if (NodeFuncLongLongToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongLongToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncLongLongToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongLongToObject.this;
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
            NodeFuncLongLongToObject<R>.FuncLongLongToObject other = (NodeFuncLongLongToObject.FuncLongLongToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncLongLongToObject<R> {
      R apply(long var1, long var3);
   }
}
