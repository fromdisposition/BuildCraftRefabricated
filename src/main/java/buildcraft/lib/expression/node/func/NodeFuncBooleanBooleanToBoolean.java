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

public class NodeFuncBooleanBooleanToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncBooleanBooleanToBoolean.IFuncBooleanBooleanToBoolean function;
   private final StringFunctionTri stringFunction;

   public NodeFuncBooleanBooleanToBoolean(String name, NodeFuncBooleanBooleanToBoolean.IFuncBooleanBooleanToBoolean function) {
      this(function, (a, b) -> "[ boolean, boolean -> boolean ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncBooleanBooleanToBoolean(NodeFuncBooleanBooleanToBoolean.IFuncBooleanBooleanToBoolean function, StringFunctionTri stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncBooleanBooleanToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b);
   }

   public NodeFuncBooleanBooleanToBoolean.FuncBooleanBooleanToBoolean create(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB) {
      return new NodeFuncBooleanBooleanToBoolean.FuncBooleanBooleanToBoolean(argA, argB);
   }

   public class FuncBooleanBooleanToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;

      public FuncBooleanBooleanToBoolean(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncBooleanBooleanToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncBooleanBooleanToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncBooleanBooleanToBoolean.this.new FuncBooleanBooleanToBoolean(a, b),
               (a, b) -> NodeFuncBooleanBooleanToBoolean.this.new FuncBooleanBooleanToBoolean(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncBooleanBooleanToBoolean.this.new FuncBooleanBooleanToBoolean(a, b),
               (a, b) -> NodeConstantBoolean.of(NodeFuncBooleanBooleanToBoolean.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanToBoolean.this.canInline) {
            if (NodeFuncBooleanBooleanToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanToBoolean.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanToBoolean.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncBooleanBooleanToBoolean.FuncBooleanBooleanToBoolean other = (NodeFuncBooleanBooleanToBoolean.FuncBooleanBooleanToBoolean)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanToBoolean {
      boolean apply(boolean var1, boolean var2);
   }
}
