/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;

public class Box implements IBox {

    public Object[] laserData;

    public BlockPos lastMin, lastMax;

    private BlockPos min, max;

    public Box() {
        reset();
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
        min = null;
        max = null;
    }

    public boolean isInitialized() {
        return min != null && max != null;
    }

    public void extendToEncompassBoth(BlockPos newMin, BlockPos newMax) {
        this.min = VecUtil.min(this.min, newMin, newMax);
        this.max = VecUtil.max(this.max, newMin, newMax);
    }

    public void setMin(BlockPos min) {
        if (min == null) return;
        this.min = min;
        this.max = VecUtil.max(min, max);
    }

    public void setMax(BlockPos max) {
        if (max == null) return;
        this.min = VecUtil.min(min, max);
        this.max = max;
    }

    public void initialize(IBox box) {
        reset();
        extendToEncompassBoth(box.min(), box.max());
    }

    public void initialize(IAreaProvider a) {
        reset();
        extendToEncompassBoth(a.min(), a.max());
    }

    public void initialize(CompoundTag nbt) {
        reset();
        if (nbt.contains("xMin")) {
            min = new BlockPos(nbt.getInt("xMin").orElse(0), nbt.getInt("yMin").orElse(0), nbt.getInt("zMin").orElse(0));
            max = new BlockPos(nbt.getInt("xMax").orElse(0), nbt.getInt("yMax").orElse(0), nbt.getInt("zMax").orElse(0));
        } else {
            if (nbt.contains("min")) {
                CompoundTag minTag = nbt.getCompound("min").orElse(new CompoundTag());
                min = new BlockPos(minTag.getInt("X").orElse(0), minTag.getInt("Y").orElse(0), minTag.getInt("Z").orElse(0));
            }
            if (nbt.contains("max")) {
                CompoundTag maxTag = nbt.getCompound("max").orElse(new CompoundTag());
                max = new BlockPos(maxTag.getInt("X").orElse(0), maxTag.getInt("Y").orElse(0), maxTag.getInt("Z").orElse(0));
            }
        }
        if (min != null && max != null) {
            extendToEncompassBoth(min, max);
        }
    }

    public void writeToNBT(CompoundTag nbt) {
        if (min != null) {
            CompoundTag minTag = new CompoundTag();
            minTag.putInt("X", min.getX());
            minTag.putInt("Y", min.getY());
            minTag.putInt("Z", min.getZ());
            nbt.put("min", minTag);
        }
        if (max != null) {
            CompoundTag maxTag = new CompoundTag();
            maxTag.putInt("X", max.getX());
            maxTag.putInt("Y", max.getY());
            maxTag.putInt("Z", max.getZ());
            nbt.put("max", maxTag);
        }
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        writeToNBT(nbt);
        return nbt;
    }

    public void initializeCenter(BlockPos center, int size) {
        initializeCenter(center, new Vec3i(size, size, size));
    }

    public void initializeCenter(BlockPos center, Vec3i size) {
        extendToEncompassBoth(center.subtract(size), center.offset(size));
    }

    public List<BlockPos> getBlocksInArea() {
        List<BlockPos> blocks = new ArrayList<>();
        if (min != null && max != null) {
            for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                blocks.add(pos.immutable());
            }
        }
        return blocks;
    }

    public List<BlockPos> getBlocksOnEdge() {
        return PositionUtil.getAllOnEdge(min, max);
    }

    @Override
    public Box expand(int amount) {
        if (!isInitialized()) return this;
        Vec3i am = new Vec3i(amount, amount, amount);
        setMin(min().subtract(am));
        setMax(max().offset(am));
        return this;
    }

    @Override
    public IBox contract(int amount) {
        return expand(-amount);
    }

    @Override
    public boolean contains(Vec3 p) {
        AABB bb = getBoundingBox();
        if (p.x < bb.minX || p.x >= bb.maxX) return false;
        if (p.y < bb.minY || p.y >= bb.maxY) return false;
        if (p.z < bb.minZ || p.z >= bb.maxZ) return false;
        return true;
    }

    public boolean contains(BlockPos i) {
        return contains(new Vec3(i.getX(), i.getY(), i.getZ()));
    }

    @Override
    public BlockPos min() {
        return min;
    }

    @Override
    public BlockPos max() {
        return max;
    }

    @Override
    public BlockPos size() {
        if (!isInitialized()) return BlockPos.ZERO;
        return max.subtract(min).offset(1, 1, 1);
    }

    public BlockPos center() {
        return BlockPos.containing(centerExact());
    }

    public Vec3 centerExact() {
        return new Vec3(size().getX(), size().getY(), size().getZ()).scale(0.5).add(new Vec3(min().getX(), min().getY(), min().getZ()));
    }

    @Override
    public String toString() {
        return "Box[min = " + min + ", max = " + max + "]";
    }

    public Box extendToEncompass(IBox toBeContained) {
        if (toBeContained == null) {
            return this;
        }
        extendToEncompassBoth(toBeContained.min(), toBeContained.max());
        return this;
    }

    public AABB getBoundingBox() {
        if (min == null || max == null) {
           return new AABB(0, 0, 0, 0, 0, 0);
        }
        return new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0);
    }

    public Box extendToEncompass(Vec3 toBeContained) {
        setMin(VecUtil.min(min, VecUtil.convertFloor(toBeContained)));
        setMax(VecUtil.max(max, VecUtil.convertCeiling(toBeContained)));
        return this;
    }

    public Box extendToEncompass(BlockPos toBeContained) {
        setMin(VecUtil.min(min, toBeContained));
        setMax(VecUtil.max(max, toBeContained));
        return this;
    }

    @Override
    public double distanceTo(BlockPos index) {
        return Math.sqrt(distanceToSquared(index));
    }

    @Override
    public double distanceToSquared(BlockPos index) {
        return closestInsideTo(index).distSqr(index);
    }

    public BlockPos closestInsideTo(BlockPos toTest) {
        return VecUtil.max(min, VecUtil.min(max, toTest));
    }

    @Override
    public BlockPos getRandomBlockPos(Random rand) {
        return PositionUtil.randomBlockPos(rand, min, max.offset(1, 1, 1));
    }

    public boolean isCorner(BlockPos pos) {
        return PositionUtil.isCorner(min, max, pos);
    }

    public boolean isOnEdge(BlockPos pos) {
        return PositionUtil.isOnEdge(min, max, pos);
    }

    public boolean isOnFace(BlockPos pos) {
        return PositionUtil.isOnFace(min, max, pos);
    }

    public boolean doesIntersectWith(Box box) {
        if (isInitialized() && box.isInitialized()) {
            return min.getX() <= box.max.getX() && max.getX() >= box.min.getX()
                && min.getY() <= box.max.getY() && max.getY() >= box.min.getY()
                && min.getZ() <= box.max.getZ() && max.getZ() >= box.min.getZ();
        }
        return false;
    }

    @Nullable
    public Box getIntersect(Box box) {
        if (doesIntersectWith(box)) {
            BlockPos min2 = VecUtil.max(min, box.min);
            BlockPos max2 = VecUtil.min(max, box.max);
            return new Box(min2, max2);
        }
        return null;
    }

    public int getBlocksOnEdgeCount() {
        return PositionUtil.getCountOnEdge(min(), max());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Box box = (Box) obj;
        if (!Objects.equal(min, box.min)) return false;
        if (!Objects.equal(max, box.max)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(min, max);
    }
}
