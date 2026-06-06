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

public class NodeFuncObjectDoubleDoubleToDouble<A> extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncObjectDoubleDoubleToDouble.IFuncObjectDoubleDoubleToDouble<A> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectDoubleDoubleToDouble(String name, Class<A> argTypeA, NodeFuncObjectDoubleDoubleToDouble.IFuncObjectDoubleDoubleToDouble<A> function) {
      this(argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", double, double -> double ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncObjectDoubleDoubleToDouble(
      Class<A> argTypeA, NodeFuncObjectDoubleDoubleToDouble.IFuncObjectDoubleDoubleToDouble<A> function, StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectDoubleDoubleToDouble<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectDoubleDoubleToDouble<A>.FuncObjectDoubleDoubleToDouble create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC
   ) {
      return new FuncObjectDoubleDoubleToDouble(argA, argB, argC);
   }

   public class FuncObjectDoubleDoubleToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;

      public FuncObjectDoubleDoubleToDouble(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public double evaluate() {
         return NodeFuncObjectDoubleDoubleToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncObjectDoubleDoubleToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleDoubleToDouble.this.new FuncObjectDoubleDoubleToDouble(a, b, c),
               (a, b, c) -> NodeFuncObjectDoubleDoubleToDouble.this.new FuncObjectDoubleDoubleToDouble(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleDoubleToDouble.this.new FuncObjectDoubleDoubleToDouble(a, b, c),
               (a, b, c) -> NodeConstantDouble.of(NodeFuncObjectDoubleDoubleToDouble.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleDoubleToDouble.this.canInline) {
            if (NodeFuncObjectDoubleDoubleToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleDoubleToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleDoubleToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleDoubleToDouble.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncObjectDoubleDoubleToDouble<A>.FuncObjectDoubleDoubleToDouble other = (NodeFuncObjectDoubleDoubleToDouble.FuncObjectDoubleDoubleToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleDoubleToDouble<A> {
      double apply(A var1, double var2, double var4);
   }
}
