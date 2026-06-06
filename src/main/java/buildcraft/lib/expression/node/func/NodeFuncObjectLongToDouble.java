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

public class NodeFuncObjectLongToDouble<A> extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncObjectLongToDouble.IFuncObjectLongToDouble<A> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectLongToDouble(String name, Class<A> argTypeA, NodeFuncObjectLongToDouble.IFuncObjectLongToDouble<A> function) {
      this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", long -> double ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncObjectLongToDouble(Class<A> argTypeA, NodeFuncObjectLongToDouble.IFuncObjectLongToDouble<A> function, StringFunctionTri stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectLongToDouble<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectLongToDouble<A>.FuncObjectLongToDouble create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB) {
      return new NodeFuncObjectLongToDouble.FuncObjectLongToDouble(argA, argB);
   }

   public class FuncObjectLongToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeLong argB;

      public FuncObjectLongToDouble(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public double evaluate() {
         return NodeFuncObjectLongToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncObjectLongToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectLongToDouble.this.new FuncObjectLongToDouble(a, b),
               (a, b) -> NodeFuncObjectLongToDouble.this.new FuncObjectLongToDouble(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectLongToDouble.this.new FuncObjectLongToDouble(a, b),
               (a, b) -> NodeConstantDouble.of(NodeFuncObjectLongToDouble.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectLongToDouble.this.canInline) {
            if (NodeFuncObjectLongToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectLongToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectLongToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectLongToDouble.this;
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
            NodeFuncObjectLongToDouble<A>.FuncObjectLongToDouble other = (NodeFuncObjectLongToDouble.FuncObjectLongToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectLongToDouble<A> {
      double apply(A var1, long var2);
   }
}
