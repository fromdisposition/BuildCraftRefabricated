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

public class NodeFuncBooleanBooleanToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncBooleanBooleanToObject.IFuncBooleanBooleanToObject<R> function;
   private final StringFunctionTri stringFunction;
   private final Class<R> returnType;

   public NodeFuncBooleanBooleanToObject(String name, Class<R> returnType, NodeFuncBooleanBooleanToObject.IFuncBooleanBooleanToObject<R> function) {
      this(returnType, function, (a, b) -> "[ boolean, boolean -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncBooleanBooleanToObject(
      Class<R> returnType, NodeFuncBooleanBooleanToObject.IFuncBooleanBooleanToObject<R> function, StringFunctionTri stringFunction
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
      return this.stringFunction.apply("{A}", "{B}");
   }

   public NodeFuncBooleanBooleanToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b);
   }

   public NodeFuncBooleanBooleanToObject<R>.FuncBooleanBooleanToObject create(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB) {
      return new FuncBooleanBooleanToObject(argA, argB);
   }

   public class FuncBooleanBooleanToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;

      public FuncBooleanBooleanToObject(IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncBooleanBooleanToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncBooleanBooleanToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncBooleanBooleanToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncBooleanBooleanToObject.this.new FuncBooleanBooleanToObject(a, b),
               (a, b) -> NodeFuncBooleanBooleanToObject.this.new FuncBooleanBooleanToObject(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncBooleanBooleanToObject.this.new FuncBooleanBooleanToObject(a, b),
               (a, b) -> new NodeConstantObject<>(
                  NodeFuncBooleanBooleanToObject.this.returnType, NodeFuncBooleanBooleanToObject.this.function.apply(a.evaluate(), b.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanToObject.this.canInline) {
            if (NodeFuncBooleanBooleanToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanToObject.this;
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
            NodeFuncBooleanBooleanToObject<R>.FuncBooleanBooleanToObject other = (NodeFuncBooleanBooleanToObject.FuncBooleanBooleanToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncBooleanBooleanToObject<R> {
      R apply(boolean var1, boolean var2);
   }
}
