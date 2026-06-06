package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import java.util.Objects;

public class NodeFuncBooleanBooleanBooleanToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanToObject<R> function;
   private final StringFunctionQuad stringFunction;
   private final Class<R> returnType;

   public NodeFuncBooleanBooleanBooleanToObject(
      String name, Class<R> returnType, NodeFuncBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanToObject<R> function
   ) {
      this(
         returnType,
         function,
         (a, b, c) -> "[ boolean, boolean, boolean -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ", " + c + ")"
      );
   }

   public NodeFuncBooleanBooleanBooleanToObject(
      Class<R> returnType, NodeFuncBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanToObject<R> function, StringFunctionQuad stringFunction
   ) {
      this.returnType = returnType;
      this.function = function;
      this.stringFunction = stringFunction;
   }

   @Override
   public Class<R> getType() {
      return this.returnType;
   }

   @Override
   public String toString() {
      return this.stringFunction.apply("{A}", "{B}", "{C}");
   }

   public NodeFuncBooleanBooleanBooleanToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b, c);
   }

   public NodeFuncBooleanBooleanBooleanToObject<R>.FuncBooleanBooleanBooleanToObject create(
      IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC
   ) {
      return new FuncBooleanBooleanBooleanToObject(argA, argB, argC);
   }

   public class FuncBooleanBooleanBooleanToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;

      public FuncBooleanBooleanBooleanToObject(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncBooleanBooleanBooleanToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncBooleanBooleanBooleanToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncBooleanBooleanBooleanToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToObject.this.new FuncBooleanBooleanBooleanToObject(a, b, c),
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToObject.this.new FuncBooleanBooleanBooleanToObject(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncBooleanBooleanBooleanToObject.this.new FuncBooleanBooleanBooleanToObject(a, b, c),
               (a, b, c) -> new NodeConstantObject<>(
                  NodeFuncBooleanBooleanBooleanToObject.this.returnType,
                  NodeFuncBooleanBooleanBooleanToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanBooleanToObject.this.canInline) {
            if (NodeFuncBooleanBooleanBooleanToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanBooleanToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanBooleanToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanBooleanToObject.this;
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
            NodeFuncBooleanBooleanBooleanToObject<R>.FuncBooleanBooleanBooleanToObject other = (NodeFuncBooleanBooleanBooleanToObject.FuncBooleanBooleanBooleanToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanBooleanToObject<R> {
      R apply(boolean var1, boolean var2, boolean var3);
   }
}
