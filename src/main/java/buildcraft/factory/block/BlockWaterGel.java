/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import buildcraft.lib.misc.SoundUtil;
import buildcraft.factory.BCFactoryItems;

@SuppressWarnings({"this-escape", "deprecation"})
public class BlockWaterGel extends Block {
    public static final MapCodec<BlockWaterGel> CODEC = simpleCodec(BlockWaterGel::new);

    public enum GelStage implements StringRepresentable {
        SPREAD_0(0.3f, true, 3f),
        SPREAD_1(0.4f, true, 3f),
        SPREAD_2(0.6f, true, 3f),
        SPREAD_3(0.8f, true, 3f),
        GELLING_0(1.0f, false, 0.6f),
        GELLING_1(1.2f, false, 0.6f),
        GEL(1.5f, false, 0.1f);

        public static final GelStage[] VALUES = values();

        public final SoundType soundType;
        public final String modelName = name().toLowerCase(Locale.ROOT);
        public final boolean spreading;
        public final float hardness;

        GelStage(float pitch, boolean spreading, float hardness) {
            this.soundType = new SoundType(
                SoundType.SLIME_BLOCK.volume,
                pitch,
                SoundEvents.SLIME_BLOCK_BREAK,
                SoundEvents.SLIME_BLOCK_STEP,
                SoundEvents.SLIME_BLOCK_PLACE,
                SoundEvents.SLIME_BLOCK_HIT,
                SoundEvents.SLIME_BLOCK_FALL
            );
            this.spreading = spreading;
            this.hardness = hardness;
        }

        @Override
        public String getSerializedName() {
            return modelName;
        }

        public static GelStage fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= VALUES.length) {
                return GEL;
            }
            return VALUES[ordinal];
        }

        public GelStage next() {
            if (this == SPREAD_0) return SPREAD_1;
            if (this == SPREAD_1) return SPREAD_2;
            if (this == SPREAD_2) return SPREAD_3;
            if (this == SPREAD_3) return GELLING_0;
            if (this == GELLING_0) return GELLING_1;
            return GEL;
        }
    }

    public static final EnumProperty<GelStage> PROP_STAGE = EnumProperty.create("stage", GelStage.class);

    public BlockWaterGel(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PROP_STAGE, GelStage.SPREAD_0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROP_STAGE);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        GelStage stage = state.getValue(PROP_STAGE);
        GelStage next = stage.next();
        BlockState nextState = state.setValue(PROP_STAGE, next);

        if (stage.spreading) {
            Deque<BlockPos> openQueue = new ArrayDeque<>();
            Set<BlockPos> seenSet = new HashSet<>();
            List<BlockPos> changeable = new ArrayList<>();
            List<Direction> faces = new ArrayList<>();
            Collections.addAll(faces, Direction.values());
            Collections.shuffle(faces);
            seenSet.add(pos);
            for (Direction face : faces) {
                openQueue.add(pos.relative(face));
            }
            Collections.shuffle(faces);
            int tries = 0;

            while (openQueue.size() > 0 && changeable.size() < 3 && tries < 10_000) {
                BlockPos test = openQueue.removeFirst();

                boolean water = isWater(level, test);
                boolean spreadable = water || canSpread(level, test);

                if (water && level.getFluidState(test).isSource()) {
                    changeable.add(test);
                }
                if (spreadable) {
                    Collections.shuffle(faces);
                    for (Direction face : faces) {
                        BlockPos n = test.relative(face);
                        if (seenSet.add(n)) {
                            openQueue.add(n);
                        }
                    }
                }
                tries++;
            }
            final int time = next.spreading ? 200 : 400;
            if (changeable.size() == 3 || level.getRandom().nextDouble() < 0.5) {
                for (BlockPos p : changeable) {
                    level.setBlockAndUpdate(p, nextState);
                    level.scheduleTick(p, this, rand.nextInt(150) + time);
                }
                level.setBlockAndUpdate(pos, nextState);
                SoundUtil.playBlockPlace(level, pos);
            }
            level.scheduleTick(pos, this, rand.nextInt(150) + time);
        } else if (stage != next) {
            if (notTouchingWater(level, pos)) {
                level.setBlockAndUpdate(pos, nextState);
                level.scheduleTick(pos, this, rand.nextInt(150) + 400);
            } else {
                level.scheduleTick(pos, this, rand.nextInt(150) + 600);
            }
        }
    }

    private static boolean notTouchingWater(Level level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            if (isWater(level, pos.relative(face))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWater(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.WATER);
    }

    private boolean canSpread(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(this);
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        GelStage stage = state.getValue(PROP_STAGE);
        return stage.soundType;
    }

    @Override
    public float defaultDestroyTime() {

        return 0.6f;
    }

    @Override
    public float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player, BlockGetter level, BlockPos pos) {
        GelStage stage = state.getValue(PROP_STAGE);
        float hardness = stage.hardness;
        if (hardness < 0) {
            return 0.0F;
        }

        float speed = player.getDestroySpeed(state);
        boolean canHarvest = player.hasCorrectToolForDrops(state);
        if (canHarvest) {
            return speed / hardness / 30.0F;
        } else {
            return speed / hardness / 100.0F;
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        GelStage stage = state.getValue(PROP_STAGE);
        RandomSource rand = builder.getLevel().getRandom();
        int count;
        if (stage.spreading) {
            count = rand.nextInt(2) + 1;
        } else {
            count = 1;
        }
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(BCFactoryItems.GELLED_WATER, count));
        return drops;
    }
}
