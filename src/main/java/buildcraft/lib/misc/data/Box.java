/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;
import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Box implements IBox {
   public Object[] laserData;
   public BlockPos lastMin;
   public BlockPos lastMax;
   private BlockPos min;
   private BlockPos max;

   public Box() {
      this.reset();
   }

   public Box(BlockPos min, BlockPos max) {
      this();
      this.min = VecUtil.min(min, max);
      this.max = VecUtil.max(min, max);
   }

   public Box(BlockEntity e) {
      this(e.getBlockPos(), e.getBlockPos());
   }

   public final void reset() {
      this.min = null;
      this.max = null;
   }

   public boolean isInitialized() {
      return this.min != null && this.max != null;
   }

   public void extendToEncompassBoth(BlockPos newMin, BlockPos newMax) {
      this.min = VecUtil.min(this.min, newMin, newMax);
      this.max = VecUtil.max(this.max, newMin, newMax);
   }

   public void setMin(BlockPos min) {
      if (min != null) {
         this.min = min;
         this.max = VecUtil.max(min, this.max);
      }
   }

   public void setMax(BlockPos max) {
      if (max != null) {
         this.min = VecUtil.min(this.min, max);
         this.max = max;
      }
   }

   public void initialize(IBox box) {
      this.reset();
      this.extendToEncompassBoth(box.min(), box.max());
   }

   public void initialize(IAreaProvider a) {
      this.reset();
      this.extendToEncompassBoth(a.min(), a.max());
   }

   public void initialize(CompoundTag nbt) {
      this.reset();
      if (nbt.contains("xMin")) {
         this.min = new BlockPos(nbt.getInt("xMin").orElse(0), nbt.getInt("yMin").orElse(0), nbt.getInt("zMin").orElse(0));
         this.max = new BlockPos(nbt.getInt("xMax").orElse(0), nbt.getInt("yMax").orElse(0), nbt.getInt("zMax").orElse(0));
      } else {
         if (nbt.contains("min")) {
            CompoundTag minTag = nbt.getCompound("min").orElse(new CompoundTag());
            this.min = new BlockPos(minTag.getInt("X").orElse(0), minTag.getInt("Y").orElse(0), minTag.getInt("Z").orElse(0));
         }

         if (nbt.contains("max")) {
            CompoundTag maxTag = nbt.getCompound("max").orElse(new CompoundTag());
            this.max = new BlockPos(maxTag.getInt("X").orElse(0), maxTag.getInt("Y").orElse(0), maxTag.getInt("Z").orElse(0));
         }
      }

      if (this.min != null && this.max != null) {
         this.extendToEncompassBoth(this.min, this.max);
      }
   }

   public void writeToNBT(CompoundTag nbt) {
      if (this.min != null) {
         CompoundTag minTag = new CompoundTag();
         minTag.putInt("X", this.min.getX());
         minTag.putInt("Y", this.min.getY());
         minTag.putInt("Z", this.min.getZ());
         nbt.put("min", minTag);
      }

      if (this.max != null) {
         CompoundTag maxTag = new CompoundTag();
         maxTag.putInt("X", this.max.getX());
         maxTag.putInt("Y", this.max.getY());
         maxTag.putInt("Z", this.max.getZ());
         nbt.put("max", maxTag);
      }
   }

   public CompoundTag writeToNBT() {
      CompoundTag nbt = new CompoundTag();
      this.writeToNBT(nbt);
      return nbt;
   }

   public void initializeCenter(BlockPos center, int size) {
      this.initializeCenter(center, new Vec3i(size, size, size));
   }

   public void initializeCenter(BlockPos center, Vec3i size) {
      this.extendToEncompassBoth(center.subtract(size), center.offset(size));
   }

   public List<BlockPos> getBlocksInArea() {
      List<BlockPos> blocks = new ArrayList<>();
      if (this.min != null && this.max != null) {
         for (BlockPos pos : BlockPos.betweenClosed(this.min, this.max)) {
            blocks.add(pos.immutable());
         }
      }

      return blocks;
   }

   public List<BlockPos> getBlocksOnEdge() {
      return PositionUtil.getAllOnEdge(this.min, this.max);
   }

   public Box expand(int amount) {
      if (!this.isInitialized()) {
         return this;
      }

      Vec3i am = new Vec3i(amount, amount, amount);
      this.setMin(this.min().subtract(am));
      this.setMax(this.max().offset(am));
      return this;
   }

   @Override
   public IBox contract(int amount) {
      return this.expand(-amount);
   }

   @Override
   public boolean contains(Vec3 p) {
      AABB bb = this.getBoundingBox();
      if (p.x < bb.minX || p.x >= bb.maxX) {
         return false;
      } else {
         return p.y < bb.minY || p.y >= bb.maxY ? false : !(p.z < bb.minZ) && !(p.z >= bb.maxZ);
      }
   }

   public boolean contains(BlockPos i) {
      return this.contains(new Vec3(i.getX(), i.getY(), i.getZ()));
   }

   @Override
   public BlockPos min() {
      return this.min;
   }

   @Override
   public BlockPos max() {
      return this.max;
   }

   @Override
   public BlockPos size() {
      return !this.isInitialized() ? BlockPos.ZERO : this.max.subtract(this.min).offset(1, 1, 1);
   }

   public BlockPos center() {
      return BlockPos.containing(this.centerExact());
   }

   public Vec3 centerExact() {
      return new Vec3(this.size().getX(), this.size().getY(), this.size().getZ())
         .scale(0.5)
         .add(new Vec3(this.min().getX(), this.min().getY(), this.min().getZ()));
   }

   @Override
   public String toString() {
      return "Box[min = " + this.min + ", max = " + this.max + "]";
   }

   public Box extendToEncompass(IBox toBeContained) {
      if (toBeContained == null) {
         return this;
      }

      this.extendToEncompassBoth(toBeContained.min(), toBeContained.max());
      return this;
   }

   public AABB getBoundingBox() {
      return this.min != null && this.max != null
         ? new AABB(this.min.getX(), this.min.getY(), this.min.getZ(), this.max.getX() + 1.0, this.max.getY() + 1.0, this.max.getZ() + 1.0)
         : new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   }

   public Box extendToEncompass(Vec3 toBeContained) {
      this.setMin(VecUtil.min(this.min, VecUtil.convertFloor(toBeContained)));
      this.setMax(VecUtil.max(this.max, VecUtil.convertCeiling(toBeContained)));
      return this;
   }

   public Box extendToEncompass(BlockPos toBeContained) {
      this.setMin(VecUtil.min(this.min, toBeContained));
      this.setMax(VecUtil.max(this.max, toBeContained));
      return this;
   }

   @Override
   public double distanceTo(BlockPos index) {
      return Math.sqrt(this.distanceToSquared(index));
   }

   @Override
   public double distanceToSquared(BlockPos index) {
      return this.closestInsideTo(index).distSqr(index);
   }

   public BlockPos closestInsideTo(BlockPos toTest) {
      return VecUtil.max(this.min, VecUtil.min(this.max, toTest));
   }

   @Override
   public BlockPos getRandomBlockPos(Random rand) {
      return PositionUtil.randomBlockPos(rand, this.min, this.max.offset(1, 1, 1));
   }

   public boolean isCorner(BlockPos pos) {
      return PositionUtil.isCorner(this.min, this.max, pos);
   }

   public boolean isOnEdge(BlockPos pos) {
      return PositionUtil.isOnEdge(this.min, this.max, pos);
   }

   public boolean isOnFace(BlockPos pos) {
      return PositionUtil.isOnFace(this.min, this.max, pos);
   }

   public boolean doesIntersectWith(Box box) {
      return this.isInitialized() && box.isInitialized()
         ? this.min.getX() <= box.max.getX()
            && this.max.getX() >= box.min.getX()
            && this.min.getY() <= box.max.getY()
            && this.max.getY() >= box.min.getY()
            && this.min.getZ() <= box.max.getZ()
            && this.max.getZ() >= box.min.getZ()
         : false;
   }

   @Nullable
   public Box getIntersect(Box box) {
      if (this.doesIntersectWith(box)) {
         BlockPos min2 = VecUtil.max(this.min, box.min);
         BlockPos max2 = VecUtil.min(this.max, box.max);
         return new Box(min2, max2);
      } else {
         return null;
      }
   }

   public int getBlocksOnEdgeCount() {
      return PositionUtil.getCountOnEdge(this.min(), this.max());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      Box box = (Box)obj;
      return !Objects.equal(this.min, box.min) ? false : Objects.equal(this.max, box.max);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(new Object[]{this.min, this.max});
   }
}
