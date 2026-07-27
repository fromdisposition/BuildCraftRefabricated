/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.path;

import java.util.Iterator;
import net.minecraft.core.BlockPos;

/**
 * Walks the shells of an expanding cube around the origin (relative coordinates). Like vanilla
 * {@code BlockPos.betweenClosed}, the iterator returns one reused {@link BlockPos.MutableBlockPos} instance --
 * read or copy ({@code immutable()}) the position before calling {@code next()} again, never store it directly.
 */
public class BlockScannerExpanding implements Iterable<BlockPos> {
   private final int maxRadius;
   private int searchRadius = 1;
   private int searchX = -1;
   private int searchY = -1;
   private int searchZ = -1;

   public BlockScannerExpanding() {
      this(64);
   }

   /** @param maxRadius how far (in blocks, Chebyshev) the expanding cube walks before it stops. */
   public BlockScannerExpanding(int maxRadius) {
      this.maxRadius = maxRadius;
   }

   @Override
   public Iterator<BlockPos> iterator() {
      return new BlockIt();
   }

   private final class BlockIt implements Iterator<BlockPos> {
      private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

      @Override
      public boolean hasNext() {
         return BlockScannerExpanding.this.searchRadius < BlockScannerExpanding.this.maxRadius;
      }

      @Override
      public BlockPos next() {
         BlockPos next = this.cursor
            .set(BlockScannerExpanding.this.searchX, BlockScannerExpanding.this.searchY, BlockScannerExpanding.this.searchZ);

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
