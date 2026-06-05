/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import buildcraft.lib.client.render.laser.LaserData_BC8;

public class Lock {
    public Cause cause;
    public List<Target> targets = new ArrayList<>();

    public Lock() {
    }

    public Lock(Cause cause, Target... targets) {
        this.cause = cause;
        this.targets.addAll(Arrays.asList(targets));
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        CompoundTag causeTag = new CompoundTag();
        causeTag.putString("type", Cause.EnumCause.getForClass(cause.getClass()).name());
        causeTag.put("data", cause.writeToNBT(new CompoundTag()));
        nbt.put("cause", causeTag);

        ListTag targetsList = new ListTag();
        for (Target target : targets) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.putString("type", Target.EnumTarget.getForClass(target.getClass()).name());
            targetTag.put("data", target.writeToNBT(new CompoundTag()));
            targetsList.add(targetTag);
        }
        nbt.put("targets", targetsList);
        return nbt;
    }

    public void readFromNBT(CompoundTag nbt) {
        CompoundTag causeTag = nbt.getCompound("cause").orElseGet(CompoundTag::new);
        String causeType = causeTag.getString("type").orElse("BLOCK");
        try {
            cause = Cause.EnumCause.valueOf(causeType).supplier.get();
            cause.readFromNBT(causeTag.getCompound("data").orElseGet(CompoundTag::new));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown lock cause type: " + causeType, e);
        }

        if (nbt.contains("targets")) {
            ListTag targetsList = nbt.getList("targets").orElseGet(ListTag::new);
            for (int i = 0; i < targetsList.size(); i++) {
                CompoundTag targetTag = targetsList.getCompound(i).orElseGet(CompoundTag::new);
                String targetType = targetTag.getString("type").orElse("REMOVE");
                try {
                    Target target = Target.EnumTarget.valueOf(targetType).supplier.get();
                    target.readFromNBT(targetTag.getCompound("data").orElseGet(CompoundTag::new));
                    targets.add(target);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown lock target type: " + targetType, e);
                }
            }
        }
    }

    public static abstract class Cause {
        public abstract CompoundTag writeToNBT(CompoundTag nbt);

        public abstract void readFromNBT(CompoundTag nbt);

        public abstract boolean stillWorks(Level world);

        public static class CauseBlock extends Cause {
            public BlockPos pos;
            public net.minecraft.world.level.block.Block block;

            public CauseBlock() {
            }

            public CauseBlock(BlockPos pos, net.minecraft.world.level.block.Block block) {
                this.pos = pos;
                this.block = block;
            }

            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                if (pos != null) {
                    CompoundTag posTag = new CompoundTag();
                    posTag.putInt("X", pos.getX());
                    posTag.putInt("Y", pos.getY());
                    posTag.putInt("Z", pos.getZ());
                    nbt.put("pos", posTag);
                }
                nbt.putString("block",
                        net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString());
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                if (nbt.contains("pos")) {
                    CompoundTag posTag = nbt.getCompound("pos").orElseGet(CompoundTag::new);
                    pos = new BlockPos(posTag.getInt("X").orElse(0), posTag.getInt("Y").orElse(0),
                            posTag.getInt("Z").orElse(0));
                }
                String blockKey = nbt.getString("block").orElse("minecraft:air");
                block = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getValue(Identifier.parse(blockKey));
            }

            @Override
            public boolean stillWorks(Level world) {
                return world.getBlockState(pos).getBlock() == block;
            }
        }

        enum EnumCause {
            BLOCK(CauseBlock::new);

            public final Supplier<? extends Cause> supplier;

            EnumCause(Supplier<? extends Cause> supplier) {
                this.supplier = supplier;
            }

            public static EnumCause getForClass(Class<? extends Cause> clazz) {
                return Arrays.stream(values())
                        .filter(enumCause -> enumCause.supplier.get().getClass() == clazz)
                        .findFirst()
                        .orElse(null);
            }
        }
    }

    public static abstract class Target {
        public abstract CompoundTag writeToNBT(CompoundTag nbt);

        public abstract void readFromNBT(CompoundTag nbt);

        public static class TargetRemove extends Target {
            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
            }
        }

        public static class TargetResize extends Target {
            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
            }
        }

        public static class TargetAddon extends Target {
            public EnumAddonSlot slot;

            public TargetAddon() {
            }

            public TargetAddon(EnumAddonSlot slot) {
                this.slot = slot;
            }

            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                nbt.putString("slot", slot.name());
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                slot = EnumAddonSlot.valueOf(nbt.getString("slot").orElse(""));
            }
        }

        public static class TargetUsedByMachine extends Target {
            public EnumType type;

            public TargetUsedByMachine() {
            }

            public TargetUsedByMachine(EnumType type) {
                this.type = type;
            }

            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                nbt.putString("type", type.name());
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                type = EnumType.valueOf(nbt.getString("type").orElse("STRIPES_WRITE"));
            }

            public enum EnumType {
                STRIPES_WRITE {
                    @Override
                    public buildcraft.lib.client.render.laser.LaserData_BC8.LaserType getLaserType() {
                        return buildcraft.core.client.BuildCraftLaserManager.STRIPES_WRITE;
                    }
                },
                STRIPES_READ {
                    @Override
                    public buildcraft.lib.client.render.laser.LaserData_BC8.LaserType getLaserType() {
                        return buildcraft.core.client.BuildCraftLaserManager.STRIPES_READ;
                    }
                };

                public abstract buildcraft.lib.client.render.laser.LaserData_BC8.LaserType getLaserType();
            }
        }

        enum EnumTarget {
            REMOVE(TargetRemove::new),
            RESIZE(TargetResize::new),
            ADDON(TargetAddon::new),
            USED_BY_MACHINE(TargetUsedByMachine::new);

            public final Supplier<? extends Target> supplier;

            EnumTarget(Supplier<? extends Target> supplier) {
                this.supplier = supplier;
            }

            public static EnumTarget getForClass(Class<? extends Target> clazz) {
                return Arrays.stream(values())
                        .filter(enumTarget -> enumTarget.supplier.get().getClass() == clazz)
                        .findFirst()
                        .orElse(null);
            }
        }
    }
}
