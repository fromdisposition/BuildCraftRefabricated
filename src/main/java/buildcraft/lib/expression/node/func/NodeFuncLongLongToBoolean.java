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

public class NodeFuncLongLongToBoolean extends NodeFuncBase implements INodeFunc.INodeFuncBoolean {
   public final NodeFuncLongLongToBoolean.IFuncLongLongToBoolean function;
   private final StringFunctionTri stringFunction;

   public NodeFuncLongLongToBoolean(String name, NodeFuncLongLongToBoolean.IFuncLongLongToBoolean function) {
      this(function, (a, b) -> "[ long, long -> boolean ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncLongLongToBoolean(NodeFuncLongLongToBoolean.IFuncLongLongToBoolean function, StringFunctionTri stringFunction) {
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncLongLongToBoolean setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a, b);
   }

   public NodeFuncLongLongToBoolean.FuncLongLongToBoolean create(IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB) {
      return new FuncLongLongToBoolean(argA, argB);
   }

   public class FuncLongLongToBoolean implements IExpressionNode.INodeBoolean, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;
      public final IExpressionNode.INodeLong argB;

      public FuncLongLongToBoolean(IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public boolean evaluate() {
         return NodeFuncLongLongToBoolean.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         return !NodeFuncLongLongToBoolean.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncLongLongToBoolean.this.new FuncLongLongToBoolean(a, b),
               (a, b) -> NodeFuncLongLongToBoolean.this.new FuncLongLongToBoolean(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncLongLongToBoolean.this.new FuncLongLongToBoolean(a, b),
               (a, b) -> NodeConstantBoolean.of(NodeFuncLongLongToBoolean.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongLongToBoolean.this.canInline) {
            if (NodeFuncLongLongToBoolean.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongLongToBoolean.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncLongLongToBoolean.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongLongToBoolean.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncLongLongToBoolean.FuncLongLongToBoolean other = (NodeFuncLongLongToBoolean.FuncLongLongToBoolean)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncLongLongToBoolean {
      boolean apply(long var1, long var3);
   }
}
