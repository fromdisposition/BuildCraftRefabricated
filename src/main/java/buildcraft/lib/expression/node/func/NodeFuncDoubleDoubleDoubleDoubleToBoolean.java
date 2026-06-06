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

public class NodeFuncDoubleDoubleDoubleDoubleToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncDoubleDoubleDoubleDoubleToBoolean.IFuncDoubleDoubleDoubleDoubleToBoolean function;
   private final StringFunctionPenta stringFunction;

   public NodeFuncDoubleDoubleDoubleDoubleToBoolean(String name, NodeFuncDoubleDoubleDoubleDoubleToBoolean.IFuncDoubleDoubleDoubleDoubleToBoolean function) {
      this(function, (a, b, c, d) -> "[ double, double, double, double -> boolean ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")");
   }

   public NodeFuncDoubleDoubleDoubleDoubleToBoolean(
      NodeFuncDoubleDoubleDoubleDoubleToBoolean.IFuncDoubleDoubleDoubleDoubleToBoolean function, StringFunctionPenta stringFunction
   ) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncDoubleDoubleDoubleDoubleToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble d = stack.popDouble();
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b, c, d);
   }

   public NodeFuncDoubleDoubleDoubleDoubleToBoolean.FuncDoubleDoubleDoubleDoubleToBoolean create(
      IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC, IExpressionNode.INodeDouble argD
   ) {
      return new FuncDoubleDoubleDoubleDoubleToBoolean(argA, argB, argC, argD);
   }

   public class FuncDoubleDoubleDoubleDoubleToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;
      public final IExpressionNode.INodeDouble argD;

      public FuncDoubleDoubleDoubleDoubleToBoolean(
         IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC, IExpressionNode.INodeDouble argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.new FuncDoubleDoubleDoubleDoubleToBoolean(a, b, c, d),
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.new FuncDoubleDoubleDoubleDoubleToBoolean(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.new FuncDoubleDoubleDoubleDoubleToBoolean(a, b, c, d),
               (a, b, c, d) -> NodeConstantBoolean.of(
                  NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.canInline) {
            if (NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleDoubleDoubleToBoolean.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleDoubleDoubleToBoolean.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncDoubleDoubleDoubleDoubleToBoolean.FuncDoubleDoubleDoubleDoubleToBoolean other = (NodeFuncDoubleDoubleDoubleDoubleToBoolean.FuncDoubleDoubleDoubleDoubleToBoolean)obj;
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
   public interface IFuncDoubleDoubleDoubleDoubleToBoolean {
      boolean apply(double var1, double var3, double var5, double var7);
   }
}
