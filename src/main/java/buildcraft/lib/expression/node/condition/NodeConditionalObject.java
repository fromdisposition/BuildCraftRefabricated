package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.ExpressionDebugManager;
import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;

public class NodeConditionalObject<T> implements IExpressionNode.INodeObject<T>, IDependantNode {
   private final IExpressionNode.INodeBoolean condition;
   private final IExpressionNode.INodeObject<T> ifTrue;
   private final IExpressionNode.INodeObject<T> ifFalse;

   public NodeConditionalObject(IExpressionNode.INodeBoolean condition, IExpressionNode.INodeObject<T> ifTrue, IExpressionNode.INodeObject<T> ifFalse) {
      this.condition = condition;
      this.ifTrue = ifTrue;
      this.ifFalse = ifFalse;
   }

   @Override
   public Class<T> getType() {
      return this.ifTrue.getType();
   }

   @Override
   public T evaluate() {
      return this.condition.evaluate() ? this.ifTrue.evaluate() : this.ifFalse.evaluate();
   }

   @Override
   public IExpressionNode.INodeObject<T> inline() {
      ExpressionDebugManager.debugStart("Inlining " + this);
      IExpressionNode.INodeBoolean c = this.condition.inline();
      IExpressionNode.INodeObject<T> t = this.ifTrue.inline();
      IExpressionNode.INodeObject<T> f = this.ifFalse.inline();
      if (c instanceof IConstantNode) {
         IExpressionNode.INodeObject<T> val = c.evaluate() ? t : f;
         ExpressionDebugManager.debugEnd("Fully inlined to " + val);
         return val;
      } else if (c == this.condition && t == this.ifTrue && f == this.ifFalse) {
         ExpressionDebugManager.debugEnd("Unable to inline at all!");
         return this;
      } else {
         NodeConditionalObject<T> val = new NodeConditionalObject<>(c, t, f);
         ExpressionDebugManager.debugEnd("Partially inlined to " + val);
         return val;
      }
   }

   @Override
   public void visitDependants(IDependancyVisitor visitor) {
      visitor.dependOn(this.condition, this.ifTrue, this.ifFalse);
   }

   @Override
   public String toString() {
      return "(" + this.condition + ") ? (" + this.ifTrue + ") : (" + this.ifFalse + ")";
   }
}
