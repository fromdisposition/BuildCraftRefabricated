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

public class NodeFuncBooleanBooleanToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncBooleanBooleanToLong.IFuncBooleanBooleanToLong function;
   private final StringFunctionTri stringFunction;

   public NodeFuncBooleanBooleanToLong(String name, NodeFuncBooleanBooleanToLong.IFuncBooleanBooleanToLong function) {
      this(function, (a, b) -> "[ boolean, boolean -> long ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncBooleanBooleanToLong(NodeFuncBooleanBooleanToLong.IFuncBooleanBooleanToLong function, StringFunctionTri stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncBooleanBooleanToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b);
   }

   public NodeFuncBooleanBooleanToLong.FuncBooleanBooleanToLong create(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB) {
      return new NodeFuncBooleanBooleanToLong.FuncBooleanBooleanToLong(argA, argB);
   }

   public class FuncBooleanBooleanToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;

      public FuncBooleanBooleanToLong(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public long evaluate() {
         return NodeFuncBooleanBooleanToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncBooleanBooleanToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncBooleanBooleanToLong.this.new FuncBooleanBooleanToLong(a, b),
               (a, b) -> NodeFuncBooleanBooleanToLong.this.new FuncBooleanBooleanToLong(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncBooleanBooleanToLong.this.new FuncBooleanBooleanToLong(a, b),
               (a, b) -> NodeConstantLong.of(NodeFuncBooleanBooleanToLong.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanToLong.this.canInline) {
            if (NodeFuncBooleanBooleanToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanToLong.this;
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
            NodeFuncBooleanBooleanToLong.FuncBooleanBooleanToLong other = (NodeFuncBooleanBooleanToLong.FuncBooleanBooleanToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanToLong {
      long apply(boolean var1, boolean var2);
   }
}
