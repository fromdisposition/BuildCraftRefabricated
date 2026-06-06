package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import java.util.Objects;

public class NodeFuncLongToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncLongToLong.IFuncLongToLong function;
   private final StringFunctionBi stringFunction;

   public NodeFuncLongToLong(String name, NodeFuncLongToLong.IFuncLongToLong function) {
      this(function, a -> "[ long -> long ] " + name + "(" + a + ")");
   }

   public NodeFuncLongToLong(NodeFuncLongToLong.IFuncLongToLong function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncLongToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a);
   }

   public NodeFuncLongToLong.FuncLongToLong create(IExpressionNode.INodeLong argA) {
      return new FuncLongToLong(argA);
   }

   public class FuncLongToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;

      public FuncLongToLong(IExpressionNode.INodeLong argA) {
         this.argA = argA;
      }

      @Override
      public long evaluate() {
         return NodeFuncLongToLong.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncLongToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncLongToLong.this.new FuncLongToLong(a), a -> NodeFuncLongToLong.this.new FuncLongToLong(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncLongToLong.this.new FuncLongToLong(a),
               a -> NodeConstantLong.of(NodeFuncLongToLong.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongToLong.this.canInline) {
            if (NodeFuncLongToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncLongToLong.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongToLong.this;
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
            NodeFuncLongToLong.FuncLongToLong other = (NodeFuncLongToLong.FuncLongToLong)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncLongToLong {
      long apply(long var1);
   }
}
