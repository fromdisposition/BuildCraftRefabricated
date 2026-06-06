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

public class NodeFuncObjectDoubleToDouble<A> extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncObjectDoubleToDouble.IFuncObjectDoubleToDouble<A> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectDoubleToDouble(String name, Class<A> argTypeA, NodeFuncObjectDoubleToDouble.IFuncObjectDoubleToDouble<A> function) {
      this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", double -> double ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncObjectDoubleToDouble(Class<A> argTypeA, NodeFuncObjectDoubleToDouble.IFuncObjectDoubleToDouble<A> function, StringFunctionTri stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectDoubleToDouble<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectDoubleToDouble<A>.FuncObjectDoubleToDouble create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB) {
      return new NodeFuncObjectDoubleToDouble.FuncObjectDoubleToDouble(argA, argB);
   }

   public class FuncObjectDoubleToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;

      public FuncObjectDoubleToDouble(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public double evaluate() {
         return NodeFuncObjectDoubleToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncObjectDoubleToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectDoubleToDouble.this.new FuncObjectDoubleToDouble(a, b),
               (a, b) -> NodeFuncObjectDoubleToDouble.this.new FuncObjectDoubleToDouble(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectDoubleToDouble.this.new FuncObjectDoubleToDouble(a, b),
               (a, b) -> NodeConstantDouble.of(NodeFuncObjectDoubleToDouble.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleToDouble.this.canInline) {
            if (NodeFuncObjectDoubleToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleToDouble.this;
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
            NodeFuncObjectDoubleToDouble<A>.FuncObjectDoubleToDouble other = (NodeFuncObjectDoubleToDouble.FuncObjectDoubleToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleToDouble<A> {
      double apply(A var1, double var2);
   }
}
