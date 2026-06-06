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

public class NodeFuncDoubleToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncDoubleToLong.IFuncDoubleToLong function;
   private final StringFunctionBi stringFunction;

   public NodeFuncDoubleToLong(String name, NodeFuncDoubleToLong.IFuncDoubleToLong function) {
      this(function, a -> "[ double -> long ] " + name + "(" + a + ")");
   }

   public NodeFuncDoubleToLong(NodeFuncDoubleToLong.IFuncDoubleToLong function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncDoubleToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a);
   }

   public NodeFuncDoubleToLong.FuncDoubleToLong create(IExpressionNode.INodeDouble argA) {
      return new NodeFuncDoubleToLong.FuncDoubleToLong(argA);
   }

   public class FuncDoubleToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;

      public FuncDoubleToLong(IExpressionNode.INodeDouble argA) {
         this.argA = argA;
      }

      @Override
      public long evaluate() {
         return NodeFuncDoubleToLong.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncDoubleToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncDoubleToLong.this.new FuncDoubleToLong(a), a -> NodeFuncDoubleToLong.this.new FuncDoubleToLong(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncDoubleToLong.this.new FuncDoubleToLong(a),
               a -> NodeConstantLong.of(NodeFuncDoubleToLong.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleToLong.this.canInline) {
            if (NodeFuncDoubleToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleToLong.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleToLong.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncDoubleToLong.FuncDoubleToLong other = (NodeFuncDoubleToLong.FuncDoubleToLong)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleToLong {
      long apply(double var1);
   }
}
