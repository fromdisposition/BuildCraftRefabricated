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

public class NodeFuncObjectDoubleDoubleToBoolean<A> extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncObjectDoubleDoubleToBoolean.IFuncObjectDoubleDoubleToBoolean<A> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectDoubleDoubleToBoolean(String name, Class<A> argTypeA, NodeFuncObjectDoubleDoubleToBoolean.IFuncObjectDoubleDoubleToBoolean<A> function) {
      this(argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", double, double -> boolean ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncObjectDoubleDoubleToBoolean(
      Class<A> argTypeA, NodeFuncObjectDoubleDoubleToBoolean.IFuncObjectDoubleDoubleToBoolean<A> function, StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectDoubleDoubleToBoolean<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectDoubleDoubleToBoolean<A>.FuncObjectDoubleDoubleToBoolean create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC
   ) {
      return new NodeFuncObjectDoubleDoubleToBoolean.FuncObjectDoubleDoubleToBoolean(argA, argB, argC);
   }

   public class FuncObjectDoubleDoubleToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;

      public FuncObjectDoubleDoubleToBoolean(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncObjectDoubleDoubleToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncObjectDoubleDoubleToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleDoubleToBoolean.this.new FuncObjectDoubleDoubleToBoolean(a, b, c),
               (a, b, c) -> NodeFuncObjectDoubleDoubleToBoolean.this.new FuncObjectDoubleDoubleToBoolean(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleDoubleToBoolean.this.new FuncObjectDoubleDoubleToBoolean(a, b, c),
               (a, b, c) -> NodeConstantBoolean.of(NodeFuncObjectDoubleDoubleToBoolean.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleDoubleToBoolean.this.canInline) {
            if (NodeFuncObjectDoubleDoubleToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleDoubleToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleDoubleToBoolean.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleDoubleToBoolean.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncObjectDoubleDoubleToBoolean<A>.FuncObjectDoubleDoubleToBoolean other = (NodeFuncObjectDoubleDoubleToBoolean.FuncObjectDoubleDoubleToBoolean)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleDoubleToBoolean<A> {
      boolean apply(A var1, double var2, double var4);
   }
}
