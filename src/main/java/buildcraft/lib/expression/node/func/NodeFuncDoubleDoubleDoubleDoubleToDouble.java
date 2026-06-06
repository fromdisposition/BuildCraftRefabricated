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

public class NodeFuncDoubleDoubleDoubleDoubleToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncDoubleDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleDoubleToDouble function;
   private final StringFunctionPenta stringFunction;

   public NodeFuncDoubleDoubleDoubleDoubleToDouble(String name, NodeFuncDoubleDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleDoubleToDouble function) {
      this(function, (a, b, c, d) -> "[ double, double, double, double -> double ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")");
   }

   public NodeFuncDoubleDoubleDoubleDoubleToDouble(
      NodeFuncDoubleDoubleDoubleDoubleToDouble.IFuncDoubleDoubleDoubleDoubleToDouble function, StringFunctionPenta stringFunction
   ) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncDoubleDoubleDoubleDoubleToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble d = stack.popDouble();
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b, c, d);
   }

   public NodeFuncDoubleDoubleDoubleDoubleToDouble.FuncDoubleDoubleDoubleDoubleToDouble create(
      IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC, IExpressionNode.INodeDouble argD
   ) {
      return new NodeFuncDoubleDoubleDoubleDoubleToDouble.FuncDoubleDoubleDoubleDoubleToDouble(argA, argB, argC, argD);
   }

   public class FuncDoubleDoubleDoubleDoubleToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;
      public final IExpressionNode.INodeDouble argD;

      public FuncDoubleDoubleDoubleDoubleToDouble(
         IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC, IExpressionNode.INodeDouble argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public double evaluate() {
         return NodeFuncDoubleDoubleDoubleDoubleToDouble.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncDoubleDoubleDoubleDoubleToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToDouble.this.new FuncDoubleDoubleDoubleDoubleToDouble(a, b, c, d),
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToDouble.this.new FuncDoubleDoubleDoubleDoubleToDouble(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToDouble.this.new FuncDoubleDoubleDoubleDoubleToDouble(a, b, c, d),
               (a, b, c, d) -> NodeConstantDouble.of(
                  NodeFuncDoubleDoubleDoubleDoubleToDouble.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleDoubleDoubleToDouble.this.canInline) {
            if (NodeFuncDoubleDoubleDoubleDoubleToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleDoubleDoubleToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleDoubleDoubleToDouble.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleDoubleDoubleToDouble.this;
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
            NodeFuncDoubleDoubleDoubleDoubleToDouble.FuncDoubleDoubleDoubleDoubleToDouble other = (NodeFuncDoubleDoubleDoubleDoubleToDouble.FuncDoubleDoubleDoubleDoubleToDouble)obj;
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
   public interface IFuncDoubleDoubleDoubleDoubleToDouble {
      double apply(double var1, double var3, double var5, double var7);
   }
}
