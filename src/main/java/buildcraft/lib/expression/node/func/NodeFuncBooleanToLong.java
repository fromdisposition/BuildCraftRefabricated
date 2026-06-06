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

public class NodeFuncBooleanToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncBooleanToLong.IFuncBooleanToLong function;
   private final StringFunctionBi stringFunction;

   public NodeFuncBooleanToLong(String name, NodeFuncBooleanToLong.IFuncBooleanToLong function) {
      this(function, a -> "[ boolean -> long ] " + name + "(" + a + ")");
   }

   public NodeFuncBooleanToLong(NodeFuncBooleanToLong.IFuncBooleanToLong function, StringFunctionBi stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}");
   }

   public NodeFuncBooleanToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a);
   }

   public NodeFuncBooleanToLong.FuncBooleanToLong create(IExpressionNode.INodeBoolean argA) {
      return new NodeFuncBooleanToLong.FuncBooleanToLong(argA);
   }

   public class FuncBooleanToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;

      public FuncBooleanToLong(IExpressionNode.INodeBoolean argA) {
         this.argA = argA;
      }

      @Override
      public long evaluate() {
         return NodeFuncBooleanToLong.this.function.apply(this.argA.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncBooleanToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this, this.argA, a -> NodeFuncBooleanToLong.this.new FuncBooleanToLong(a), a -> NodeFuncBooleanToLong.this.new FuncBooleanToLong(a)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               a -> NodeFuncBooleanToLong.this.new FuncBooleanToLong(a),
               a -> NodeConstantLong.of(NodeFuncBooleanToLong.this.function.apply(a.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanToLong.this.canInline) {
            if (NodeFuncBooleanToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanToLong.this.stringFunction.apply(this.argA.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanToLong.this;
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
            NodeFuncBooleanToLong.FuncBooleanToLong other = (NodeFuncBooleanToLong.FuncBooleanToLong)obj;
            return Objects.equals(this.argA, other.argA);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanToLong {
      long apply(boolean var1);
   }
}
