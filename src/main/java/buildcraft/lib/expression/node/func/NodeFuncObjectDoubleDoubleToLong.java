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

public class NodeFuncObjectDoubleDoubleToLong<A> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectDoubleDoubleToLong.IFuncObjectDoubleDoubleToLong<A> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectDoubleDoubleToLong(String name, Class<A> argTypeA, NodeFuncObjectDoubleDoubleToLong.IFuncObjectDoubleDoubleToLong<A> function) {
      this(argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", double, double -> long ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncObjectDoubleDoubleToLong(
      Class<A> argTypeA, NodeFuncObjectDoubleDoubleToLong.IFuncObjectDoubleDoubleToLong<A> function, StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectDoubleDoubleToLong<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectDoubleDoubleToLong<A>.FuncObjectDoubleDoubleToLong create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC
   ) {
      return new FuncObjectDoubleDoubleToLong(argA, argB, argC);
   }

   public class FuncObjectDoubleDoubleToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;

      public FuncObjectDoubleDoubleToLong(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectDoubleDoubleToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectDoubleDoubleToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleDoubleToLong.this.new FuncObjectDoubleDoubleToLong(a, b, c),
               (a, b, c) -> NodeFuncObjectDoubleDoubleToLong.this.new FuncObjectDoubleDoubleToLong(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleDoubleToLong.this.new FuncObjectDoubleDoubleToLong(a, b, c),
               (a, b, c) -> NodeConstantLong.of(NodeFuncObjectDoubleDoubleToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleDoubleToLong.this.canInline) {
            if (NodeFuncObjectDoubleDoubleToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleDoubleToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleDoubleToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleDoubleToLong.this;
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
            NodeFuncObjectDoubleDoubleToLong<A>.FuncObjectDoubleDoubleToLong other = (NodeFuncObjectDoubleDoubleToLong.FuncObjectDoubleDoubleToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleDoubleToLong<A> {
      long apply(A var1, double var2, double var4);
   }
}
