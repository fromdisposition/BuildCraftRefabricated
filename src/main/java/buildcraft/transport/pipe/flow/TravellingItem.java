/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;
import java.util.EnumSet;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class TravellingItem {
   @Nonnull
   public final Supplier<ItemStack> clientItemLink;
   public int stackSize;
   public DyeColor colour;
   @Nonnull
   ItemStack stack;
   int id = 0;
   boolean toCenter;
   double speed = 0.05;
   long tickStarted;
   long tickFinished;
   int timeToDest;
   Direction side;
   EnumSet<Direction> tried = EnumSet.noneOf(Direction.class);
   boolean isPhantom = false;

   public TravellingItem(@Nonnull ItemStack stack) {
      this.stack = stack;
      this.clientItemLink = () -> ItemStack.EMPTY;
   }

   public TravellingItem(Supplier<ItemStack> clientStackLink, int count) {
      this.clientItemLink = StackUtil.asNonNull(clientStackLink);
      this.stackSize = count;
      this.stack = StackUtil.EMPTY;
   }

   public TravellingItem(CompoundTag nbt, long tickNow) {
      this.clientItemLink = () -> ItemStack.EMPTY;
      this.stack = NBTUtilBC.itemStackFromNBT(BcNbt.getCompound(nbt, "stack"));
      if (this.stack.isEmpty()) {
         CompoundTag stackTag = BcNbt.getCompound(nbt, "stack");
         if (!stackTag.isEmpty()) {
            this.stack = ItemStack.EMPTY;
         }
      }

      int c = BcNbt.getByte(nbt, "colour", (byte)0);
      this.colour = c == 0 ? null : DyeColor.byId(c - 1);
      this.toCenter = BcNbt.getBoolean(nbt, "toCenter", false);
      this.speed = BcNbt.getDouble(nbt, "speed", 0.05);
      if (this.speed < 0.001) {
         this.speed = 0.001;
      }

      this.tickStarted = BcNbt.getInt(nbt, "tickStarted", 0) + tickNow;
      this.tickFinished = BcNbt.getInt(nbt, "tickFinished", 0) + tickNow;
      this.timeToDest = BcNbt.getInt(nbt, "timeToDest", 0);
      this.side = NBTUtilBC.readEnum(nbt.get("side"), Direction.class);
      if (this.side == null || this.timeToDest == 0) {
         this.toCenter = true;
      }

      this.tried = readEnumSet(nbt.get("tried"), Direction.class);
      this.isPhantom = BcNbt.getBoolean(nbt, "isPhantom", false);
   }

   public CompoundTag writeToNbt(long tickNow) {
      CompoundTag nbt = new CompoundTag();
      nbt.put("stack", NBTUtilBC.itemStackToNBT(this.stack));
      nbt.putByte("colour", (byte)(this.colour == null ? 0 : this.colour.getId() + 1));
      nbt.putBoolean("toCenter", this.toCenter);
      nbt.putDouble("speed", this.speed);
      nbt.putInt("tickStarted", (int)(this.tickStarted - tickNow));
      nbt.putInt("tickFinished", (int)(this.tickFinished - tickNow));
      nbt.putInt("timeToDest", this.timeToDest);
      if (this.side != null) {
         nbt.put("side", NBTUtilBC.writeEnum(this.side));
      }

      nbt.put("tried", writeEnumSet(this.tried, Direction.class));
      if (this.isPhantom) {
         nbt.putBoolean("isPhantom", true);
      }

      return nbt;
   }

   public int getCurrentDelay(long tickNow) {
      long diff = this.tickFinished - tickNow;
      return diff < 0L ? 0 : (int)diff;
   }

   public double getWayThrough(long now) {
      long diff = this.tickFinished - this.tickStarted;
      if (diff <= 0L) {
         return 1.0;
      }

      long nowDiff = now - this.tickStarted;
      return (double)nowDiff / diff;
   }

   public void genTimings(long now, double distance) {
      this.tickStarted = now;
      this.timeToDest = (int)Math.ceil(distance / this.speed);
      this.tickFinished = now + this.timeToDest;
   }

   public boolean canMerge(TravellingItem with) {
      return !this.isPhantom && !with.isPhantom
         ? this.toCenter == with.toCenter
            && this.colour == with.colour
            && this.side == with.side
            && Math.abs(this.tickFinished - with.tickFinished) < 4L
            && this.stack.getMaxStackSize() >= this.stack.getCount() + with.stack.getCount()
            && StackUtil.canMerge(this.stack, with.stack)
         : false;
   }

   public boolean mergeWith(TravellingItem with) {
      if (this.canMerge(with)) {
         this.stack.grow(with.stack.getCount());
         return true;
      } else {
         return false;
      }
   }

   public Vec3 interpolatePosition(Vec3 start, Vec3 end, long tick, float partialTicks) {
      long diff = this.tickFinished - this.tickStarted;
      long nowDiff = tick - this.tickStarted;
      double sinceStart = (float)nowDiff + partialTicks;
      double interpMul = sinceStart / diff;
      double oneMinus = 1.0 - interpMul;
      if (interpMul <= 0.0) {
         return start;
      }

      if (interpMul >= 1.0) {
         return end;
      }

      double x = oneMinus * start.x + interpMul * end.x;
      double y = oneMinus * start.y + interpMul * end.y;
      double z = oneMinus * start.z + interpMul * end.z;
      return new Vec3(x, y, z);
   }

   public Vec3 getRenderPosition(BlockPos pos, long tick, float partialTicks, PipeFlowItems flow) {
      double[] scratch = new double[3];
      this.writeRenderPosition(pos, tick, partialTicks, flow, scratch);
      return new Vec3(scratch[0], scratch[1], scratch[2]);
   }

   public void writeRenderPosition(BlockPos pos, long tick, float partialTicks, PipeFlowItems flow, double[] out) {
      long diff = this.tickFinished - this.tickStarted;
      long afterTick = tick - this.tickStarted;
      float interp = ((float)afterTick + partialTicks) / (float)diff;
      interp = Math.max(0.0F, Math.min(1.0F, interp));
      Vec3 center = Vec3.atCenterOf(pos);
      Vec3 vecSide = this.side == null ? center : VecUtil.offset(center, this.side, flow.getPipeLength(this.side));
      Vec3 vecFrom;
      Vec3 vecTo;
      if (this.toCenter) {
         vecFrom = vecSide;
         vecTo = center;
      } else {
         vecFrom = center;
         vecTo = vecSide;
      }

      double oneMinus = 1.0F - interp;
      out[0] = oneMinus * vecFrom.x + interp * vecTo.x;
      out[1] = oneMinus * vecFrom.y + interp * vecTo.y;
      out[2] = oneMinus * vecFrom.z + interp * vecTo.z;
   }

   public Direction getRenderDirection(long tick, float partialTicks) {
      if (this.toCenter) {
         return this.side == null ? null : this.side.getOpposite();
      } else {
         return this.side;
      }
   }

   public boolean isVisible() {
      return true;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   private static <E extends Enum<E>> ListTag writeEnumSet(EnumSet<E> set, Class<E> clazz) {
      ListTag list = new ListTag();

      for (E value : set) {
         list.add(NBTUtilBC.writeEnum(value));
      }

      return list;
   }

   private static <E extends Enum<E>> EnumSet<E> readEnumSet(Tag tag, Class<E> clazz) {
      EnumSet<E> set = EnumSet.noneOf(clazz);
      if (tag instanceof ListTag listTag) {
         for (int i = 0; i < listTag.size(); i++) {
            E value = NBTUtilBC.readEnum(listTag.get(i), clazz);
            if (value != null) {
               set.add(value);
            }
         }
      }

      return set;
   }
}
