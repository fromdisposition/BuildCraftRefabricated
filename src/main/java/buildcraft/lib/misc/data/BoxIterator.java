/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.api.core.IBox;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import java.util.Iterator;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;

public class BoxIterator implements Iterator<BlockPos> {
   @Nonnull
   private final BlockPos min;
   @Nonnull
   private final BlockPos max;
   private final boolean invert;
   private final boolean repeat;
   private AxisOrder order;
   private BlockPos current;
   private boolean hasRepeated = false;

   public BoxIterator(IBox box, AxisOrder order, boolean invert) {
      this(box.min(), box.max(), order, invert);
   }

   public BoxIterator(BlockPos min, BlockPos max, AxisOrder order, boolean invert) {
      this(min, max, invert, false, order, null);
   }

   private BoxIterator(BlockPos min, BlockPos max, boolean invert, boolean repeat, AxisOrder order, @Nullable BlockPos current) {
      if (min == null) {
         throw new NullPointerException("min");
      }

      if (max == null) {
         throw new NullPointerException("max");
      }

      if (order == null) {
         throw new NullPointerException("order");
      }

      this.min = min;
      this.max = max;
      this.invert = invert;
      this.repeat = repeat;
      this.order = order;
      this.current = current == null ? this.getStart() : current;
   }

   @Nullable
   public static BoxIterator readFromNbt(CompoundTag nbt) {
      BlockPos min = NBTUtilBC.readBlockPos(nbt.getCompoundOrEmpty("min"));
      BlockPos max = NBTUtilBC.readBlockPos(nbt.getCompoundOrEmpty("max"));
      boolean invert = nbt.getBooleanOr("invert", false);
      boolean repeat = false;
      AxisOrder order = AxisOrder.readNbt(nbt.getCompoundOrEmpty("order"));
      BlockPos current = NBTUtilBC.readBlockPos(nbt.getCompoundOrEmpty("current"));
      return min != null && max != null && order != null ? new BoxIterator(min, max, invert, repeat, order, current) : null;
   }

   public CompoundTag writeToNbt() {
      CompoundTag nbt = new CompoundTag();
      nbt.put("min", NBTUtilBC.writeBlockPos(this.min));
      nbt.put("max", NBTUtilBC.writeBlockPos(this.max));
      nbt.putBoolean("invert", this.invert);
      nbt.put("order", this.order.writeNBT());
      if (this.current != null) {
         nbt.put("current", NBTUtilBC.writeBlockPos(this.current));
      }

      return nbt;
   }

   private BlockPos getStart() {
      BlockPos pos = BlockPos.ZERO;
      pos = this.replace(pos, this.order.first);
      pos = this.replace(pos, this.order.second);
      return this.replace(pos, this.order.third);
   }

   private BlockPos replace(BlockPos toReplace, Direction facing) {
      BlockPos with = facing.getAxisDirection() == AxisDirection.POSITIVE ? this.min : this.max;
      return VecUtil.replaceValue(toReplace, facing.getAxis(), VecUtil.getValue(with, facing.getAxis()));
   }

   public BlockPos getCurrent() {
      return this.current;
   }

   @Nonnull
   public BlockPos getMin() {
      return this.min;
   }

   @Nonnull
   public BlockPos getMax() {
      return this.max;
   }

   public boolean isInvert() {
      return this.invert;
   }

   public boolean isRepeat() {
      return this.repeat;
   }

   public AxisOrder getOrder() {
      return this.order;
   }

   @Override
   public String toString() {
      return "{BoxIterator ["
         + blockPosToString(this.min)
         + "] -> ["
         + blockPosToString(this.max)
         + "] @ "
         + blockPosToString(this.current)
         + " order: ["
         + this.order
         + "]"
         + (this.invert ? " inverting" : "")
         + (this.repeat ? " repeating" : "")
         + " }";
   }

   private static String blockPosToString(@Nullable BlockPos pos) {
      return pos == null ? "null" : pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
   }

   public BlockPos advance() {
      if (this.current == null) {
         this.current = this.getStart();
         return this.getCurrent();
      }

      this.current = increment(this.current, this.order.first);
      if (this.shouldReset(this.order.first)) {
         if (this.invert) {
            this.order = this.order.invertFirst();
         }

         this.current = this.replace(this.current, this.order.first);
         this.current = increment(this.current, this.order.second);
         if (this.shouldReset(this.order.second)) {
            if (this.invert) {
               this.order = this.order.invertSecond();
            }

            this.current = this.replace(this.current, this.order.second);
            this.current = increment(this.current, this.order.third);
            if (this.shouldReset(this.order.third)) {
               if (this.repeat) {
                  if (this.invert) {
                     this.order = this.order.invertThird();
                  }

                  this.current = this.replace(this.current, this.order.third);
                  this.hasRepeated = true;
               } else {
                  this.current = null;
               }
            }
         }
      }

      return this.getCurrent();
   }

