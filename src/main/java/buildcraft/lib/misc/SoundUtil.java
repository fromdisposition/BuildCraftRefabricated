package buildcraft.lib.misc;

import buildcraft.lib.common.SoundActions;
import buildcraft.lib.fluids.FluidStack;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class SoundUtil {
   public static void playBlockPlace(Level world, BlockPos pos) {
      playBlockPlace(world, pos, world.getBlockState(pos));
   }

   public static void playBlockPlace(Level world, BlockPos pos, BlockState state) {
      SoundType soundType = state.getSoundType();
      float volume = (soundType.getVolume() + 1.0F) / 2.0F;
      float pitch = soundType.getPitch() * 0.8F;
      world.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, volume, pitch);
   }

   public static void playSlideSound(Level level, BlockPos pos, BlockState state, InteractionResult result) {
      if (result != InteractionResult.PASS) {
         SoundType soundType = state.getSoundType();
         SoundEvent event;
         if (result != InteractionResult.SUCCESS && result != InteractionResult.CONSUME) {
            event = SoundEvents.PISTON_EXTEND;
         } else {
            event = SoundEvents.PISTON_CONTRACT;
         }

         float volume = (soundType.getVolume() + 1.0F) / 2.0F;
         float pitch = soundType.getPitch() * 0.8F;
         level.playSound(null, pos, event, SoundSource.BLOCKS, volume, pitch);
      }
   }

   public static void playChangeColour(Level level, BlockPos pos, @Nullable DyeColor colour) {
      SoundType soundType = SoundType.SLIME_BLOCK;
      SoundEvent soundEvent;
      if (colour == null) {
         soundEvent = SoundEvents.BUCKET_EMPTY;
      } else {
         soundEvent = SoundEvents.SLIME_SQUISH;
      }

      float volume = (soundType.getVolume() + 1.0F) / 2.0F;
      float pitch = soundType.getPitch() * 0.8F;
      level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, volume, pitch);
   }

   public static void playBucketEmpty(Level world, BlockPos pos, FluidStack fluid) {
      SoundEvent sound = fluid.getFluidType().getSound(SoundActions.BUCKET_EMPTY);
      world.playSound(null, pos, sound != null ? sound : SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   public static void playBucketFill(Level world, BlockPos pos, FluidStack fluid) {
      SoundEvent sound = fluid.getFluidType().getSound(SoundActions.BUCKET_FILL);
      world.playSound(null, pos, sound != null ? sound : SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
   }
}
