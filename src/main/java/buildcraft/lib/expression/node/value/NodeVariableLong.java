package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;

public class NodeVariableLong extends NodeVariable implements IVariableNode.IVariableNodeLong, IDependantNode {
   public long value;
   private IExpressionNode.INodeLong src;

   public NodeVariableLong(String name) {
      super(name);
   }

   @Override
   public long evaluate() {
      return this.src != null ? this.src.evaluate() : this.value;
   }

   @Override
   public IExpressionNode.INodeLong inline() {
      if (this.isConst) {
         return new NodeConstantLong(this.value);
      } else {
         return this.src != null ? this.src.inline() : this;
      }
   }

   @Override
   public void set(long value) {
      this.value = value;
   }

   @Override
   public void setConstantSource(IExpressionNode source) {
      if (this.src != null) {
         throw new IllegalStateException("Already have a constant source");
      }

      this.src = (IExpressionNode.INodeLong)source;
   }

   @Override
   public void visitDependants(IDependancyVisitor visitor) {
      if (this.src != null) {
         visitor.dependOn(this.src);
      } else {
         visitor.dependOnExplictly(this);
      }
   }
}
