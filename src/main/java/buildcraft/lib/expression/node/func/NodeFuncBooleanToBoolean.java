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

public class NodeFuncBooleanToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncBooleanToBoolean.IFuncBooleanToBoolean function;
   private final StringFunctionBi stringFunction;

   public NodeFuncBooleanToBoolean(String name, NodeFuncBooleanToBoolean.IFuncBooleanToBoolean function) {
      this(function, a -> "[ boolean -> boolean ] " + name + "(" + a + ")");
   }

   public NodeFuncBooleanToBoolean(NodeFuncBooleanToBoolean.IFuncBooleanToBoolean function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncBooleanToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a);
   }

   public NodeFuncBooleanToBoolean.FuncBooleanToBoolean create(IExpressionNode.INodeBoolean argA) {
      return new FuncBooleanToBoolean(argA);
   }

   public class FuncBooleanToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;

      public FuncBooleanToBoolean(IExpressionNode.INodeBoolean argA) {
         this.argA = argA;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncBooleanToBoolean.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncBooleanToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncBooleanToBoolean.this.new FuncBooleanToBoolean(a), a -> NodeFuncBooleanToBoolean.this.new FuncBooleanToBoolean(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncBooleanToBoolean.this.new FuncBooleanToBoolean(a),
               a -> NodeConstantBoolean.of(NodeFuncBooleanToBoolean.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanToBoolean.this.canInline) {
            if (NodeFuncBooleanToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanToBoolean.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanToBoolean.this;
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
            NodeFuncBooleanToBoolean.FuncBooleanToBoolean other = (NodeFuncBooleanToBoolean.FuncBooleanToBoolean)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanToBoolean {
      boolean apply(boolean var1);
   }
}
