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

public class NodeFuncObjectLongObjectToObject<A, C, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, C, R> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<C> argTypeC;
   private final Class<R> returnType;

   public NodeFuncObjectLongObjectToObject(
      String name, Class<A> argTypeA, Class<C> argTypeC, Class<R> returnType, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, C, R> function
   ) {
      this(
         argTypeA,
         argTypeC,
         returnType,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", long, "
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

   public NodeFuncObjectLongObjectToObject(
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, C, R> function,
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

   public NodeFuncObjectLongObjectToObject<A, C, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectLongObjectToObject<A, C, R>.FuncObjectLongObjectToObject create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeObject<C> argC
   ) {
      return new NodeFuncObjectLongObjectToObject.FuncObjectLongObjectToObject(argA, argB, argC);
   }

   public class FuncObjectLongObjectToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeLong argB;
      public final IExpressionNode.INodeObject<C> argC;

      public FuncObjectLongObjectToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeObject<C> argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectLongObjectToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectLongObjectToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectLongObjectToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectLongObjectToObject.this.new FuncObjectLongObjectToObject(a, b, c),
               (a, b, c) -> NodeFuncObjectLongObjectToObject.this.new FuncObjectLongObjectToObject(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectLongObjectToObject.this.new FuncObjectLongObjectToObject(a, b, c),
               (a, b, c) -> new NodeConstantObject<>(
                  NodeFuncObjectLongObjectToObject.this.returnType,
                  NodeFuncObjectLongObjectToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectLongObjectToObject.this.canInline) {
            if (NodeFuncObjectLongObjectToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectLongObjectToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectLongObjectToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectLongObjectToObject.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncObjectLongObjectToObject<A, C, R>.FuncObjectLongObjectToObject other = (NodeFuncObjectLongObjectToObject.FuncObjectLongObjectToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectLongObjectToObject<A, C, R> {
      R apply(A var1, long var2, C var4);
   }
}
