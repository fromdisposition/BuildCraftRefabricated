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

public class NodeFuncObjectLongToLong<A> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectLongToLong.IFuncObjectLongToLong<A> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectLongToLong(String name, Class<A> argTypeA, NodeFuncObjectLongToLong.IFuncObjectLongToLong<A> function) {
      this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", long -> long ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncObjectLongToLong(Class<A> argTypeA, NodeFuncObjectLongToLong.IFuncObjectLongToLong<A> function, StringFunctionTri stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectLongToLong<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectLongToLong<A>.FuncObjectLongToLong create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB) {
      return new NodeFuncObjectLongToLong.FuncObjectLongToLong(argA, argB);
   }

   public class FuncObjectLongToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeLong argB;

      public FuncObjectLongToLong(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectLongToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectLongToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectLongToLong.this.new FuncObjectLongToLong(a, b),
               (a, b) -> NodeFuncObjectLongToLong.this.new FuncObjectLongToLong(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectLongToLong.this.new FuncObjectLongToLong(a, b),
               (a, b) -> NodeConstantLong.of(NodeFuncObjectLongToLong.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectLongToLong.this.canInline) {
            if (NodeFuncObjectLongToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectLongToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectLongToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectLongToLong.this;
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
            NodeFuncObjectLongToLong<A>.FuncObjectLongToLong other = (NodeFuncObjectLongToLong.FuncObjectLongToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectLongToLong<A> {
      long apply(A var1, long var2);
   }
}
