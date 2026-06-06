package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import java.util.Objects;

public class NodeFuncLongToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncLongToBoolean.IFuncLongToBoolean function;
   private final StringFunctionBi stringFunction;

   public NodeFuncLongToBoolean(String name, NodeFuncLongToBoolean.IFuncLongToBoolean function) {
      this(function, a -> "[ long -> boolean ] " + name + "(" + a + ")");
   }

   public NodeFuncLongToBoolean(NodeFuncLongToBoolean.IFuncLongToBoolean function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncLongToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a);
   }

   public NodeFuncLongToBoolean.FuncLongToBoolean create(IExpressionNode.INodeLong argA) {
      return new FuncLongToBoolean(argA);
   }

   public class FuncLongToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;

      public FuncLongToBoolean(IExpressionNode.INodeLong argA) {
         this.argA = argA;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncLongToBoolean.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncLongToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncLongToBoolean.this.new FuncLongToBoolean(a), a -> NodeFuncLongToBoolean.this.new FuncLongToBoolean(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncLongToBoolean.this.new FuncLongToBoolean(a),
               a -> NodeConstantBoolean.of(NodeFuncLongToBoolean.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongToBoolean.this.canInline) {
            if (NodeFuncLongToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncLongToBoolean.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongToBoolean.this;
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
            NodeFuncLongToBoolean.FuncLongToBoolean other = (NodeFuncLongToBoolean.FuncLongToBoolean)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncLongToBoolean {
      boolean apply(long var1);
   }
}
