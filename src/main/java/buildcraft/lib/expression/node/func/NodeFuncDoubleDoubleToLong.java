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

public class NodeFuncDoubleDoubleToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncDoubleDoubleToLong.IFuncDoubleDoubleToLong function;
   private final StringFunctionTri stringFunction;

   public NodeFuncDoubleDoubleToLong(String name, NodeFuncDoubleDoubleToLong.IFuncDoubleDoubleToLong function) {
      this(function, (a, b) -> "[ double, double -> long ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncDoubleDoubleToLong(NodeFuncDoubleDoubleToLong.IFuncDoubleDoubleToLong function, StringFunctionTri stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncDoubleDoubleToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b);
   }

   public NodeFuncDoubleDoubleToLong.FuncDoubleDoubleToLong create(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB) {
      return new NodeFuncDoubleDoubleToLong.FuncDoubleDoubleToLong(argA, argB);
   }

   public class FuncDoubleDoubleToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;

      public FuncDoubleDoubleToLong(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public long evaluate() {
         return NodeFuncDoubleDoubleToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncDoubleDoubleToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncDoubleDoubleToLong.this.new FuncDoubleDoubleToLong(a, b),
               (a, b) -> NodeFuncDoubleDoubleToLong.this.new FuncDoubleDoubleToLong(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncDoubleDoubleToLong.this.new FuncDoubleDoubleToLong(a, b),
               (a, b) -> NodeConstantLong.of(NodeFuncDoubleDoubleToLong.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleToLong.this.canInline) {
            if (NodeFuncDoubleDoubleToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleToLong.this;
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
            NodeFuncDoubleDoubleToLong.FuncDoubleDoubleToLong other = (NodeFuncDoubleDoubleToLong.FuncDoubleDoubleToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleDoubleToLong {
      long apply(double var1, double var3);
   }
}
