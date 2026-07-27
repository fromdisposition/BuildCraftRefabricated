/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.lib.nbt.BcNbt;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;

public class AverageLong {
   private final long[] data;
   private final int precise;
   private int pos;
   private long averageRaw;
   private long tickValue;

   public AverageLong(int precise) {
      this.precise = precise;
      this.data = new long[precise];
   }

   public void clear() {
      // Callers clear() every idle tick (lasers without a target, tables without a recipe), so this must not
      // allocate: zero the existing array in place, and skip even that when the window is already empty.
      if (this.averageRaw == 0L && this.tickValue == 0L && this.pos == 0) {
         return;
      }

      Arrays.fill(this.data, 0L);
      this.pos = 0;
      this.averageRaw = 0L;
      this.tickValue = 0L;
   }

   public double getAverage() {
      return (double)this.averageRaw / this.precise;
   }

   public long getAverageLong() {
      return this.averageRaw / this.precise;
   }

   public void tick(long value) {
      this.internalTick(this.tickValue + value);
      this.tickValue = 0L;
   }

   public void tick() {
      this.internalTick(this.tickValue);
      this.tickValue = 0L;
   }

   private void internalTick(long value) {
      this.pos = ++this.pos % this.precise;
      long oldValue = this.data[this.pos];
      this.data[this.pos] = value;
      if (this.pos == 0) {
         this.averageRaw = 0L;

         for (long iValue : this.data) {
            this.averageRaw += iValue;
         }
      } else {
         this.averageRaw = this.averageRaw - oldValue + value;
      }
   }

   public void push(long value) {
      this.tickValue += value;
   }

   public void writeToNbt(CompoundTag nbt, String subTag) {
      int[] ints = new int[this.precise * 2];

      for (int i = 0; i < this.precise; i++) {
         long val = this.data[i];
         ints[i * 2] = (int)val;
         ints[i * 2 + 1] = (int)(val >>> 32);
      }

      nbt.putIntArray(subTag, ints);
   }

   public void readFromNbt(CompoundTag nbt, String subTag) {
      int[] ints = BcNbt.getIntArray(nbt, subTag);
      if (ints.length >= this.precise * 2) {
         this.averageRaw = 0L;
         this.pos = 0;
         this.tickValue = 0L;

         for (int i = 0; i < this.precise; i++) {
            long val = ints[i * 2];
            val |= (long)ints[i * 2 + 1] << 32;
            this.averageRaw += val;
            this.data[i] = val;
         }
      }
   }
}
