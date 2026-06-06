package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import java.util.Objects;

public class NodeFuncObjectDoubleToBoolean<A> extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncObjectDoubleToBoolean.IFuncObjectDoubleToBoolean<A> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectDoubleToBoolean(String name, Class<A> argTypeA, NodeFuncObjectDoubleToBoolean.IFuncObjectDoubleToBoolean<A> function) {
      this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", double -> boolean ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncObjectDoubleToBoolean(
      Class<A> argTypeA, NodeFuncObjectDoubleToBoolean.IFuncObjectDoubleToBoolean<A> function, StringFunctionTri stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectDoubleToBoolean<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectDoubleToBoolean<A>.FuncObjectDoubleToBoolean create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB) {
      return new FuncObjectDoubleToBoolean(argA, argB);
   }

   public class FuncObjectDoubleToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;

      public FuncObjectDoubleToBoolean(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncObjectDoubleToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncObjectDoubleToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectDoubleToBoolean.this.new FuncObjectDoubleToBoolean(a, b),
               (a, b) -> NodeFuncObjectDoubleToBoolean.this.new FuncObjectDoubleToBoolean(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectDoubleToBoolean.this.new FuncObjectDoubleToBoolean(a, b),
               (a, b) -> NodeConstantBoolean.of(NodeFuncObjectDoubleToBoolean.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleToBoolean.this.canInline) {
            if (NodeFuncObjectDoubleToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleToBoolean.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleToBoolean.this;
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
            NodeFuncObjectDoubleToBoolean<A>.FuncObjectDoubleToBoolean other = (NodeFuncObjectDoubleToBoolean.FuncObjectDoubleToBoolean)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleToBoolean<A> {
      boolean apply(A var1, double var2);
   }
}
