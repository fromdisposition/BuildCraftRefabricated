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

public class NodeFuncBooleanBooleanBooleanToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanToLong function;
   private final StringFunctionQuad stringFunction;

   public NodeFuncBooleanBooleanBooleanToLong(String name, NodeFuncBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanToLong function) {
      this(function, (a, b, c) -> "[ boolean, boolean, boolean -> long ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncBooleanBooleanBooleanToLong(NodeFuncBooleanBooleanBooleanToLong.IFuncBooleanBooleanBooleanToLong function, StringFunctionQuad stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncBooleanBooleanBooleanToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b, c);
   }

   public NodeFuncBooleanBooleanBooleanToLong.FuncBooleanBooleanBooleanToLong create(
      IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC
   ) {
      return new NodeFuncBooleanBooleanBooleanToLong.FuncBooleanBooleanBooleanToLong(argA, argB, argC);
   }

   public class FuncBooleanBooleanBooleanToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;

      public FuncBooleanBooleanBooleanToLong(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public long evaluate() {
         return NodeFuncBooleanBooleanBooleanToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncBooleanBooleanBooleanToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToLong.this.new FuncBooleanBooleanBooleanToLong(a, b, c),
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToLong.this.new FuncBooleanBooleanBooleanToLong(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToLong.this.new FuncBooleanBooleanBooleanToLong(a, b, c),
               (a, b, c) -> NodeConstantLong.of(NodeFuncBooleanBooleanBooleanToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanBooleanToLong.this.canInline) {
            if (NodeFuncBooleanBooleanBooleanToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanBooleanToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanBooleanToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanBooleanToLong.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncBooleanBooleanBooleanToLong.FuncBooleanBooleanBooleanToLong other = (NodeFuncBooleanBooleanBooleanToLong.FuncBooleanBooleanBooleanToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanBooleanToLong {
      long apply(boolean var1, boolean var2, boolean var3);
   }
}
