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

public class NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, D> extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, C, D> function;
   private final StringFunctionPenta stringFunction;
   private final Class<A> argTypeA;
   private final Class<B> argTypeB;
   private final Class<C> argTypeC;
   private final Class<D> argTypeD;

   public NodeFuncObjectObjectObjectObjectToBoolean(
      String name,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, C, D> function
   ) {
      this(
         argTypeA,
         argTypeB,
         argTypeC,
         argTypeD,
         function,
         (a, b, c, d) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", "
            + NodeTypes.getName(argTypeB)
            + ", "
            + NodeTypes.getName(argTypeC)
            + ", "
            + NodeTypes.getName(argTypeD)
            + " -> boolean ] "
            + name
            + "("
            + a
            + ", "
            + b
            + ", "
            + c
            + ", "
            + d
            + ")"
      );
   }

   public NodeFuncObjectObjectObjectObjectToBoolean(
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, C, D> function,
      StringFunctionPenta stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.argTypeB = argTypeB;
      this.argTypeC = argTypeC;
      this.argTypeD = argTypeD;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, D> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<D> d = stack.popObject(this.argTypeD);
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeObject<B> b = stack.popObject(this.argTypeB);
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c, d);
   }

   public NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, D>.FuncObjectObjectObjectObjectToBoolean create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC, IExpressionNode.INodeObject<D> argD
   ) {
      return new NodeFuncObjectObjectObjectObjectToBoolean.FuncObjectObjectObjectObjectToBoolean(argA, argB, argC, argD);
   }

   public class FuncObjectObjectObjectObjectToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeObject<B> argB;
      public final IExpressionNode.INodeObject<C> argC;
      public final IExpressionNode.INodeObject<D> argD;

      public FuncObjectObjectObjectObjectToBoolean(
         IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC, IExpressionNode.INodeObject<D> argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncObjectObjectObjectObjectToBoolean.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncObjectObjectObjectObjectToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToBoolean.this.new FuncObjectObjectObjectObjectToBoolean(a, b, c, d),
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToBoolean.this.new FuncObjectObjectObjectObjectToBoolean(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToBoolean.this.new FuncObjectObjectObjectObjectToBoolean(a, b, c, d),
               (a, b, c, d) -> NodeConstantBoolean.of(
                  NodeFuncObjectObjectObjectObjectToBoolean.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectObjectObjectObjectToBoolean.this.canInline) {
            if (NodeFuncObjectObjectObjectObjectToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectObjectObjectObjectToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncObjectObjectObjectObjectToBoolean.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectObjectObjectObjectToBoolean.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, D>.FuncObjectObjectObjectObjectToBoolean other = (NodeFuncObjectObjectObjectObjectToBoolean.FuncObjectObjectObjectObjectToBoolean)obj;
            return Objects.equals(this.argA, other.argA)
               && Objects.equals(this.argB, other.argB)
               && Objects.equals(this.argC, other.argC)
               && Objects.equals(this.argD, other.argD);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectObjectObjectObjectToBoolean<A, B, C, D> {
      boolean apply(A var1, B var2, C var3, D var4);
   }
}
