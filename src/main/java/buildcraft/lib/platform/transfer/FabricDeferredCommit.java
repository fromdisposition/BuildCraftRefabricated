package buildcraft.lib.platform.transfer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class FabricDeferredCommit<T> {
   private final List<T> pending = new ArrayList<>();
   private final List<Integer> watermarks = new ArrayList<>();
   private final BitSet hooked = new BitSet();
   private final Consumer<T> onRootCommit;

   public FabricDeferredCommit(Consumer<T> onRootCommit) {
      this.onRootCommit = onRootCommit;
   }

   public void enqueue(TransactionContext transaction, T entry) {
      int depth = transaction.nestingDepth();

      while (this.watermarks.size() <= depth) {
         this.watermarks.add(this.pending.size());
      }

      if (!this.hooked.get(depth)) {
         int hookedDepth = depth;
         transaction.getOpenTransaction(depth).addCloseCallback((context, result) -> this.onClose(hookedDepth, result.wasCommitted()));
         this.hooked.set(depth);
      }

      this.pending.add(entry);
   }

   private void onClose(int depth, boolean committed) {
      this.hooked.clear(depth);
      int watermark = this.watermarks.get(depth);
      if (!committed) {
         while (this.pending.size() > watermark) {
            this.pending.removeLast();
         }
      } else {
         if (depth == 0) {
            for (int i = watermark; i < this.pending.size(); i++) {
               this.onRootCommit.accept(this.pending.get(i));
            }

            this.pending.clear();
            this.watermarks.clear();
         }
      }
   }
}
