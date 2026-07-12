package buildcraft.lib.fabric.transfer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Queues side effects until the ROOT transaction commits, mirroring how Fabric's own
 * {@code SnapshotParticipant} tracks state per nesting level: each depth that holds entries has a
 * close callback; when a nested transaction commits, its entries are adopted by the parent (hooking
 * it on demand — the parent is still open at that point), so the chain always reaches depth 0. An
 * abort at any depth discards only the entries queued at or below it.
 */
public final class FabricDeferredCommit<T> {
   private final List<T> pending = new ArrayList<>();
   private final List<Integer> watermarks = new ArrayList<>();
   private final BitSet hooked = new BitSet();
   private final Consumer<T> onRootCommit;

   public FabricDeferredCommit(Consumer<T> onRootCommit) {
      this.onRootCommit = onRootCommit;
   }

   public void enqueue(TransactionContext transaction, T entry) {
      this.hook(transaction, transaction.nestingDepth(), this.pending.size());
      this.pending.add(entry);
   }

   /** Watermark = pending size when the depth got its first entry; only meaningful while hooked. */
   private void hook(TransactionContext transaction, int depth, int watermark) {
      if (!this.hooked.get(depth)) {
         while (this.watermarks.size() <= depth) {
            this.watermarks.add(0);
         }

         this.watermarks.set(depth, watermark);
         this.hooked.set(depth);
         transaction.getOpenTransaction(depth).addCloseCallback((context, result) -> this.onClose(context, depth, result.wasCommitted()));
      }
   }

   private void onClose(TransactionContext context, int depth, boolean committed) {
      this.hooked.clear(depth);
      int watermark = this.watermarks.get(depth);
      if (!committed) {
         while (this.pending.size() > watermark) {
            this.pending.removeLast();
         }
      } else if (depth > 0) {
         // Committed into the still-open parent: it now owns these entries. If the parent is already
         // hooked its (smaller or equal) watermark stays, and the entries merge under it.
         this.hook(context, depth - 1, watermark);
      } else {
         for (int i = watermark; i < this.pending.size(); i++) {
            this.onRootCommit.accept(this.pending.get(i));
         }

         while (this.pending.size() > watermark) {
            this.pending.removeLast();
         }
      }
   }
}
