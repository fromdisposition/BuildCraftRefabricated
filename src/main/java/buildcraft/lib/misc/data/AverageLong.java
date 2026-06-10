/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import net.minecraft.nbt.CompoundTag;

public class AverageLong {
   private long[] data;
   private final int precise;
   private int pos;
   private long averageRaw;
   private long tickValue;

   public AverageLong(int precise) {
      this.precise = precise;
      this.clear();
   }

   public void clear() {
      this.data = new long[this.precise];
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
      int[] ints = nbt.getIntArray(subTag).orElse(new int[0]);
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
