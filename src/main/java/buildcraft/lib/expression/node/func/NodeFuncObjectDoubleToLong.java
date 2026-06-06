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

public class NodeFuncObjectDoubleToLong<A> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectDoubleToLong.IFuncObjectDoubleToLong<A> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectDoubleToLong(String name, Class<A> argTypeA, NodeFuncObjectDoubleToLong.IFuncObjectDoubleToLong<A> function) {
      this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", double -> long ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncObjectDoubleToLong(Class<A> argTypeA, NodeFuncObjectDoubleToLong.IFuncObjectDoubleToLong<A> function, StringFunctionTri stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectDoubleToLong<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectDoubleToLong<A>.FuncObjectDoubleToLong create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB) {
      return new FuncObjectDoubleToLong(argA, argB);
   }

   public class FuncObjectDoubleToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;

      public FuncObjectDoubleToLong(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectDoubleToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectDoubleToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectDoubleToLong.this.new FuncObjectDoubleToLong(a, b),
               (a, b) -> NodeFuncObjectDoubleToLong.this.new FuncObjectDoubleToLong(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectDoubleToLong.this.new FuncObjectDoubleToLong(a, b),
               (a, b) -> NodeConstantLong.of(NodeFuncObjectDoubleToLong.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleToLong.this.canInline) {
            if (NodeFuncObjectDoubleToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleToLong.this;
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
            NodeFuncObjectDoubleToLong<A>.FuncObjectDoubleToLong other = (NodeFuncObjectDoubleToLong.FuncObjectDoubleToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleToLong<A> {
      long apply(A var1, double var2);
   }
}
