/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// Backed by an ArrayList with a moving head index so advance() is O(1) amortized (unlike list.remove(0)); this is
// on the item-pipe hot path (one advance per pipe per tick), so the front shift was worth removing.
public class DelayedList<E> {
   /** Compact (drop spent front buckets in one shift) once this many have been advanced past. */
   private static final int COMPACT_THRESHOLD = 64;

   protected final List<List<E>> elements;
   private final Supplier<List<E>> innerListSupplier;
   /** Index of the bucket due now (delay 0). Buckets before it are spent and dropped on compaction. */
   private int head = 0;

   public DelayedList() {
      this(new ArrayList<>(), ArrayList::new);
   }

   private DelayedList(List<List<E>> actualList, Supplier<List<E>> innerList) {
      this.elements = actualList;
      this.innerListSupplier = innerList;
   }

   public int getMaxDelay() {
      return this.elements.size() - this.head;
   }

   public List<E> advance() {
      if (this.head >= this.elements.size()) {
         return ImmutableList.of();
      }

      List<E> due = this.elements.get(this.head);
      this.head++;
      if (this.head >= COMPACT_THRESHOLD) {
         // Drop all spent front buckets at once (amortized O(1) per advance). The returned `due` list is a
         // separate object the caller still holds, so removing its slot here does not affect it.
         this.elements.subList(0, this.head).clear();
         this.head = 0;
      }

      return due;
   }

   public void add(int delay, E element) {
      if (delay < 0) {
         delay = 0;
      }

      int index = this.head + delay;
      while (this.elements.size() <= index) {
         this.elements.add(this.innerListSupplier.get());
      }

      this.elements.get(index).add(element);
   }

   public List<List<E>> getAllElements() {
      return this.head == 0 ? this.elements : this.elements.subList(this.head, this.elements.size());
   }

   public void clear() {
      this.elements.clear();
      this.head = 0;
   }
}
