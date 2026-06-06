package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import java.util.Objects;

public class NodeFuncObjectBooleanToLong<A> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectBooleanToLong.IFuncObjectBooleanToLong<A> function;
   private final StringFunctionTri stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectBooleanToLong(String name, Class<A> argTypeA, NodeFuncObjectBooleanToLong.IFuncObjectBooleanToLong<A> function) {
      this(argTypeA, function, (a, b) -> "[ " + NodeTypes.getName(argTypeA) + ", boolean -> long ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncObjectBooleanToLong(Class<A> argTypeA, NodeFuncObjectBooleanToLong.IFuncObjectBooleanToLong<A> function, StringFunctionTri stringFunction) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncObjectBooleanToLong<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b);
   }

   public NodeFuncObjectBooleanToLong<A>.FuncObjectBooleanToLong create(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB) {
      return new NodeFuncObjectBooleanToLong.FuncObjectBooleanToLong(argA, argB);
   }

   public class FuncObjectBooleanToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeBoolean argB;

      public FuncObjectBooleanToLong(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectBooleanToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectBooleanToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectBooleanToLong.this.new FuncObjectBooleanToLong(a, b),
               (a, b) -> NodeFuncObjectBooleanToLong.this.new FuncObjectBooleanToLong(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncObjectBooleanToLong.this.new FuncObjectBooleanToLong(a, b),
               (a, b) -> NodeConstantLong.of(NodeFuncObjectBooleanToLong.this.function.apply(a.evaluate(), b.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectBooleanToLong.this.canInline) {
            if (NodeFuncObjectBooleanToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectBooleanToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncObjectBooleanToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectBooleanToLong.this;
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
            NodeFuncObjectBooleanToLong<A>.FuncObjectBooleanToLong other = (NodeFuncObjectBooleanToLong.FuncObjectBooleanToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectBooleanToLong<A> {
      long apply(A var1, boolean var2);
   }
}
