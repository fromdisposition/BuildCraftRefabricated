package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import java.util.Objects;

public class NodeFuncLongLongLongLongToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncLongLongLongLongToBoolean.IFuncLongLongLongLongToBoolean function;
   private final StringFunctionPenta stringFunction;

   public NodeFuncLongLongLongLongToBoolean(String name, NodeFuncLongLongLongLongToBoolean.IFuncLongLongLongLongToBoolean function) {
      this(function, (a, b, c, d) -> "[ long, long, long, long -> boolean ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")");
   }

   public NodeFuncLongLongLongLongToBoolean(NodeFuncLongLongLongLongToBoolean.IFuncLongLongLongLongToBoolean function, StringFunctionPenta stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncLongLongLongLongToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong d = stack.popLong();
      IExpressionNode.INodeLong c = stack.popLong();
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a, b, c, d);
   }

   public NodeFuncLongLongLongLongToBoolean.FuncLongLongLongLongToBoolean create(
      IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeLong argC, IExpressionNode.INodeLong argD
   ) {
      return new FuncLongLongLongLongToBoolean(argA, argB, argC, argD);
   }

   public class FuncLongLongLongLongToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;
      public final IExpressionNode.INodeLong argB;
      public final IExpressionNode.INodeLong argC;
      public final IExpressionNode.INodeLong argD;

      public FuncLongLongLongLongToBoolean(
         IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeLong argC, IExpressionNode.INodeLong argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncLongLongLongLongToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncLongLongLongLongToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncLongLongLongLongToBoolean.this.new FuncLongLongLongLongToBoolean(a, b, c, d),
               (a, b, c, d) -> NodeFuncLongLongLongLongToBoolean.this.new FuncLongLongLongLongToBoolean(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncLongLongLongLongToBoolean.this.new FuncLongLongLongLongToBoolean(a, b, c, d),
               (a, b, c, d) -> NodeConstantBoolean.of(
                  NodeFuncLongLongLongLongToBoolean.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongLongLongLongToBoolean.this.canInline) {
            if (NodeFuncLongLongLongLongToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongLongLongLongToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncLongLongLongLongToBoolean.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongLongLongLongToBoolean.this;
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
            NodeFuncLongLongLongLongToBoolean.FuncLongLongLongLongToBoolean other = (NodeFuncLongLongLongLongToBoolean.FuncLongLongLongLongToBoolean)obj;
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
   public interface IFuncLongLongLongLongToBoolean {
      boolean apply(long var1, long var3, long var5, long var7);
   }
}
