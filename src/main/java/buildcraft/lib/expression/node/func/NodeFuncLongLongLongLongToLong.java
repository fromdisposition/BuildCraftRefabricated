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

public class NodeFuncLongLongLongLongToLong extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncLongLongLongLongToLong.IFuncLongLongLongLongToLong function;
   private final StringFunctionPenta stringFunction;

   public NodeFuncLongLongLongLongToLong(String name, NodeFuncLongLongLongLongToLong.IFuncLongLongLongLongToLong function) {
      this(function, (a, b, c, d) -> "[ long, long, long, long -> long ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")");
   }

   public NodeFuncLongLongLongLongToLong(NodeFuncLongLongLongLongToLong.IFuncLongLongLongLongToLong function, StringFunctionPenta stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncLongLongLongLongToLong setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong d = stack.popLong();
      IExpressionNode.INodeLong c = stack.popLong();
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a, b, c, d);
   }

   public NodeFuncLongLongLongLongToLong.FuncLongLongLongLongToLong create(
      IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeLong argC, IExpressionNode.INodeLong argD
   ) {
      return new FuncLongLongLongLongToLong(argA, argB, argC, argD);
   }

   public class FuncLongLongLongLongToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;
      public final IExpressionNode.INodeLong argB;
      public final IExpressionNode.INodeLong argC;
      public final IExpressionNode.INodeLong argD;

      public FuncLongLongLongLongToLong(
         IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeLong argC, IExpressionNode.INodeLong argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public long evaluate() {
         return NodeFuncLongLongLongLongToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncLongLongLongLongToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncLongLongLongLongToLong.this.new FuncLongLongLongLongToLong(a, b, c, d),
               (a, b, c, d) -> NodeFuncLongLongLongLongToLong.this.new FuncLongLongLongLongToLong(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncLongLongLongLongToLong.this.new FuncLongLongLongLongToLong(a, b, c, d),
               (a, b, c, d) -> NodeConstantLong.of(NodeFuncLongLongLongLongToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongLongLongLongToLong.this.canInline) {
            if (NodeFuncLongLongLongLongToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongLongLongLongToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncLongLongLongLongToLong.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongLongLongLongToLong.this;
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
            NodeFuncLongLongLongLongToLong.FuncLongLongLongLongToLong other = (NodeFuncLongLongLongLongToLong.FuncLongLongLongLongToLong)obj;
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
   public interface IFuncLongLongLongLongToLong {
      long apply(long var1, long var3, long var5, long var7);
   }
}
