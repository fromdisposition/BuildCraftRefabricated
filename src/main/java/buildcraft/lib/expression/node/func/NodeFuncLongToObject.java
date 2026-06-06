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

public class NodeFuncLongToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncLongToObject.IFuncLongToObject<R> function;
   private final StringFunctionBi stringFunction;
   private final Class<R> returnType;

   public NodeFuncLongToObject(String name, Class<R> returnType, NodeFuncLongToObject.IFuncLongToObject<R> function) {
      this(returnType, function, a -> "[ long -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ")");
   }

   public NodeFuncLongToObject(Class<R> returnType, NodeFuncLongToObject.IFuncLongToObject<R> function, StringFunctionBi stringFunction) {
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

   public NodeFuncLongToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a);
   }

   public NodeFuncLongToObject<R>.FuncLongToObject create(IExpressionNode.INodeLong argA) {
      return new NodeFuncLongToObject.FuncLongToObject(argA);
   }

   public class FuncLongToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;

      public FuncLongToObject(IExpressionNode.INodeLong argA) {
         this.argA = argA;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncLongToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncLongToObject.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncLongToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncLongToObject.this.new FuncLongToObject(a), a -> NodeFuncLongToObject.this.new FuncLongToObject(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncLongToObject.this.new FuncLongToObject(a),
               a -> new NodeConstantObject<>(NodeFuncLongToObject.this.returnType, NodeFuncLongToObject.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongToObject.this.canInline) {
            if (NodeFuncLongToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncLongToObject.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongToObject.this;
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
            NodeFuncLongToObject<R>.FuncLongToObject other = (NodeFuncLongToObject.FuncLongToObject)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncLongToObject<R> {
      R apply(long var1);
   }
}
