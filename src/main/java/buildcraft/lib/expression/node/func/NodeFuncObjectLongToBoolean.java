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

public class NodeFuncObjectLongToBoolean<A> extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncObjectLongToBoolean.IFuncObjectLongToBoolean<A> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectLongToBoolean(String name, Class<A> argTypeA, NodeFuncObjectLongToBoolean.IFuncObjectLongToBoolean<A> function) {
      this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", long -> boolean ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncObjectLongToBoolean(Class<A> argTypeA, NodeFuncObjectLongToBoolean.IFuncObjectLongToBoolean<A> function, StringFunctionTri stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectLongToBoolean<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectLongToBoolean<A>.FuncObjectLongToBoolean create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB) {
      return new NodeFuncObjectLongToBoolean.FuncObjectLongToBoolean(argA, argB);
   }

   public class FuncObjectLongToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeLong argB;

      public FuncObjectLongToBoolean(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncObjectLongToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncObjectLongToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectLongToBoolean.this.new FuncObjectLongToBoolean(a, b),
               (a, b) -> NodeFuncObjectLongToBoolean.this.new FuncObjectLongToBoolean(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectLongToBoolean.this.new FuncObjectLongToBoolean(a, b),
               (a, b) -> NodeConstantBoolean.of(NodeFuncObjectLongToBoolean.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectLongToBoolean.this.canInline) {
            if (NodeFuncObjectLongToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectLongToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectLongToBoolean.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectLongToBoolean.this;
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
            NodeFuncObjectLongToBoolean<A>.FuncObjectLongToBoolean other = (NodeFuncObjectLongToBoolean.FuncObjectLongToBoolean)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectLongToBoolean<A> {
      boolean apply(A var1, long var2);
   }
}
