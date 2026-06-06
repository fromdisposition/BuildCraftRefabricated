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

public class NodeFuncObjectLongDoubleToObject<A, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<A, R> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<R> returnType;

   public NodeFuncObjectLongDoubleToObject(
      String name, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<A, R> function
   ) {
      this(
         argTypeA,
         returnType,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", long, double -> "
            + NodeTypes.getName(returnType)
            + " ] "
            + name
            + "("
            + a
            + ", "
            + b
            + ", "
            + c
            + ")"
      );
   }

   public NodeFuncObjectLongDoubleToObject(
      Class<A> argTypeA, Class<R> returnType, NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<A, R> function, StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
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

   public NodeFuncObjectLongDoubleToObject<A, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeDouble c = stack.popDouble();
      IExpressionNode.INodeLong b = stack.popLong();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectLongDoubleToObject<A, R>.FuncObjectLongDoubleToObject create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeDouble argC
   ) {
      return new NodeFuncObjectLongDoubleToObject.FuncObjectLongDoubleToObject(argA, argB, argC);
   }

   public class FuncObjectLongDoubleToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeLong argB;
      public final IExpressionNode.INodeDouble argC;

      public FuncObjectLongDoubleToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeLong argB, IExpressionNode.INodeDouble argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectLongDoubleToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectLongDoubleToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectLongDoubleToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectLongDoubleToObject.this.new FuncObjectLongDoubleToObject(a, b, c),
               (a, b, c) -> NodeFuncObjectLongDoubleToObject.this.new FuncObjectLongDoubleToObject(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectLongDoubleToObject.this.new FuncObjectLongDoubleToObject(a, b, c),
               (a, b, c) -> new NodeConstantObject<>(
                  NodeFuncObjectLongDoubleToObject.this.returnType,
                  NodeFuncObjectLongDoubleToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectLongDoubleToObject.this.canInline) {
            if (NodeFuncObjectLongDoubleToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectLongDoubleToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectLongDoubleToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectLongDoubleToObject.this;
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
            NodeFuncObjectLongDoubleToObject<A, R>.FuncObjectLongDoubleToObject other = (NodeFuncObjectLongDoubleToObject.FuncObjectLongDoubleToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectLongDoubleToObject<A, R> {
      R apply(A var1, long var2, double var4);
   }
}
