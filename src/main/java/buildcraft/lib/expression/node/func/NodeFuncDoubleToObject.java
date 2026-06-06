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

public class NodeFuncDoubleToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncDoubleToObject.IFuncDoubleToObject<R> function;
   private final StringFunctionBi stringFunction;
   private final Class<R> returnType;

   public NodeFuncDoubleToObject(String name, Class<R> returnType, NodeFuncDoubleToObject.IFuncDoubleToObject<R> function) {
      this(returnType, function, a -> "[ double -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ")");
   }

   public NodeFuncDoubleToObject(Class<R> returnType, NodeFuncDoubleToObject.IFuncDoubleToObject<R> function, StringFunctionBi stringFunction) {
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

   public NodeFuncDoubleToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a);
   }

   public NodeFuncDoubleToObject<R>.FuncDoubleToObject create(IExpressionNode.INodeDouble argA) {
      return new FuncDoubleToObject(argA);
   }

   public class FuncDoubleToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;

      public FuncDoubleToObject(IExpressionNode.INodeDouble argA) {
         this.argA = argA;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncDoubleToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncDoubleToObject.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncDoubleToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncDoubleToObject.this.new FuncDoubleToObject(a), a -> NodeFuncDoubleToObject.this.new FuncDoubleToObject(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncDoubleToObject.this.new FuncDoubleToObject(a),
               a -> new NodeConstantObject<>(NodeFuncDoubleToObject.this.returnType, NodeFuncDoubleToObject.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleToObject.this.canInline) {
            if (NodeFuncDoubleToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleToObject.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleToObject.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncDoubleToObject<R>.FuncDoubleToObject other = (NodeFuncDoubleToObject.FuncDoubleToObject)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleToObject<R> {
      R apply(double var1);
   }
}
