package buildcraft.lib.expression.node.condition;

import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeConditionalLong implements IExpressionNode.INodeLong, IDependantNode {
   private final IExpressionNode.INodeBoolean condition;
   private final IExpressionNode.INodeLong ifTrue;
   private final IExpressionNode.INodeLong ifFalse;

   public NodeConditionalLong(IExpressionNode.INodeBoolean condition, IExpressionNode.INodeLong ifTrue, IExpressionNode.INodeLong ifFalse) {
      this.condition = condition;
      this.ifTrue = ifTrue;
      this.ifFalse = ifFalse;
   }

   @Override
   public long evaluate() {
      return this.condition.evaluate() ? this.ifTrue.evaluate() : this.ifFalse.evaluate();
   }

   @Override
   public IExpressionNode.INodeLong inline() {
      IExpressionNode.INodeBoolean c = this.condition.inline();
      IExpressionNode.INodeLong t = this.ifTrue.inline();
      IExpressionNode.INodeLong f = this.ifFalse.inline();
      if (c instanceof NodeConstantBoolean && t instanceof NodeConstantLong && f instanceof NodeConstantLong) {
         return new NodeConstantLong(((NodeConstantBoolean)c).value ? ((NodeConstantLong)t).value : ((NodeConstantLong)f).value);
      } else if (c != this.condition || t != this.ifTrue || f != this.ifFalse) {
         return new NodeConditionalLong(c, t, f);
      } else if (c instanceof NodeConstantBoolean) {
         return ((NodeConstantBoolean)c).value ? t : f;
      } else {
         return this;
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
