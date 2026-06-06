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

public class NodeFuncLongLongLongLongToObject<R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject<R> function;
   private final StringFunctionPenta stringFunction;
   private final Class<R> returnType;

   public NodeFuncLongLongLongLongToObject(String name, Class<R> returnType, NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject<R> function) {
      this(
         returnType,
         function,
         (a, b, c, d) -> "[ long, long, long, long -> " + NodeTypes.getName(returnType) + " ] " + name + "(" + a + ", " + b + ", " + c + ", " + d + ")"
      );
   }

   public NodeFuncLongLongLongLongToObject(
      Class<R> returnType, NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject<R> function, StringFunctionPenta stringFunction
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

   public NodeFuncLongLongLongLongToObject<R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeLong d = stack.popLong();
      IExpressionNode.INodeLong c = stack.popLong();
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeLong a = stack.popLong();
      return this.create(a, b, c, d);
   }

   public NodeFuncLongLongLongLongToObject<R>.FuncLongLongLongLongToObject create(
      IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeLong argC, IExpressionNode.INodeLong argD
   ) {
      return new NodeFuncLongLongLongLongToObject.FuncLongLongLongLongToObject(argA, argB, argC, argD);
   }

   public class FuncLongLongLongLongToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeLong argA;
      public final IExpressionNode.INodeLong argB;
      public final IExpressionNode.INodeLong argC;
      public final IExpressionNode.INodeLong argD;

      public FuncLongLongLongLongToObject(
         IExpressionNode.INodeLong argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeLong argC, IExpressionNode.INodeLong argD
      ) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
         this.argD = argD;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncLongLongLongLongToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncLongLongLongLongToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate(), this.argD.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncLongLongLongLongToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncLongLongLongLongToObject.this.new FuncLongLongLongLongToObject(a, b, c, d),
               (a, b, c, d) -> NodeFuncLongLongLongLongToObject.this.new FuncLongLongLongLongToObject(a, b, c, d)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               this.argD,
               (a, b, c, d) -> NodeFuncLongLongLongLongToObject.this.new FuncLongLongLongLongToObject(a, b, c, d),
               (a, b, c, d) -> new NodeConstantObject<>(
                  NodeFuncLongLongLongLongToObject.this.returnType,
                  NodeFuncLongLongLongLongToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate(), d.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncLongLongLongLongToObject.this.canInline) {
            if (NodeFuncLongLongLongLongToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncLongLongLongLongToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC, this.argD);
      }

      @Override
      public String toString() {
         return NodeFuncLongLongLongLongToObject.this.stringFunction
            .apply(this.argA.toString(), this.argB.toString(), this.argC.toString(), this.argD.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncLongLongLongLongToObject.this;
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
            NodeFuncLongLongLongLongToObject<R>.FuncLongLongLongLongToObject other = (NodeFuncLongLongLongLongToObject.FuncLongLongLongLongToObject)obj;
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
   public interface IFuncLongLongLongLongToObject<R> {
      R apply(long var1, long var3, long var5, long var7);
   }
}
