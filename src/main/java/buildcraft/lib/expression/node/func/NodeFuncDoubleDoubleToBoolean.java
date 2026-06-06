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

public class NodeFuncDoubleDoubleToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncDoubleDoubleToBoolean.IFuncDoubleDoubleToBoolean function;
   private final StringFunctionTri stringFunction;

   public NodeFuncDoubleDoubleToBoolean(String name, NodeFuncDoubleDoubleToBoolean.IFuncDoubleDoubleToBoolean function) {
      this(function, (a, b) -> "[ double, double -> boolean ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncDoubleDoubleToBoolean(NodeFuncDoubleDoubleToBoolean.IFuncDoubleDoubleToBoolean function, StringFunctionTri stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncDoubleDoubleToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b);
   }

   public NodeFuncDoubleDoubleToBoolean.FuncDoubleDoubleToBoolean create(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB) {
      return new FuncDoubleDoubleToBoolean(argA, argB);
   }

   public class FuncDoubleDoubleToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;

      public FuncDoubleDoubleToBoolean(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncDoubleDoubleToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncDoubleDoubleToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncDoubleDoubleToBoolean.this.new FuncDoubleDoubleToBoolean(a, b),
               (a, b) -> NodeFuncDoubleDoubleToBoolean.this.new FuncDoubleDoubleToBoolean(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncDoubleDoubleToBoolean.this.new FuncDoubleDoubleToBoolean(a, b),
               (a, b) -> NodeConstantBoolean.of(NodeFuncDoubleDoubleToBoolean.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleToBoolean.this.canInline) {
            if (NodeFuncDoubleDoubleToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleToBoolean.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleToBoolean.this;
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
            NodeFuncDoubleDoubleToBoolean.FuncDoubleDoubleToBoolean other = (NodeFuncDoubleDoubleToBoolean.FuncDoubleDoubleToBoolean)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleDoubleToBoolean {
      boolean apply(double var1, double var3);
   }
}
