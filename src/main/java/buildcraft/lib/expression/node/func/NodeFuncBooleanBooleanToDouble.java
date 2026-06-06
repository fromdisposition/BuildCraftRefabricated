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

public class NodeFuncBooleanBooleanToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncBooleanBooleanToDouble.IFuncBooleanBooleanToDouble function;
   private final StringFunctionTri stringFunction;

   public NodeFuncBooleanBooleanToDouble(String name, NodeFuncBooleanBooleanToDouble.IFuncBooleanBooleanToDouble function) {
      this(function, (a, b) -> "[ boolean, boolean -> double ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncBooleanBooleanToDouble(NodeFuncBooleanBooleanToDouble.IFuncBooleanBooleanToDouble function, StringFunctionTri stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncBooleanBooleanToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b);
   }

   public NodeFuncBooleanBooleanToDouble.FuncBooleanBooleanToDouble create(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB) {
      return new FuncBooleanBooleanToDouble(argA, argB);
   }

   public class FuncBooleanBooleanToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;

      public FuncBooleanBooleanToDouble(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public double evaluate() {
         return NodeFuncBooleanBooleanToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncBooleanBooleanToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncBooleanBooleanToDouble.this.new FuncBooleanBooleanToDouble(a, b),
               (a, b) -> NodeFuncBooleanBooleanToDouble.this.new FuncBooleanBooleanToDouble(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncBooleanBooleanToDouble.this.new FuncBooleanBooleanToDouble(a, b),
               (a, b) -> NodeConstantDouble.of(NodeFuncBooleanBooleanToDouble.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanToDouble.this.canInline) {
            if (NodeFuncBooleanBooleanToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanToDouble.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncBooleanBooleanToDouble.FuncBooleanBooleanToDouble other = (NodeFuncBooleanBooleanToDouble.FuncBooleanBooleanToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanToDouble {
      double apply(boolean var1, boolean var2);
   }
}