   private static BlockPos increment(BlockPos pos, Direction facing) {
      int diff = facing.getAxisDirection().getStep();
      int value = VecUtil.getValue(pos, facing.getAxis()) + diff;
      return VecUtil.replaceValue(pos, facing.getAxis(), value);
   }

   private boolean shouldReset(Direction facing) {
      int lstReturned = VecUtil.getValue(this.current, facing.getAxis());
      BlockPos goingTo = facing.getAxisDirection() == AxisDirection.POSITIVE ? this.max : this.min;
      int to = VecUtil.getValue(goingTo, facing.getAxis());
      return facing.getAxisDirection() == AxisDirection.POSITIVE ? lstReturned > to : lstReturned < to;
   }

   public boolean contains(BlockPos pos) {
      if (pos.getX() < this.min.getX() || pos.getX() > this.max.getX()) {
         return false;
      } else {
         return pos.getY() < this.min.getY() || pos.getY() > this.max.getY() ? false : pos.getZ() >= this.min.getZ() && pos.getZ() <= this.max.getZ();
      }
   }

   public boolean willVisit(BlockPos pos) {
      if (!this.contains(pos)) {
         return false;
      } else {
         return this.current == null ? true : this.compare(pos) < 0;
      }
   }

   public boolean hasVisited(BlockPos pos) {
      if (!this.contains(pos)) {
         return false;
      } else {
         return this.current == null && !this.hasRepeated ? false : this.compare(pos) >= 0;
      }
   }

   private int compare(BlockPos pos) {
      int cmp = this.compare(pos, this.order.third);
      if (cmp != 0) {
         return cmp;
      }

      cmp = this.compare(pos, this.order.second);
      return cmp != 0 ? cmp : this.compare(pos, this.order.first);
   }

   private int compare(BlockPos pos, Direction direction) {
      int argVal = VecUtil.getValue(pos, direction.getAxis());
      int currentVal = VecUtil.getValue(this.current, direction.getAxis());
      return (currentVal - argVal) * direction.getAxisDirection().getStep();
   }

   public void moveTo(BlockPos pos) {
      if (!this.contains(pos)) {
         throw new IllegalArgumentException("This " + this + " doesn't contain " + pos + "!");
      }

      Direction a = this.order.first;
      Direction b = this.order.second;
      Direction c = this.order.third;
      int valueA = VecUtil.getValue(pos, a.getAxis());
      int valueB = VecUtil.getValue(pos, b.getAxis());
      int valueC = VecUtil.getValue(pos, c.getAxis());
      int boundA = VecUtil.getValue(this.max, this.min, a);
      int boundB = VecUtil.getValue(this.max, this.min, b);
      int boundC = VecUtil.getValue(this.max, this.min, c);
      if (!this.invert) {
         if (valueA != boundA) {
            this.current = pos.relative(a.getOpposite());
         } else if (valueB != boundB) {
            this.current = pos.relative(b.getOpposite());
            this.current = VecUtil.replaceValue(this.current, a.getAxis(), VecUtil.getValue(this.min, this.max, a));
         } else if (valueC != boundC) {
            this.current = pos.relative(c.getOpposite());
            this.current = VecUtil.replaceValue(this.current, a.getAxis(), VecUtil.getValue(this.min, this.max, a));
            this.current = VecUtil.replaceValue(this.current, b.getAxis(), VecUtil.getValue(this.min, this.max, b));
         } else {
            this.current = null;
         }
      } else {
         if (this.current == null) {
            this.current = this.getStart();
         }

         int db = this.compare(pos, b);
         int dc = this.compare(pos, c);
         BlockPos size = this.max.subtract(this.min);
         int sizeB = 1 + VecUtil.getValue(size, b.getAxis());
         if ((dc * sizeB + db) % 2 == 1) {
            this.order = this.order.invertFirst();
         }

         if (dc % 2 == 1) {
            this.order = this.order.invertSecond();
         }

         a = this.order.first;
         b = this.order.second;
         c = this.order.third;
         boundA = VecUtil.getValue(this.max, this.min, a);
         boundB = VecUtil.getValue(this.max, this.min, b);
         boundC = VecUtil.getValue(this.max, this.min, c);
         if (valueA != boundA) {
            this.current = pos.relative(this.order.first.getOpposite());
         } else if (valueB != boundB) {
            this.current = pos.relative(this.order.second.getOpposite());
            this.order = this.order.invertFirst();
         } else if (valueC != boundC) {
            this.current = pos.relative(this.order.third.getOpposite());
            this.order = this.order.invertFirst();
            this.order = this.order.invertSecond();
         } else {
            this.current = null;
         }
      }
   }

   public boolean hasFinished() {
      return this.current == null;
   }

   @Override
   public boolean hasNext() {
      return !this.hasFinished();
   }

   public BlockPos next() {
      BlockPos c = this.current;
      this.advance();
      return c;
   }
}
