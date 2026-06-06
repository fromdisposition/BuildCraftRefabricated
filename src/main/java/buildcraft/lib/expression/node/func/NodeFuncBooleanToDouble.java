package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import java.util.Objects;

public class NodeFuncBooleanToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncBooleanToDouble.IFuncBooleanToDouble function;
   private final StringFunctionBi stringFunction;

   public NodeFuncBooleanToDouble(String name, NodeFuncBooleanToDouble.IFuncBooleanToDouble function) {
      this(function, a -> "[ boolean -> double ] " + name + "(" + a + ")");
   }

   public NodeFuncBooleanToDouble(NodeFuncBooleanToDouble.IFuncBooleanToDouble function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncBooleanToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a);
   }

   public NodeFuncBooleanToDouble.FuncBooleanToDouble create(IExpressionNode.INodeBoolean argA) {
      return new FuncBooleanToDouble(argA);
   }

   public class FuncBooleanToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;

      public FuncBooleanToDouble(IExpressionNode.INodeBoolean argA) {
         this.argA = argA;
      }

      @Override
      public double evaluate() {
         return NodeFuncBooleanToDouble.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncBooleanToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncBooleanToDouble.this.new FuncBooleanToDouble(a), a -> NodeFuncBooleanToDouble.this.new FuncBooleanToDouble(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncBooleanToDouble.this.new FuncBooleanToDouble(a),
               a -> NodeConstantDouble.of(NodeFuncBooleanToDouble.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanToDouble.this.canInline) {
            if (NodeFuncBooleanToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanToDouble.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanToDouble.this;
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
            NodeFuncBooleanToDouble.FuncBooleanToDouble other = (NodeFuncBooleanToDouble.FuncBooleanToDouble)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanToDouble {
      double apply(boolean var1);
   }
}
