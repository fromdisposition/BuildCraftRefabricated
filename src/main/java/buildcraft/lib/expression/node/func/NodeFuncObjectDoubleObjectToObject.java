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

public class NodeFuncObjectDoubleObjectToObject<A, C, R> extends NodeFuncBase implements INodeFunc.INodeFuncObject<R> {
   public final NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, C, R> function;
   private final StringFunctionQuad stringFunction;
   private final Class<A> argTypeA;
   private final Class<C> argTypeC;
   private final Class<R> returnType;

   public NodeFuncObjectDoubleObjectToObject(
      String name,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, C, R> function
   ) {
      this(
         argTypeA,
         argTypeC,
         returnType,
         function,
         (a, b, c) -> "[ "
            + NodeTypes.getName(argTypeA)
            + ", double, "
            + NodeTypes.getName(argTypeC)
            + " -> "
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

   public NodeFuncObjectDoubleObjectToObject(
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, C, R> function,
      StringFunctionQuad stringFunction
   ) {
      this.argTypeA = argTypeA;
      this.argTypeC = argTypeC;
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

   public NodeFuncObjectDoubleObjectToObject<A, C, R> setNeverInline() {
      super.setNeverInline();
      return this;
   }

   @Override
   public IExpressionNode.INodeObject<R> getNode(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode.INodeObject<C> c = stack.popObject(this.argTypeC);
      IExpressionNode.INodeDouble b = stack.popDouble();
      IExpressionNode.INodeObject<A> a = stack.popObject(this.argTypeA);
      return this.create(a, b, c);
   }

   public NodeFuncObjectDoubleObjectToObject<A, C, R>.FuncObjectDoubleObjectToObject create(
      IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeObject<C> argC
   ) {
      return new NodeFuncObjectDoubleObjectToObject.FuncObjectDoubleObjectToObject(argA, argB, argC);
   }

   public class FuncObjectDoubleObjectToObject implements IExpressionNode.INodeObject<R>, IDependantNode, NodeFuncBase.IFunctionNode {
      public final IExpressionNode.INodeObject<A> argA;
      public final IExpressionNode.INodeDouble argB;
      public final IExpressionNode.INodeObject<C> argC;

      public FuncObjectDoubleObjectToObject(IExpressionNode.INodeObject<A> argA, IExpressionNode.INodeDouble argB, IExpressionNode.INodeObject<C> argC) {
         this.argA = argA;
         this.argB = argB;
         this.argC = argC;
      }

      @Override
      public Class<R> getType() {
         return NodeFuncObjectDoubleObjectToObject.this.returnType;
      }

      @Override
      public R evaluate() {
         return NodeFuncObjectDoubleObjectToObject.this.function.apply(this.argA.evaluate(), this.argB.evaluate(), this.argC.evaluate());
      }

      @Override
      public IExpressionNode.INodeObject<R> inline() {
         return !NodeFuncObjectDoubleObjectToObject.this.canInline
            ? NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleObjectToObject.this.new FuncObjectDoubleObjectToObject(a, b, c),
               (a, b, c) -> NodeFuncObjectDoubleObjectToObject.this.new FuncObjectDoubleObjectToObject(a, b, c)
            )
            : NodeInliningHelper.tryInline(
               this,
               this.argA,
               this.argB,
               this.argC,
               (a, b, c) -> NodeFuncObjectDoubleObjectToObject.this.new FuncObjectDoubleObjectToObject(a, b, c),
               (a, b, c) -> new NodeConstantObject<>(
                  NodeFuncObjectDoubleObjectToObject.this.returnType,
                  NodeFuncObjectDoubleObjectToObject.this.function.apply(a.evaluate(), b.evaluate(), c.evaluate())
               )
            );
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         if (!NodeFuncObjectDoubleObjectToObject.this.canInline) {
            if (NodeFuncObjectDoubleObjectToObject.this.function instanceof IDependantNode) {
               visitor.dependOn((IDependantNode)NodeFuncObjectDoubleObjectToObject.this.function);
            } else {
               visitor.dependOnExplictly(this);
            }
         }

         visitor.dependOn(this.argA, this.argB, this.argC);
      }

      @Override
      public String toString() {
         return NodeFuncObjectDoubleObjectToObject.this.stringFunction.apply(this.argA.toString(), this.argB.toString(), this.argC.toString());
      }

      @Override
      public NodeFuncBase getFunction() {
         return NodeFuncObjectDoubleObjectToObject.this;
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
            NodeFuncObjectDoubleObjectToObject<A, C, R>.FuncObjectDoubleObjectToObject other = (NodeFuncObjectDoubleObjectToObject.FuncObjectDoubleObjectToObject)obj;
            return Objects.equals(this.argA, other.argA) && Objects.equals(this.argB, other.argB) && Objects.equals(this.argC, other.argC);
         } else {
            return false;
         }
      }
   }

   @FunctionalInterface
   public interface IFuncObjectDoubleObjectToObject<A, C, R> {
      R apply(A var1, double var2, C var4);
   }
}
