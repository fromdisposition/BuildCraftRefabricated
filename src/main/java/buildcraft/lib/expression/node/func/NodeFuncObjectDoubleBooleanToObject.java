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

public class NodeFuncObjectDoubleBooleanToObject<A, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<A, R> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<R> returnType;

   public NodeFuncObjectDoubleBooleanToObject(
      String name, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<A, R> function
   ) {
      this(
         argTypeA,
         returnType,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", double, boolean -> "
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

   public NodeFuncObjectDoubleBooleanToObject(
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<A, R> function,
      StringFunctionQuad stringFunction
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

   public NodeFuncObjectDoubleBooleanToObject<A, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeBoolean c = stack.popBoolean();
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectDoubleBooleanToObject<A, R>.FuncObjectDoubleBooleanToObject create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeBoolean argC
   ) {
      return new FuncObjectDoubleBooleanToObject(argA, argB, argC);
   }

   public class FuncObjectDoubleBooleanToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeBoolean argC;

      public FuncObjectDoubleBooleanToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeBoolean argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectDoubleBooleanToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectDoubleBooleanToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectDoubleBooleanToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleBooleanToObject.this.new FuncObjectDoubleBooleanToObject(a, b, c),
               (a, b, c) -> NodeFuncObjectDoubleBooleanToObject.this.new FuncObjectDoubleBooleanToObject(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleBooleanToObject.this.new FuncObjectDoubleBooleanToObject(a, b, c),
               (a, b, c) -> new NodeConstantObject<>(
                  NodeFuncObjectDoubleBooleanToObject.this.returnType,
                  NodeFuncObjectDoubleBooleanToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleBooleanToObject.this.canInline) {
            if (NodeFuncObjectDoubleBooleanToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleBooleanToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleBooleanToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleBooleanToObject.this;
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
            NodeFuncObjectDoubleBooleanToObject<A, R>.FuncObjectDoubleBooleanToObject other = (NodeFuncObjectDoubleBooleanToObject.FuncObjectDoubleBooleanToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleBooleanToObject<A, R> {
      R apply(A var1, double var2, boolean var4);
   }
}
