/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import java.util.EnumSet;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.VecUtil;

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
    long tickStarted, tickFinished;
    int timeToDest;
    Direction side;
    EnumSet<Direction> tried = EnumSet.noneOf(Direction.class);
    boolean isPhantom = false;

    public TravellingItem(@Nonnull ItemStack stack) {
        this.stack = stack;
        clientItemLink = () -> ItemStack.EMPTY;
    }

    public TravellingItem(Supplier<ItemStack> clientStackLink, int count) {
        this.clientItemLink = StackUtil.asNonNull(clientStackLink);
        this.stackSize = count;
        this.stack = StackUtil.EMPTY;
    }

    public TravellingItem(CompoundTag nbt, long tickNow) {
        clientItemLink = () -> ItemStack.EMPTY;
        stack = NBTUtilBC.itemStackFromNBT(nbt.getCompoundOrEmpty("stack"));
        if (stack.isEmpty()) {

            CompoundTag stackTag = nbt.getCompoundOrEmpty("stack");
            if (!stackTag.isEmpty()) {

                stack = ItemStack.EMPTY;
            }
        }
        int c = nbt.getByteOr("colour", (byte) 0);
        this.colour = c == 0 ? null : DyeColor.byId(c - 1);
        this.toCenter = nbt.getBooleanOr("toCenter", false);
        this.speed = nbt.getDoubleOr("speed", 0.05);
        if (speed < 0.001) {
            speed = 0.001;
        }
        tickStarted = nbt.getIntOr("tickStarted", 0) + tickNow;
        tickFinished = nbt.getIntOr("tickFinished", 0) + tickNow;
        timeToDest = nbt.getIntOr("timeToDest", 0);

        side = NBTUtilBC.readEnum(nbt.get("side"), Direction.class);
        if (side == null || timeToDest == 0) {
            toCenter = true;
        }
        tried = readEnumSet(nbt.get("tried"), Direction.class);
        isPhantom = nbt.getBooleanOr("isPhantom", false);
    }

    public CompoundTag writeToNbt(long tickNow) {
        CompoundTag nbt = new CompoundTag();
        nbt.put("stack", NBTUtilBC.itemStackToNBT(stack));
        nbt.putByte("colour", (byte) (colour == null ? 0 : colour.getId() + 1));
        nbt.putBoolean("toCenter", toCenter);
        nbt.putDouble("speed", speed);
        nbt.putInt("tickStarted", (int) (tickStarted - tickNow));
        nbt.putInt("tickFinished", (int) (tickFinished - tickNow));
        nbt.putInt("timeToDest", timeToDest);
        if (side != null) {
            nbt.put("side", NBTUtilBC.writeEnum(side));
        }
        nbt.put("tried", writeEnumSet(tried, Direction.class));
        if (isPhantom) {
            nbt.putBoolean("isPhantom", true);
        }
        return nbt;
    }

    public int getCurrentDelay(long tickNow) {
        long diff = tickFinished - tickNow;
        if (diff < 0) {
            return 0;
        } else {
            return (int) diff;
        }
    }

    public double getWayThrough(long now) {
        long diff = tickFinished - tickStarted;
        long nowDiff = now - tickStarted;
        return nowDiff / (double) diff;
    }

    public void genTimings(long now, double distance) {
        tickStarted = now;
        timeToDest = (int) Math.ceil(distance / speed);
        tickFinished = now + timeToDest;
    }

    public boolean canMerge(TravellingItem with) {
        if (isPhantom || with.isPhantom) {
            return false;
        }
        return toCenter == with.toCenter
            && colour == with.colour
            && side == with.side
            && Math.abs(tickFinished - with.tickFinished) < 4
            && stack.getMaxStackSize() >= stack.getCount() + with.stack.getCount()
            && StackUtil.canMerge(stack, with.stack);
    }

    public boolean mergeWith(TravellingItem with) {
        if (canMerge(with)) {
            this.stack.grow(with.stack.getCount());
            return true;
        }
        return false;
    }

    public Vec3 interpolatePosition(Vec3 start, Vec3 end, long tick, float partialTicks) {
        long diff = tickFinished - tickStarted;
        long nowDiff = tick - tickStarted;
        double sinceStart = nowDiff + partialTicks;
        double interpMul = sinceStart / diff;
        double oneMinus = 1 - interpMul;
        if (interpMul <= 0) return start;
        if (interpMul >= 1) return end;

        double x = oneMinus * start.x + interpMul * end.x;
        double y = oneMinus * start.y + interpMul * end.y;
        double z = oneMinus * start.z + interpMul * end.z;
        return new Vec3(x, y, z);
    }

    public Vec3 getRenderPosition(BlockPos pos, long tick, float partialTicks, PipeFlowItems flow) {
        long diff = tickFinished - tickStarted;
        long afterTick = tick - tickStarted;

        float interp = (afterTick + partialTicks) / diff;
        interp = Math.max(0, Math.min(1, interp));

        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 vecSide = side == null ? center : VecUtil.offset(center, side, flow.getPipeLength(side));

        Vec3 vecFrom;
        Vec3 vecTo;
        if (toCenter) {
            vecFrom = vecSide;
            vecTo = center;
        } else {
            vecFrom = center;
            vecTo = vecSide;
        }

        return VecUtil.scale(vecFrom, 1 - interp).add(VecUtil.scale(vecTo, interp));
    }

    public Direction getRenderDirection(long tick, float partialTicks) {
        if (toCenter) {
            return side == null ? null : side.getOpposite();
        } else {
            return side;
        }
    }

    public boolean isVisible() {
        return true;
    }

    public ItemStack getStack() {
        return stack;
    }

    private static <E extends Enum<E>> net.minecraft.nbt.ListTag writeEnumSet(EnumSet<E> set, Class<E> clazz) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (E value : set) {
            list.add(NBTUtilBC.writeEnum(value));
        }
        return list;
    }

    private static <E extends Enum<E>> EnumSet<E> readEnumSet(net.minecraft.nbt.Tag tag, Class<E> clazz) {
        EnumSet<E> set = EnumSet.noneOf(clazz);
        if (tag instanceof net.minecraft.nbt.ListTag listTag) {
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
