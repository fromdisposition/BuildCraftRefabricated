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

public class NodeFuncDoubleDoubleDoubleDoubleToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject<R> function;
   private final StringFunctionPenta stringFunction;
   private final Class<R> returnType;

   public NodeFuncDoubleDoubleDoubleDoubleToObject(
      String name, Class<R> returnType, NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject<R> function
   ) {
      this(
         returnType,
         function,
         (a, b, c, d) -> "[ double, double, double, double -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")"
      );
   }

   public NodeFuncDoubleDoubleDoubleDoubleToObject(
      Class<R> returnType, NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject<R> function, StringFunctionPenta stringFunction
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

   public NodeFuncDoubleDoubleDoubleDoubleToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble d = stack.popDouble();
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeDouble a = stack.popDouble();
      return this.create(a, b, c, d);
   }

   public NodeFuncDoubleDoubleDoubleDoubleToObject<R>.FuncDoubleDoubleDoubleDoubleToObject create(
      IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC, IExpressionNode.INodeDouble argD
   ) {
      return new NodeFuncDoubleDoubleDoubleDoubleToObject.FuncDoubleDoubleDoubleDoubleToObject(argA, argB, argC, argD);
   }

   public class FuncDoubleDoubleDoubleDoubleToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeDouble argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeDouble argC;
      public final IExpressionNode.INodeDouble argD;

      public FuncDoubleDoubleDoubleDoubleToObject(
         IExpressionNode.INodeDouble argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeDouble argC, IExpressionNode.INodeDouble argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncDoubleDoubleDoubleDoubleToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncDoubleDoubleDoubleDoubleToObject.this.function
            .apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncDoubleDoubleDoubleDoubleToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToObject.this.new FuncDoubleDoubleDoubleDoubleToObject(a, b, c, d),
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToObject.this.new FuncDoubleDoubleDoubleDoubleToObject(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncDoubleDoubleDoubleDoubleToObject.this.new FuncDoubleDoubleDoubleDoubleToObject(a, b, c, d),
               (a, b, c, d) -> new NodeConstantObject<>(
                  NodeFuncDoubleDoubleDoubleDoubleToObject.this.returnType,
                  NodeFuncDoubleDoubleDoubleDoubleToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncDoubleDoubleDoubleDoubleToObject.this.canInline) {
            if (NodeFuncDoubleDoubleDoubleDoubleToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncDoubleDoubleDoubleDoubleToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncDoubleDoubleDoubleDoubleToObject.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncDoubleDoubleDoubleDoubleToObject.this;
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && this.getClass() == obj.getClass()) {
            NodeFuncDoubleDoubleDoubleDoubleToObject<R>.FuncDoubleDoubleDoubleDoubleToObject other = (NodeFuncDoubleDoubleDoubleDoubleToObject.FuncDoubleDoubleDoubleDoubleToObject)obj;
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
   public interface IFuncDoubleDoubleDoubleDoubleToObject<R> {
      R apply(double var1, double var3, double var5, double var7);
   }
}
