package buildcraft.transport.wire;

import buildcraft.lib.sync.ClientStateMirror;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public enum ClientWireSystems implements ClientStateMirror {
   INSTANCE;

   public final Map<Integer, WireSystem> wireSystems = new HashMap<>();

   public void applyRemoved(@Nullable int[] removedIds) {
      if (removedIds != null) {
         for (int id : removedIds) {
            this.wireSystems.remove(id);
         }
      }
   }

   public void applyTopology(@Nullable Map<Integer, WireSystem> topology) {
      if (topology != null) {
         this.wireSystems.putAll(topology);
      }
   }

   @Override
   public void applyFullSync(Runnable fullReplace) {
      fullReplace.run();
   }

   @Override
   public void applyDelta(Runnable deltaApply) {
      deltaApply.run();
   }
}
