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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;

public class BlockWaterGel extends Block {
   public static final MapCodec<BlockWaterGel> CODEC = simpleCodec(BlockWaterGel::new);
   public static final EnumProperty<BlockWaterGel.GelStage> PROP_STAGE = EnumProperty.create("stage", BlockWaterGel.GelStage.class);

   public BlockWaterGel(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(PROP_STAGE, BlockWaterGel.GelStage.SPREAD_0));
   }

   protected MapCodec<? extends Block> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{PROP_STAGE});
   }

   protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      BlockWaterGel.GelStage stage = (BlockWaterGel.GelStage)state.getValue(PROP_STAGE);
      BlockWaterGel.GelStage next = stage.next();
      BlockState nextState = (BlockState)state.setValue(PROP_STAGE, next);
      if (stage.spreading) {
         Deque<BlockPos> openQueue = new ArrayDeque<>();
         Set<BlockPos> seenSet = new HashSet<>();
         List<BlockPos> changeable = new ArrayList<>();
         List<Direction> faces = new ArrayList<>();
         Collections.addAll(faces, Direction.values());
         Collections.shuffle(faces);
         seenSet.add(pos);

         // Mark the seeds as seen up front — otherwise a sibling's expansion can re-queue them and the same
         // position gets processed (and potentially converted) twice. The per-expansion shuffle below keeps
         // the spread direction random.
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
            // Converting someone's water must be attributable: gate each target through the machine-break
            // event with the seeding player's profile (claim mods listen to it natively). Denied targets are
            // skipped, so the gel stops at a claim border and keeps gelling on its own side; ownerless gel
            // (legacy saves) fails closed and only hardens in place.
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
         // No longer spreading: the ownership entry (if any survived, e.g. legacy data) is no longer needed.
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

   /** Whether the gel should treat this position as water it can convert: plain water, or a no-collision block that
    * merely holds a water source — the water plants (seagrass, tall seagrass, kelp, kelp_plant, ...). Solid
    * waterlogged blocks (fences, slabs, stairs, ...) keep their collision and are deliberately left intact. */
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

   public SoundType getSoundType(BlockState state) {
      BlockWaterGel.GelStage stage = (BlockWaterGel.GelStage)state.getValue(PROP_STAGE);
      return stage.soundType;
   }

   public float defaultDestroyTime() {
      return 0.6F;
   }

   public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      BlockWaterGel.GelStage stage = (BlockWaterGel.GelStage)state.getValue(PROP_STAGE);
      float hardness = stage.hardness;
      if (hardness < 0.0F) {
         return 0.0F;
      }

      float speed = player.getDestroySpeed(state);
      boolean canHarvest = player.hasCorrectToolForDrops(state);
      return canHarvest ? speed / hardness / 30.0F : speed / hardness / 100.0F;
   }

   protected List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      BlockWaterGel.GelStage stage = (BlockWaterGel.GelStage)state.getValue(PROP_STAGE);
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
