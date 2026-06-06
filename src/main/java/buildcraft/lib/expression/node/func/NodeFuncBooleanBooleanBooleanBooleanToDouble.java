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

public class NodeFuncBooleanBooleanBooleanBooleanToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncBooleanBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanBooleanToDouble function;
   private final StringFunctionPenta stringFunction;

   public NodeFuncBooleanBooleanBooleanBooleanToDouble(
      String name, NodeFuncBooleanBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanBooleanToDouble function
   ) {
      this(function, (a, b, c, d) -> "[ boolean, boolean, boolean, boolean -> double ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")");
   }

   public NodeFuncBooleanBooleanBooleanBooleanToDouble(
      NodeFuncBooleanBooleanBooleanBooleanToDouble.IFuncBooleanBooleanBooleanBooleanToDouble function, StringFunctionPenta stringFunction
   ) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncBooleanBooleanBooleanBooleanToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean d = stack.popBoolean();
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b, c, d);
   }

   public NodeFuncBooleanBooleanBooleanBooleanToDouble.FuncBooleanBooleanBooleanBooleanToDouble create(
      IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC, IExpressionNode.INodeBoolean argD
   ) {
      return new NodeFuncBooleanBooleanBooleanBooleanToDouble.FuncBooleanBooleanBooleanBooleanToDouble(argA, argB, argC, argD);
   }

   public class FuncBooleanBooleanBooleanBooleanToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;
      public final IExpressionNode.INodeBoolean argD;

      public FuncBooleanBooleanBooleanBooleanToDouble(
         IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC, IExpressionNode.INodeBoolean argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public double evaluate() {
         return NodeFuncBooleanBooleanBooleanBooleanToDouble.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncBooleanBooleanBooleanBooleanToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToDouble.this.new FuncBooleanBooleanBooleanBooleanToDouble(a, b, c, d),
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToDouble.this.new FuncBooleanBooleanBooleanBooleanToDouble(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToDouble.this.new FuncBooleanBooleanBooleanBooleanToDouble(a, b, c, d),
               (a, b, c, d) -> NodeConstantDouble.of(
                  NodeFuncBooleanBooleanBooleanBooleanToDouble.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanBooleanBooleanToDouble.this.canInline) {
            if (NodeFuncBooleanBooleanBooleanBooleanToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanBooleanBooleanToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanBooleanBooleanToDouble.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanBooleanBooleanToDouble.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncBooleanBooleanBooleanBooleanToDouble.FuncBooleanBooleanBooleanBooleanToDouble other = (NodeFuncBooleanBooleanBooleanBooleanToDouble.FuncBooleanBooleanBooleanBooleanToDouble)obj;
            return Objects.equals(this.argA, other.argA)
               && Objects.equals(this.argB, other.argB)
               && Objects.equals(this.argC, other.argC)
               && Objects.equals(this.argD, other.argD);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanBooleanBooleanToDouble {
      double apply(boolean var1, boolean var2, boolean var3, boolean var4);
   }
}
