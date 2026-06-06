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

public class NodeFuncBooleanBooleanBooleanBooleanToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncBooleanBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanBooleanToObject<R> function;
   private final StringFunctionPenta stringFunction;
   private final Class<R> returnType;

   public NodeFuncBooleanBooleanBooleanBooleanToObject(
      String name, Class<R> returnType, NodeFuncBooleanBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanBooleanToObject<R> function
   ) {
      this(
         returnType,
         function,
         (a, b, c, d) -> "[ boolean, boolean, boolean, boolean -> "
            + NodeTypes.getName(returnType)
            + " ] "
            + name
            + "("
            + a
            + ", "
            + b
            + ", "
            + c
            + ", "
            + d
            + ")"
      );
   }

   public NodeFuncBooleanBooleanBooleanBooleanToObject(
      Class<R> returnType,
      NodeFuncBooleanBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanBooleanToObject<R> function,
      StringFunctionPenta stringFunction
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
      return this.stringFunction.apply("{A}", "{B}", "{C}", "{D}");
   }

   public NodeFuncBooleanBooleanBooleanBooleanToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean d = stack.popBoolean();
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeBoolean b = stack.popBoolean();
      IExpressionNode.INodeBoolean a = stack.popBoolean();
      return this.create(a, b, c, d);
   }

   public NodeFuncBooleanBooleanBooleanBooleanToObject<R>.FuncBooleanBooleanBooleanBooleanToObject create(
      IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC, IExpressionNode.INodeBoolean argD
   ) {
      return new FuncBooleanBooleanBooleanBooleanToObject(argA, argB, argC, argD);
   }

   public class FuncBooleanBooleanBooleanBooleanToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeBoolean argA;
      public final IExpressionNode.INodeBoolean argB;
      public final IExpressionNode.INodeBoolean argC;
      public final IExpressionNode.INodeBoolean argD;

      public FuncBooleanBooleanBooleanBooleanToObject(
         IExpressionNode.INodeBoolean argA, IExpressionNode.INodeBoolean argB, IExpressionNode.INodeBoolean argC, IExpressionNode.INodeBoolean argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncBooleanBooleanBooleanBooleanToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncBooleanBooleanBooleanBooleanToObject.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncBooleanBooleanBooleanBooleanToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToObject.this.new FuncBooleanBooleanBooleanBooleanToObject(a, b, c, d),
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToObject.this.new FuncBooleanBooleanBooleanBooleanToObject(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncBooleanBooleanBooleanBooleanToObject.this.new FuncBooleanBooleanBooleanBooleanToObject(a, b, c, d),
               (a, b, c, d) -> new NodeConstantObject<>(
                  NodeFuncBooleanBooleanBooleanBooleanToObject.this.returnType,
                  NodeFuncBooleanBooleanBooleanBooleanToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncBooleanBooleanBooleanBooleanToObject.this.canInline) {
            if (NodeFuncBooleanBooleanBooleanBooleanToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncBooleanBooleanBooleanBooleanToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncBooleanBooleanBooleanBooleanToObject.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncBooleanBooleanBooleanBooleanToObject.this;
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
            NodeFuncBooleanBooleanBooleanBooleanToObject<R>.FuncBooleanBooleanBooleanBooleanToObject other = (NodeFuncBooleanBooleanBooleanBooleanToObject.FuncBooleanBooleanBooleanBooleanToObject)obj;
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
   public interface IFuncBooleanBooleanBooleanBooleanToObject<R> {
      R apply(boolean var1, boolean var2, boolean var3, boolean var4);
   }
}
