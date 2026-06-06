package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;

public interface ITickableNode {
   void refresh();

   void tick();

   interface Source {
      ITickableNode createTickable();

      void setSource(IExpressionNode var1);
   }
}
