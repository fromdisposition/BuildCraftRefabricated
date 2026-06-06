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

public class NodeFuncObjectToLong<A> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectToLong.IFuncObjectToLong<A> function;
   private final StringFunctionBi stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectToLong(String name, Class<A> argTypeA, NodeFuncObjectToLong.IFuncObjectToLong<A> function) {
      this(argTypeA, function, a -> "[ " + NodeTypes.getName(argTypeA) + " -> long ] " + name + "(" + a + ")");
   }

   public NodeFuncObjectToLong(Class<A> argTypeA, NodeFuncObjectToLong.IFuncObjectToLong<A> function, StringFunctionBi stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncObjectToLong<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a);
   }

   public NodeFuncObjectToLong<A>.FuncObjectToLong create(IExpressionNode.INodeObject<A> argA) {
      return new FuncObjectToLong(argA);
   }

   public class FuncObjectToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;

      public FuncObjectToLong(IExpressionNode.INodeObject<A> argA) {
         this.argA = argA;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectToLong.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncObjectToLong.this.new FuncObjectToLong(a), a -> NodeFuncObjectToLong.this.new FuncObjectToLong(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncObjectToLong.this.new FuncObjectToLong(a),
               a -> NodeConstantLong.of(NodeFuncObjectToLong.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectToLong.this.canInline) {
            if (NodeFuncObjectToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncObjectToLong.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectToLong.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncObjectToLong<A>.FuncObjectToLong other = (NodeFuncObjectToLong.FuncObjectToLong)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectToLong<A> {
      long apply(A var1);
   }
}
