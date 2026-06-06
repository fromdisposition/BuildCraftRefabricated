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

public class NodeFuncDoubleDoubleDoubleDoubleToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncDoubleDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleDoubleToLong function;
   private final StringFunctionPenta stringFunction;

   public NodeFuncDoubleDoubleDoubleDoubleToLong(String name, NodeFuncDoubleDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleDoubleToLong function) {
      this(function, (a, b, c, d) -> "[ double, double, double, double -> long ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")");
   }

   public NodeFuncDoubleDoubleDoubleDoubleToLong(
      NodeFuncDoubleDoubleDoubleDoubleToLong.IFuncDoubleDoubleDoubleDoubleToLong function, StringFunctionPenta stringFunction
   ) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncDoubleDoubleDoubleDoubleToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble d = stack.popDouble();
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b, c, d);
   }

   public NodeFuncDoubleDoubleDoubleDoubleToLong.FuncDoubleDoubleDoubleDoubleToLong create(
      IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC, IExpressionNode.INodeDouble argD
   ) {
      return new FuncDoubleDoubleDoubleDoubleToLong(argA, argB, argC, argD);
   }

   public class FuncDoubleDoubleDoubleDoubleToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;
      public final IExpressionNode.INodeDouble argD;

      public FuncDoubleDoubleDoubleDoubleToLong(
         IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC, IExpressionNode.INodeDouble argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public long evaluate() {
         return NodeFuncDoubleDoubleDoubleDoubleToLong.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncDoubleDoubleDoubleDoubleToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToLong.this.new FuncDoubleDoubleDoubleDoubleToLong(a, b, c, d),
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToLong.this.new FuncDoubleDoubleDoubleDoubleToLong(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToLong.this.new FuncDoubleDoubleDoubleDoubleToLong(a, b, c, d),
               (a, b, c, d) -> NodeConstantLong.of(
                  NodeFuncDoubleDoubleDoubleDoubleToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleDoubleDoubleToLong.this.canInline) {
            if (NodeFuncDoubleDoubleDoubleDoubleToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleDoubleDoubleToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleDoubleDoubleToLong.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleDoubleDoubleToLong.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncDoubleDoubleDoubleDoubleToLong.FuncDoubleDoubleDoubleDoubleToLong other = (NodeFuncDoubleDoubleDoubleDoubleToLong.FuncDoubleDoubleDoubleDoubleToLong)obj;
            return Objects.equals(this.argA, other.argA)
               && Objects.equals(this.argB, other.argB)
               && Objects.equals(this.argC, other.argC)
               && Objects.equals(this.argD, other.argD);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleDoubleDoubleDoubleToLong {
      long apply(double var1, double var3, double var5, double var7);
   }
}
