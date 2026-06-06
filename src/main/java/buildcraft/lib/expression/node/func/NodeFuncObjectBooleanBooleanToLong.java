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

public class NodeFuncObjectBooleanBooleanToLong<A> extends NodeFuncBase implements INodeFunc.INodeFuncLong {
   public final NodeFuncObjectBooleanBooleanToLong.IFuncObjectBooleanBooleanToLong<A> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;

   public NodeFuncObjectBooleanBooleanToLong(String name, Class<A> argTypeA, NodeFuncObjectBooleanBooleanToLong.IFuncObjectBooleanBooleanToLong<A> function) {
      this(argTypeA, function, (a, b, c) -> "[ " + NodeTypes.getName(argTypeA) + ", boolean, boolean -> long ] " + name + "(" + a + ", " + b + ", " + c + ")");
   }

   public NodeFuncObjectBooleanBooleanToLong(
      Class<A> argTypeA, NodeFuncObjectBooleanBooleanToLong.IFuncObjectBooleanBooleanToLong<A> function, StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncObjectBooleanBooleanToLong<A> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectBooleanBooleanToLong<A>.FuncObjectBooleanBooleanToLong create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC
   ) {
      return new NodeFuncObjectBooleanBooleanToLong.FuncObjectBooleanBooleanToLong(argA, argB, argC);
   }

   public class FuncObjectBooleanBooleanToLong implements IExpressionNode.INodeLong, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;

      public FuncObjectBooleanBooleanToLong(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public long evaluate() {
         return NodeFuncObjectBooleanBooleanToLong.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         return !NodeFuncObjectBooleanBooleanToLong.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectBooleanBooleanToLong.this.new FuncObjectBooleanBooleanToLong(a, b, c),
               (a, b, c) -> NodeFuncObjectBooleanBooleanToLong.this.new FuncObjectBooleanBooleanToLong(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectBooleanBooleanToLong.this.new FuncObjectBooleanBooleanToLong(a, b, c),
               (a, b, c) -> NodeConstantLong.of(NodeFuncObjectBooleanBooleanToLong.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate()))
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectBooleanBooleanToLong.this.canInline) {
            if (NodeFuncObjectBooleanBooleanToLong.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectBooleanBooleanToLong.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectBooleanBooleanToLong.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectBooleanBooleanToLong.this;
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
            NodeFuncObjectBooleanBooleanToLong<A>.FuncObjectBooleanBooleanToLong other = (NodeFuncObjectBooleanBooleanToLong.FuncObjectBooleanBooleanToLong)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectBooleanBooleanToLong<A> {
      long apply(A var1, boolean var2, boolean var3);
   }
}
