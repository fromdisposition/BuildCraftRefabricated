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

public class NodeFuncObjectBooleanLongToObject<A, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<A, R> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<R> returnType;

   public NodeFuncObjectBooleanLongToObject(
      String name, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<A, R> function
   ) {
      this(
         argTypeA,
         returnType,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", boolean, long -> "
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

   public NodeFuncObjectBooleanLongToObject(
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<A, R> function,
      StringFunctionQuad stringFunction
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
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectBooleanLongToObject<A, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong c = stack.popLong();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectBooleanLongToObject<A, R>.FuncObjectBooleanLongToObject create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeLong argC
   ) {
      return new NodeFuncObjectBooleanLongToObject.FuncObjectBooleanLongToObject(argA, argB, argC);
   }

   public class FuncObjectBooleanLongToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeLong argC;

      public FuncObjectBooleanLongToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeLong argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectBooleanLongToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectBooleanLongToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectBooleanLongToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectBooleanLongToObject.this.new FuncObjectBooleanLongToObject(a, b, c),
               (a, b, c) -> NodeFuncObjectBooleanLongToObject.this.new FuncObjectBooleanLongToObject(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectBooleanLongToObject.this.new FuncObjectBooleanLongToObject(a, b, c),
               (a, b, c) -> new NodeConstantObject<>(
                  NodeFuncObjectBooleanLongToObject.this.returnType,
                  NodeFuncObjectBooleanLongToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectBooleanLongToObject.this.canInline) {
            if (NodeFuncObjectBooleanLongToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectBooleanLongToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectBooleanLongToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectBooleanLongToObject.this;
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
            NodeFuncObjectBooleanLongToObject<A, R>.FuncObjectBooleanLongToObject other = (NodeFuncObjectBooleanLongToObject.FuncObjectBooleanLongToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectBooleanLongToObject<A, R> {
      R apply(A var1, boolean var2, long var3);
   }
}
