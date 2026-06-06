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

public class NodeFuncObjectObjectObjectToDouble<A, B, C> extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, B, C> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<B> argTypeB;
   private final Class<C> argTypeC;

   public NodeFuncObjectObjectObjectToDouble(
      String name,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, B, C> function
   ) {
      this(
         argTypeA,
         argTypeB,
         argTypeC,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", "
            + NodeTypes.getName(argTypeB)
            + ", "
            + NodeTypes.getName(argTypeC)
            + " -> double ] "
            + name
            + "("
            + a
            + ", "
            + b
            + ", "
            + c
            + ")"
      );
   }

   public NodeFuncObjectObjectObjectToDouble(
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, B, C> function,
      StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.argTypeB = argTypeB;
      this.argTypeC = argTypeC;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectObjectObjectToDouble<A, B, C> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeObject<B> b = stack.popObject(this.argTypeB);
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectObjectObjectToDouble<A, B, C>.FuncObjectObjectObjectToDouble create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC
   ) {
      return new FuncObjectObjectObjectToDouble(argA, argB, argC);
   }

   public class FuncObjectObjectObjectToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeObject<B> argB;
      public final IExpressionNode.INodeObject<C> argC;

      public FuncObjectObjectObjectToDouble(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public double evaluate() {
         return NodeFuncObjectObjectObjectToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncObjectObjectObjectToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectObjectObjectToDouble.this.new FuncObjectObjectObjectToDouble(a, b, c),
               (a, b, c) -> NodeFuncObjectObjectObjectToDouble.this.new FuncObjectObjectObjectToDouble(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectObjectObjectToDouble.this.new FuncObjectObjectObjectToDouble(a, b, c),
               (a, b, c) -> NodeConstantDouble.of(NodeFuncObjectObjectObjectToDouble.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectObjectObjectToDouble.this.canInline) {
            if (NodeFuncObjectObjectObjectToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectObjectObjectToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectObjectObjectToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectObjectObjectToDouble.this;
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
            NodeFuncObjectObjectObjectToDouble<A, B, C>.FuncObjectObjectObjectToDouble other = (NodeFuncObjectObjectObjectToDouble.FuncObjectObjectObjectToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectObjectObjectToDouble<A, B, C> {
      double apply(A var1, B var2, C var3);
   }
}
