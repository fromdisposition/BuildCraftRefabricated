package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import java.util.Objects;

public class NodeFuncObjectBooleanToDouble<A> extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncObjectBooleanToDouble.IFuncObjectBooleanToDouble<A> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectBooleanToDouble(String name, Class<A> argTypeA, NodeFuncObjectBooleanToDouble.IFuncObjectBooleanToDouble<A> function) {
      this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", boolean -> double ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncObjectBooleanToDouble(
      Class<A> argTypeA, NodeFuncObjectBooleanToDouble.IFuncObjectBooleanToDouble<A> function, StringFunctionTri stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectBooleanToDouble<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectBooleanToDouble<A>.FuncObjectBooleanToDouble create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB) {
      return new FuncObjectBooleanToDouble(argA, argB);
   }

   public class FuncObjectBooleanToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeBoolean argB;

      public FuncObjectBooleanToDouble(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public double evaluate() {
         return NodeFuncObjectBooleanToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncObjectBooleanToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectBooleanToDouble.this.new FuncObjectBooleanToDouble(a, b),
               (a, b) -> NodeFuncObjectBooleanToDouble.this.new FuncObjectBooleanToDouble(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectBooleanToDouble.this.new FuncObjectBooleanToDouble(a, b),
               (a, b) -> NodeConstantDouble.of(NodeFuncObjectBooleanToDouble.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectBooleanToDouble.this.canInline) {
            if (NodeFuncObjectBooleanToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectBooleanToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectBooleanToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectBooleanToDouble.this;
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
            NodeFuncObjectBooleanToDouble<A>.FuncObjectBooleanToDouble other = (NodeFuncObjectBooleanToDouble.FuncObjectBooleanToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectBooleanToDouble<A> {
      double apply(A var1, boolean var2);
   }
}
