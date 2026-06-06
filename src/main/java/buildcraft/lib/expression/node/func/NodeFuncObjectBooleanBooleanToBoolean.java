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

public class NodeFuncObjectBooleanBooleanToBoolean<A> extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncObjectBooleanBooleanToBoolean.IFuncObjectBooleanBooleanToBoolean<A> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectBooleanBooleanToBoolean(
      String name, Class<A> argTypeA, NodeFuncObjectBooleanBooleanToBoolean.IFuncObjectBooleanBooleanToBoolean<A> function
   ) {
      this(
         argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", boolean, boolean -> boolean ] " + name + "(" + a + ", " + b + ", " + c + ")"
      );
   }

   public NodeFuncObjectBooleanBooleanToBoolean(
      Class<A> argTypeA, NodeFuncObjectBooleanBooleanToBoolean.IFuncObjectBooleanBooleanToBoolean<A> function, StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectBooleanBooleanToBoolean<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectBooleanBooleanToBoolean<A>.FuncObjectBooleanBooleanToBoolean create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC
   ) {
      return new NodeFuncObjectBooleanBooleanToBoolean.FuncObjectBooleanBooleanToBoolean(argA, argB, argC);
   }

   public class FuncObjectBooleanBooleanToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;

      public FuncObjectBooleanBooleanToBoolean(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncObjectBooleanBooleanToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncObjectBooleanBooleanToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectBooleanBooleanToBoolean.this.new FuncObjectBooleanBooleanToBoolean(a, b, c),
               (a, b, c) -> NodeFuncObjectBooleanBooleanToBoolean.this.new FuncObjectBooleanBooleanToBoolean(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectBooleanBooleanToBoolean.this.new FuncObjectBooleanBooleanToBoolean(a, b, c),
               (a, b, c) -> NodeConstantBoolean.of(NodeFuncObjectBooleanBooleanToBoolean.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectBooleanBooleanToBoolean.this.canInline) {
            if (NodeFuncObjectBooleanBooleanToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectBooleanBooleanToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectBooleanBooleanToBoolean.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectBooleanBooleanToBoolean.this;
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
            NodeFuncObjectBooleanBooleanToBoolean<A>.FuncObjectBooleanBooleanToBoolean other = (NodeFuncObjectBooleanBooleanToBoolean.FuncObjectBooleanBooleanToBoolean)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectBooleanBooleanToBoolean<A> {
      boolean apply(A var1, boolean var2, boolean var3);
   }
}
