package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;

public class NodeVariableDouble extends NodeVariable implements IVariableNode.IVariableNodeDouble, IDependantNode {
   public double value;
   private IExpressionNode.INodeDouble src;

   public NodeVariableDouble(String name) {
      super(name);
   }

   @Override
   public double evaluate() {
      return this.src != null ? this.src.evaluate() : this.value;
   }

   @Override
   public IExpressionNode.INodeDouble inline() {
      if (this.isConst) {
         return new NodeConstantDouble(this.value);
      } else {
         return this.src != null ? this.src.inline() : this;
      }
   }

   @Override
   public void set(double value) {
      this.value = value;
   }

   @Override
   public void setConstantSource(IExpressionNode source) {
      if (this.src != null) {
         throw new IllegalStateException("Already have a constant source");
      }

      this.src = (IExpressionNode.INodeDouble)source;
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
