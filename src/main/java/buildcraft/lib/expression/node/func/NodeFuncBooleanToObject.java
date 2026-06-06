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

public class NodeFuncBooleanToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncBooleanToObject.IFuncBooleanToObject<R> function;
   private final StringFunctionBi stringFunction;
   private final Class<R> returnType;

   public NodeFuncBooleanToObject(String name, Class<R> returnType, NodeFuncBooleanToObject.IFuncBooleanToObject<R> function) {
      this(returnType, function, a -> "[ boolean -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ")");
   }

   public NodeFuncBooleanToObject(Class<R> returnType, NodeFuncBooleanToObject.IFuncBooleanToObject<R> function, StringFunctionBi stringFunction) {
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

   public NodeFuncBooleanToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a);
   }

   public NodeFuncBooleanToObject<R>.FuncBooleanToObject create(IExpressionNode.INodeBoolean argA) {
      return new FuncBooleanToObject(argA);
   }

   public class FuncBooleanToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;

      public FuncBooleanToObject(IExpressionNode.INodeBoolean argA) {
         this.argA = argA;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncBooleanToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncBooleanToObject.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncBooleanToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncBooleanToObject.this.new FuncBooleanToObject(a), a -> NodeFuncBooleanToObject.this.new FuncBooleanToObject(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncBooleanToObject.this.new FuncBooleanToObject(a),
               a -> new NodeConstantObject<>(NodeFuncBooleanToObject.this.returnType, NodeFuncBooleanToObject.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanToObject.this.canInline) {
            if (NodeFuncBooleanToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanToObject.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanToObject.this;
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
            NodeFuncBooleanToObject<R>.FuncBooleanToObject other = (NodeFuncBooleanToObject.FuncBooleanToObject)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanToObject<R> {
      R apply(boolean var1);
   }
}
