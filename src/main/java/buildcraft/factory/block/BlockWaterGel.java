/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import buildcraft.factory.BCFactoryItems;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.SoundUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;

// The vanilla fluid renderer special-cases HalfTransparentBlock by instanceof: water touching one draws the
// clean flat OVERLAY texture on the shared face instead of the flowing texture fighting the gel's own face.
public class BlockWaterGel extends HalfTransparentBlock {
   //? if < 26.3-pre-1 {
   /*public static final MapCodec<BlockWaterGel> CODEC = simpleCodec(BlockWaterGel::new);
   *///?}
   public static final EnumProperty<BlockWaterGel.GelStage> PROP_STAGE = EnumProperty.create("stage", BlockWaterGel.GelStage.class);

   public BlockWaterGel(Properties properties) {
      super(properties);
      this.registerDefaultState((this.stateDefinition.any()).setValue(PROP_STAGE, BlockWaterGel.GelStage.SPREAD_0));
   }

   //? if < 26.3-pre-1 {
   /*protected MapCodec<? extends BlockWaterGel> codec() {
      return CODEC;
   }
   *///?}

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{PROP_STAGE});
   }

   @Override
   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      BlockWaterGel.GelStage stage = state.getValue(PROP_STAGE);
      BlockWaterGel.GelStage next = stage.next();
      BlockState nextState = state.setValue(PROP_STAGE, next);
      if (stage.spreading) {
         Deque<BlockPos> openQueue = new ArrayDeque<>();
         Set<BlockPos> seenSet = new HashSet<>();
         List<BlockPos> changeable = new ArrayList<>();
         List<Direction> faces = new ArrayList<>();
         Collections.addAll(faces, Direction.values());
         Collections.shuffle(faces);
         seenSet.add(pos);

         // Mark the seeds as seen up front, otherwise a sibling's expansion can re-queue and convert them twice.
         for (Direction face : faces) {
            BlockPos seed = pos.relative(face);
            if (seenSet.add(seed)) {
               openQueue.add(seed);
            }
         }

         for (int tries = 0; openQueue.size() > 0 && changeable.size() < 3 && tries < 10000; tries++) {
            BlockPos test = openQueue.removeFirst();
            boolean water = isGellableWater(level, test);
            boolean spreadable = water || this.canSpread(level, test);
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
         }

         int time = next.spreading ? 200 : 400;
         if (changeable.size() == 3 || level.getRandom().nextDouble() < 0.5) {
            // Gate each target through the machine-break event with the seeding player's profile so claim mods
            // can deny it; denied targets are skipped and ownerless gel fails closed, only hardening in place.
            GelOwnerSavedData ownersData = GelOwnerSavedData.getOrCreate(level);
            GameProfile gelOwner = ownersData.getOwner(pos);

            for (BlockPos p : changeable) {
               if (BlockUtil.canMachineBreak(level, p, gelOwner)) {
                  level.setBlockAndUpdate(p, nextState);
                  ownersData.setOwner(p, gelOwner);
                  level.scheduleTick(p, this, rand.nextInt(150) + time);
               }
            }

            level.setBlockAndUpdate(pos, nextState);
            SoundUtil.playBlockPlace(level, pos);
            if (!next.spreading) {
               ownersData.removeOwner(pos);
            }
         }

         level.scheduleTick(pos, this, rand.nextInt(150) + time);
      } else if (stage != next) {
         GelOwnerSavedData.getOrCreate(level).removeOwner(pos);
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
         if (isGellableWater(level, pos.relative(face))) {
            return false;
         }
      }

      return true;
   }

   /** Convertible water is plain water or a no-collision water-holding plant; solid waterlogged blocks keep collision and are left intact. */
   public static boolean isGellableWater(BlockGetter level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (state.is(Blocks.WATER)) {
         return true;
      }

      return state.getFluidState().isSourceOfType(Fluids.WATER) && state.getCollisionShape(level, pos).isEmpty();
   }

   private boolean canSpread(Level level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return state.is(this);
   }

   @Override
   public SoundType getSoundType(BlockState state) {
      BlockWaterGel.GelStage stage = state.getValue(PROP_STAGE);
      return stage.soundType;
   }

   @Override
   public float defaultDestroyTime() {
      return 0.6F;
   }

   @Override
   public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      BlockWaterGel.GelStage stage = state.getValue(PROP_STAGE);
      float hardness = stage.hardness;
      if (hardness < 0.0F) {
         return 0.0F;
      }

      float speed = player.getDestroySpeed(state);
      boolean canHarvest = player.hasCorrectToolForDrops(state);
      return canHarvest ? speed / hardness / 30.0F : speed / hardness / 100.0F;
   }

   @Override
   protected List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      BlockWaterGel.GelStage stage = state.getValue(PROP_STAGE);
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

   public enum GelStage implements StringRepresentable {
      SPREAD_0(0.3F, true, 3.0F),
      SPREAD_1(0.4F, true, 3.0F),
      SPREAD_2(0.6F, true, 3.0F),
      SPREAD_3(0.8F, true, 3.0F),
      GELLING_0(1.0F, false, 0.6F),
      GELLING_1(1.2F, false, 0.6F),
      GEL(1.5F, false, 0.1F);

      public static final BlockWaterGel.GelStage[] VALUES = values();
      public final SoundType soundType;
      public final String modelName = this.name().toLowerCase(Locale.ROOT);
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
         return this.modelName;
      }

      public static BlockWaterGel.GelStage fromOrdinal(int ordinal) {
         return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : GEL;
      }

      public BlockWaterGel.GelStage next() {
         if (this == SPREAD_0) {
            return SPREAD_1;
         } else if (this == SPREAD_1) {
            return SPREAD_2;
         } else if (this == SPREAD_2) {
            return SPREAD_3;
         } else if (this == SPREAD_3) {
            return GELLING_0;
         } else {
            return this == GELLING_0 ? GELLING_1 : GEL;
         }
      }
   }
}
