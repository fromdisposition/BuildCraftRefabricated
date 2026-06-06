package buildcraft.factory.item;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockWaterGel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public class ItemWaterGel extends Item {
   public ItemWaterGel(Properties properties) {
      super(properties);
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      BlockHitResult ray = getPlayerPOVHitResult(level, player, Fluid.SOURCE_ONLY);
      if (ray.getType() != Type.MISS && ray.getBlockPos() != null) {
         BlockState hitState = level.getBlockState(ray.getBlockPos());
         if (hitState.is(Blocks.WATER) && level.getFluidState(ray.getBlockPos()).isSource()) {
            if (!player.getAbilities().instabuild) {
               stack.shrink(1);
            }

            level.playSound(
               null,
               player.getX(),
               player.getY(),
               player.getZ(),
               SoundEvents.SNOWBALL_THROW,
               SoundSource.NEUTRAL,
               0.5F,
               0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
            );
            if (!level.isClientSide()) {
               BlockState gelState = (BlockState)BCFactoryBlocks.WATER_GEL
                  .defaultBlockState()
                  .setValue(BlockWaterGel.PROP_STAGE, BlockWaterGel.GelStage.SPREAD_0);
               level.setBlockAndUpdate(ray.getBlockPos(), gelState);
               level.scheduleTick(ray.getBlockPos(), BCFactoryBlocks.WATER_GEL, 200);
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         return InteractionResult.FAIL;
      }
   }
}
