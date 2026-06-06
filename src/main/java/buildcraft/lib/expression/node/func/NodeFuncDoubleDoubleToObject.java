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

public class NodeFuncDoubleDoubleToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject<R> function;
   private final StringFunctionTri stringFunction;
   private final Class<R> returnType;

   public NodeFuncDoubleDoubleToObject(String name, Class<R> returnType, NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject<R> function) {
      this(returnType, function, (a, b) -> "[ double, double -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ")");
   }

   public NodeFuncDoubleDoubleToObject(
      Class<R> returnType, NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject<R> function, StringFunctionTri stringFunction
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

   public NodeFuncDoubleDoubleToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b);
   }

   public NodeFuncDoubleDoubleToObject<R>.FuncDoubleDoubleToObject create(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB) {
      return new FuncDoubleDoubleToObject(argA, argB);
   }

   public class FuncDoubleDoubleToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;

      public FuncDoubleDoubleToObject(IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB) {
         this.argA = argA;
         this.argB = argB;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncDoubleDoubleToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncDoubleDoubleToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncDoubleDoubleToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncDoubleDoubleToObject.this.new FuncDoubleDoubleToObject(a, b),
               (a, b) -> NodeFuncDoubleDoubleToObject.this.new FuncDoubleDoubleToObject(a, b)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               (a, b) -> NodeFuncDoubleDoubleToObject.this.new FuncDoubleDoubleToObject(a, b),
               (a, b) -> new NodeConstantObject<>(
                  NodeFuncDoubleDoubleToObject.this.returnType, NodeFuncDoubleDoubleToObject.this.function.apply(a.evaluate(), b.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleToObject.this.canInline) {
            if (NodeFuncDoubleDoubleToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleToObject.this;
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
            NodeFuncDoubleDoubleToObject<R>.FuncDoubleDoubleToObject other = (NodeFuncDoubleDoubleToObject.FuncDoubleDoubleToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncDoubleDoubleToObject<R> {
      R apply(double var1, double var3);
   }
}
