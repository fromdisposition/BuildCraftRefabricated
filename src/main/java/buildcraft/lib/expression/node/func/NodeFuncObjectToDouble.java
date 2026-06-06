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

public class NodeFuncObjectToDouble<A> extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncObjectToDouble.IFuncObjectToDouble<A> function;
   private final StringFunctionBi stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectToDouble(String name, Class<A> argTypeA, NodeFuncObjectToDouble.IFuncObjectToDouble<A> function) {
      this(argTypeA, function, a -> "[ " + NodeTypes.getName(argTypeA) + " -> double ] " + name + "(" + a + ")");
   }

   public NodeFuncObjectToDouble(Class<A> argTypeA, NodeFuncObjectToDouble.IFuncObjectToDouble<A> function, StringFunctionBi stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncObjectToDouble<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a);
   }

   public NodeFuncObjectToDouble<A>.FuncObjectToDouble create(IExpressionNode.INodeObject<A> argA) {
      return new NodeFuncObjectToDouble.FuncObjectToDouble(argA);
   }

   public class FuncObjectToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;

      public FuncObjectToDouble(IExpressionNode.INodeObject<A> argA) {
         this.argA = argA;
      }

      @Override
      public double evaluate() {
         return NodeFuncObjectToDouble.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncObjectToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncObjectToDouble.this.new FuncObjectToDouble(a), a -> NodeFuncObjectToDouble.this.new FuncObjectToDouble(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncObjectToDouble.this.new FuncObjectToDouble(a),
               a -> NodeConstantDouble.of(NodeFuncObjectToDouble.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectToDouble.this.canInline) {
            if (NodeFuncObjectToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncObjectToDouble.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectToDouble.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncObjectToDouble<A>.FuncObjectToDouble other = (NodeFuncObjectToDouble.FuncObjectToDouble)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectToDouble<A> {
      double apply(A var1);
   }
}
