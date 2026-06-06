package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;

public enum ClientArchitectScans {
   INSTANCE;

   public static final int START_SCANNED_BLOCK_VALUE = 50;
   private final List<ClientArchitectScans.ScanRun> runs = new ArrayList<>();

   public void onReceived(List<BlockPos> positions) {
      if (!positions.isEmpty()) {
         BlockPos start = null;
         BlockPos previous = null;

         for (BlockPos pos : positions) {
            if (start == null) {
               start = pos;
               previous = pos;
            } else if (isNextX(previous, pos)) {
               previous = pos;
            } else {
               this.runs.add(new ClientArchitectScans.ScanRun(start.immutable(), previous.immutable(), 50));
               start = pos;
               previous = pos;
            }
         }

         if (start != null) {
            this.runs.add(new ClientArchitectScans.ScanRun(start.immutable(), previous.immutable(), 50));
         }
      }
   }

   public void tick() {
      this.runs.removeIf(ClientArchitectScans.ScanRun::tickAndExpired);
   }

   public List<ClientArchitectScans.ScanRun> getRuns() {
      return this.runs;
   }

   public void clear() {
      this.runs.clear();
   }

   private static boolean isNextX(BlockPos previous, BlockPos pos) {
      return previous.getY() == pos.getY() && previous.getZ() == pos.getZ() && previous.getX() + 1 == pos.getX();
   }

   public static final class ScanRun {
      private final BlockPos min;
      private final BlockPos max;
      private int remaining;

      private ScanRun(BlockPos start, BlockPos end, int remaining) {
         this.min = new BlockPos(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()));
         this.max = new BlockPos(Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
         this.remaining = remaining;
      }

      public BlockPos min() {
         return this.min;
      }

      public BlockPos max() {
         return this.max;
      }

      public int remaining() {
         return this.remaining;
      }

      private boolean tickAndExpired() {
         this.remaining--;
         return this.remaining <= 0;
      }
   }
}
