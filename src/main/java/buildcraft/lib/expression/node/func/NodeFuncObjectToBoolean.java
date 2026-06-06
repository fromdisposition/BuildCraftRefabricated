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

public class NodeFuncObjectToBoolean<A> extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncObjectToBoolean.IFuncObjectToBoolean<A> function;
   private final StringFunctionBi stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectToBoolean(String name, Class<A> argTypeA, NodeFuncObjectToBoolean.IFuncObjectToBoolean<A> function) {
      this(argTypeA, function, a -> "[ " + NodeTypes.getName(argTypeA) + " -> boolean ] " + name + "(" + a + ")");
   }

   public NodeFuncObjectToBoolean(Class<A> argTypeA, NodeFuncObjectToBoolean.IFuncObjectToBoolean<A> function, StringFunctionBi stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncObjectToBoolean<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a);
   }

   public NodeFuncObjectToBoolean<A>.FuncObjectToBoolean create(IExpressionNode.INodeObject<A> argA) {
      return new NodeFuncObjectToBoolean.FuncObjectToBoolean(argA);
   }

   public class FuncObjectToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;

      public FuncObjectToBoolean(IExpressionNode.INodeObject<A> argA) {
         this.argA = argA;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncObjectToBoolean.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncObjectToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncObjectToBoolean.this.new FuncObjectToBoolean(a), a -> NodeFuncObjectToBoolean.this.new FuncObjectToBoolean(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncObjectToBoolean.this.new FuncObjectToBoolean(a),
               a -> NodeConstantBoolean.of(NodeFuncObjectToBoolean.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectToBoolean.this.canInline) {
            if (NodeFuncObjectToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncObjectToBoolean.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectToBoolean.this;
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
            NodeFuncObjectToBoolean<A>.FuncObjectToBoolean other = (NodeFuncObjectToBoolean.FuncObjectToBoolean)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectToBoolean<A> {
      boolean apply(A var1);
   }
}
