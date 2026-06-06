package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import java.util.Objects;

public class NodeFuncLongLongLongToDouble extends NodeFuncBase implements INodeFunc.INodeFuncDouble {
   public final NodeFuncLongLongLongToDouble.IFuncLongLongLongToDouble function;
   private final StringFunctionQuad stringFunction;

   public NodeFuncLongLongLongToDouble(String name, NodeFuncLongLongLongToDouble.IFuncLongLongLongToDouble function) {
      this(function, (a, b, c) -> "[ long, long, long -> double ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncLongLongLongToDouble(NodeFuncLongLongLongToDouble.IFuncLongLongLongToDouble function, StringFunctionQuad stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncLongLongLongToDouble setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong c = stack.popLong();
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a, b, c);
   }

   public NodeFuncLongLongLongToDouble.FuncLongLongLongToDouble create(
      IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeLong argC
   ) {
      return new FuncLongLongLongToDouble(argA, argB, argC);
   }

   public class FuncLongLongLongToDouble implements IExpressionNode.INodeDouble, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;
      public final IExpressionNode.INodeLong argB;
      public final IExpressionNode.INodeLong argC;

      public FuncLongLongLongToDouble(IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeLong argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public double evaluate() {
         return NodeFuncLongLongLongToDouble.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         return !NodeFuncLongLongLongToDouble.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncLongLongLongToDouble.this.new FuncLongLongLongToDouble(a, b, c),
               (a, b, c) -> NodeFuncLongLongLongToDouble.this.new FuncLongLongLongToDouble(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncLongLongLongToDouble.this.new FuncLongLongLongToDouble(a, b, c),
               (a, b, c) -> NodeConstantDouble.of(NodeFuncLongLongLongToDouble.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongLongLongToDouble.this.canInline) {
            if (NodeFuncLongLongLongToDouble.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongLongLongToDouble.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncLongLongLongToDouble.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongLongLongToDouble.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncLongLongLongToDouble.FuncLongLongLongToDouble other = (NodeFuncLongLongLongToDouble.FuncLongLongLongToDouble)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncLongLongLongToDouble {
      double apply(long var1, long var3, long var5);
   }
}
