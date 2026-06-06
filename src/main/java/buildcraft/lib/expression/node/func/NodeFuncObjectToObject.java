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

public class NodeFuncObjectToObject<A, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectToObject.IFuncObjectToObject<A, R> function;
   private final StringFunctionBi stringFunction;
   private final Class<A> argTypeA;
   private final Class<R> returnType;

   public NodeFuncObjectToObject(String name, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectToObject.IFuncObjectToObject<A, R> function) {
      this(argTypeA, returnType, function, a -> "[ " + NodeTypes.getName(argTypeA) + " -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ")");
   }

   public NodeFuncObjectToObject(
      Class<A> argTypeA, Class<R> returnType, NodeFuncObjectToObject.IFuncObjectToObject<A, R> function, StringFunctionBi stringFunction
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
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncObjectToObject<A, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a);
   }

   public NodeFuncObjectToObject<A, R>.FuncObjectToObject create(IExpressionNode.INodeObject<A> argA) {
      return new NodeFuncObjectToObject.FuncObjectToObject(argA);
   }

   public class FuncObjectToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;

      public FuncObjectToObject(IExpressionNode.INodeObject<A> argA) {
         this.argA = argA;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectToObject.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncObjectToObject.this.new FuncObjectToObject(a), a -> NodeFuncObjectToObject.this.new FuncObjectToObject(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncObjectToObject.this.new FuncObjectToObject(a),
               a -> new NodeConstantObject<>(NodeFuncObjectToObject.this.returnType, NodeFuncObjectToObject.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectToObject.this.canInline) {
            if (NodeFuncObjectToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncObjectToObject.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectToObject.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncObjectToObject<A, R>.FuncObjectToObject other = (NodeFuncObjectToObject.FuncObjectToObject)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectToObject<A, R> {
      R apply(A var1);
   }
}
