package buildcraft.robotics.path;

import java.util.Iterator;
import net.minecraft.core.BlockPos;

/**
 * Iterates block offsets in expanding hollow cubes around the origin (radius 1..63). Port of BuildCraft 7.1.x
 * {@code BlockScannerExpanding}; offsets are relative and must be added to the scan origin.
 */
public class BlockScannerExpanding implements Iterable<BlockPos> {
   private int searchRadius = 1;
   private int searchX = -1;
   private int searchY = -1;
   private int searchZ = -1;

   @Override
   public Iterator<BlockPos> iterator() {
      return new BlockIt();
   }

   private final class BlockIt implements Iterator<BlockPos> {
      @Override
      public boolean hasNext() {
         return BlockScannerExpanding.this.searchRadius < 64;
      }

      @Override
      public BlockPos next() {
         BlockPos next = new BlockPos(BlockScannerExpanding.this.searchX, BlockScannerExpanding.this.searchY, BlockScannerExpanding.this.searchZ);

         if (Math.abs(BlockScannerExpanding.this.searchX) == BlockScannerExpanding.this.searchRadius
            || Math.abs(BlockScannerExpanding.this.searchZ) == BlockScannerExpanding.this.searchRadius) {
            BlockScannerExpanding.this.searchY++;
         } else {
            BlockScannerExpanding.this.searchY += BlockScannerExpanding.this.searchRadius * 2;
         }

         if (BlockScannerExpanding.this.searchY > BlockScannerExpanding.this.searchRadius) {
            BlockScannerExpanding.this.searchY = -BlockScannerExpanding.this.searchRadius;
            BlockScannerExpanding.this.searchZ++;
            if (BlockScannerExpanding.this.searchZ > BlockScannerExpanding.this.searchRadius) {
               BlockScannerExpanding.this.searchZ = -BlockScannerExpanding.this.searchRadius;
               BlockScannerExpanding.this.searchX++;
               if (BlockScannerExpanding.this.searchX > BlockScannerExpanding.this.searchRadius) {
                  BlockScannerExpanding.this.searchRadius++;
                  BlockScannerExpanding.this.searchX = -BlockScannerExpanding.this.searchRadius;
                  BlockScannerExpanding.this.searchY = -BlockScannerExpanding.this.searchRadius;
                  BlockScannerExpanding.this.searchZ = -BlockScannerExpanding.this.searchRadius;
               }
            }
         }

         return next;
      }
   }
}
