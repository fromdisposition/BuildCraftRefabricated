package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import java.util.Objects;

public class NodeFuncObjectObjectObjectObjectToLong<A, B, C, D> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, C, D> function;
   private final StringFunctionPenta stringFunction;
   private final Class<A> argTypeA;
   private final Class<B> argTypeB;
   private final Class<C> argTypeC;
   private final Class<D> argTypeD;

   public NodeFuncObjectObjectObjectObjectToLong(
      String name,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, C, D> function
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
            + " -> long ] "
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

   public NodeFuncObjectObjectObjectObjectToLong(
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, C, D> function,
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

   public NodeFuncObjectObjectObjectObjectToLong<A, B, C, D> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<D> d = stack.popObject(this.argTypeD);
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeObject<B> b = stack.popObject(this.argTypeB);
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c, d);
   }

   public NodeFuncObjectObjectObjectObjectToLong<A, B, C, D>.FuncObjectObjectObjectObjectToLong create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC, IExpressionNode.INodeObject<D> argD
   ) {
      return new NodeFuncObjectObjectObjectObjectToLong.FuncObjectObjectObjectObjectToLong(argA, argB, argC, argD);
   }

   public class FuncObjectObjectObjectObjectToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeObject<B> argB;
      public final IExpressionNode.INodeObject<C> argC;
      public final IExpressionNode.INodeObject<D> argD;

      public FuncObjectObjectObjectObjectToLong(
         IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeObject<B> argB, IExpressionNode.INodeObject<C> argC, IExpressionNode.INodeObject<D> argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectObjectObjectObjectToLong.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectObjectObjectObjectToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToLong.this.new FuncObjectObjectObjectObjectToLong(a, b, c, d),
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToLong.this.new FuncObjectObjectObjectObjectToLong(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncObjectObjectObjectObjectToLong.this.new FuncObjectObjectObjectObjectToLong(a, b, c, d),
               (a, b, c, d) -> NodeConstantLong.of(
                  NodeFuncObjectObjectObjectObjectToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectObjectObjectObjectToLong.this.canInline) {
            if (NodeFuncObjectObjectObjectObjectToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectObjectObjectObjectToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncObjectObjectObjectObjectToLong.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectObjectObjectObjectToLong.this;
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
            NodeFuncObjectObjectObjectObjectToLong<A, B, C, D>.FuncObjectObjectObjectObjectToLong other = (NodeFuncObjectObjectObjectObjectToLong.FuncObjectObjectObjectObjectToLong)obj;
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
   public interface IFuncObjectObjectObjectObjectToLong<A, B, C, D> {
      long apply(A var1, B var2, C var3, D var4);
   }
}
