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

public class NodeFuncObjectBooleanToObject<A, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<A, R> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;
   private final Class<R> returnType;

   public NodeFuncObjectBooleanToObject(
      String name, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<A, R> function
   ) {
      this(
         argTypeA,
         returnType,
         function,
         (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", boolean -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ")"
      );
   }

   public NodeFuncObjectBooleanToObject(
      Class<A> argTypeA, Class<R> returnType, NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<A, R> function, StringFunctionTri stringFunction
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

   public NodeFuncObjectBooleanToObject<A, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectBooleanToObject<A, R>.FuncObjectBooleanToObject create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB) {
      return new NodeFuncObjectBooleanToObject.FuncObjectBooleanToObject(argA, argB);
   }

   public class FuncObjectBooleanToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeBoolean argB;

      public FuncObjectBooleanToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectBooleanToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectBooleanToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectBooleanToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectBooleanToObject.this.new FuncObjectBooleanToObject(a, b),
               (a, b) -> NodeFuncObjectBooleanToObject.this.new FuncObjectBooleanToObject(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectBooleanToObject.this.new FuncObjectBooleanToObject(a, b),
               (a, b) -> new NodeConstantObject<>(
                  NodeFuncObjectBooleanToObject.this.returnType, NodeFuncObjectBooleanToObject.this.function.apply(a.evaluate(), b.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectBooleanToObject.this.canInline) {
            if (NodeFuncObjectBooleanToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectBooleanToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectBooleanToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectBooleanToObject.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncObjectBooleanToObject<A, R>.FuncObjectBooleanToObject other = (NodeFuncObjectBooleanToObject.FuncObjectBooleanToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectBooleanToObject<A, R> {
      R apply(A var1, boolean var2);
   }
}
