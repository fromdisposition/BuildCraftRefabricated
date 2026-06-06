package buildcraft.lib.misc.data;

import buildcraft.lib.expression.node.value.ITickableNode;
import buildcraft.lib.expression.node.value.NodeStateful;
import buildcraft.lib.expression.node.value.NodeUpdatable;
import java.util.Arrays;
import java.util.List;

public class ModelVariableData {
   private static int currentBakeId = 0;
   private int bakeId = -1;
   private ITickableNode[] tickableNodes;

   public static void onModelBake() {
      currentBakeId++;
   }

   public boolean hasNoNodes() {
      return this.tickableNodes == null;
   }

   public void setNodes(ITickableNode[] nodes) {
      this.bakeId = currentBakeId;
      this.tickableNodes = nodes;
   }

   public void addNodes(ITickableNode[] additional) {
      int originalLength = this.tickableNodes.length;
      this.tickableNodes = Arrays.copyOf(this.tickableNodes, originalLength + additional.length);
      System.arraycopy(additional, 0, this.tickableNodes, originalLength, additional.length);
   }

   private boolean checkModelBake() {
      if (this.tickableNodes == null) {
         return false;
      }

      if (currentBakeId == this.bakeId) {
         return true;
      }

      this.tickableNodes = null;
      return false;
   }

   public void refresh() {
      if (this.checkModelBake()) {
         for (ITickableNode node : this.tickableNodes) {
            node.refresh();
         }
      }
   }

   public void tick() {
      if (this.checkModelBake()) {
         for (ITickableNode node : this.tickableNodes) {
            node.tick();
         }
      }
   }

   public void addDebugInfo(List<String> to) {
      if (this.tickableNodes != null) {
         for (ITickableNode node : this.tickableNodes) {
            if (node instanceof NodeUpdatable nU) {
               to.add("  " + nU.name + " = " + nU.variable.evaluateAsString());
            } else if (node instanceof NodeStateful.Instance nS) {
               to.add("  " + nS.getContainer().name + " = " + nS.storedVar.evaluateAsString());
            }
         }
      }
   }
}
