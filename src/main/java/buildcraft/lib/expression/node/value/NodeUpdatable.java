package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.NodeTypes;
import javax.annotation.Nullable;

public class NodeUpdatable implements ITickableNode, ITickableNode.Source {
   public final String name;
   public final NodeVariable variable;
   private IExpressionNode source;
   private boolean finalised;

   public NodeUpdatable(String name, IExpressionNode source) {
      this.name = name;
      this.variable = NodeTypes.makeVariableNode(NodeTypes.getType(source), name);
      this.setSource(source);
   }

   @Override
   public void refresh() {
      this.variable.set(this.source);
   }

   @Override
   public void tick() {
      this.refresh();
   }

   @Override
   public ITickableNode createTickable() {
      return this;
   }

   @Override
   public void setSource(IExpressionNode source) {
      this.source = source;
      this.refresh();
   }

   public void makeSourceConstant() {
      if (this.source == null) {
         throw new IllegalStateException("Source not set yet!");
      }

      this.finalised = true;
      this.variable.setConstantSource(this.source);
   }

   @Nullable
   public IExpressionNode getConstantSource() {
      return this.finalised ? this.source : null;
   }
}
