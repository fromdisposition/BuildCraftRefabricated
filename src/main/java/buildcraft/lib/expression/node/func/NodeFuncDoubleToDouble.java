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

public class NodeFuncDoubleToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncDoubleToDouble.IFuncDoubleToDouble function;
   private final StringFunctionBi stringFunction;

   public NodeFuncDoubleToDouble(String name, NodeFuncDoubleToDouble.IFuncDoubleToDouble function) {
      this(function, a -> "[ double -> double ] " + name + "(" + a + ")");
   }

   public NodeFuncDoubleToDouble(NodeFuncDoubleToDouble.IFuncDoubleToDouble function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncDoubleToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a);
   }

   public NodeFuncDoubleToDouble.FuncDoubleToDouble create(IExpressionNode.INodeDouble argA) {
      return new FuncDoubleToDouble(argA);
   }

   public class FuncDoubleToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;

      public FuncDoubleToDouble(IExpressionNode.INodeDouble argA) {
         this.argA = argA;
      }

      @Override
      public double evaluate() {
         return NodeFuncDoubleToDouble.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncDoubleToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncDoubleToDouble.this.new FuncDoubleToDouble(a), a -> NodeFuncDoubleToDouble.this.new FuncDoubleToDouble(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncDoubleToDouble.this.new FuncDoubleToDouble(a),
               a -> NodeConstantDouble.of(NodeFuncDoubleToDouble.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleToDouble.this.canInline) {
            if (NodeFuncDoubleToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleToDouble.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleToDouble.this;
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
            NodeFuncDoubleToDouble.FuncDoubleToDouble other = (NodeFuncDoubleToDouble.FuncDoubleToDouble)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleToDouble {
      double apply(double var1);
   }
}
