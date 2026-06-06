package buildcraft.core.block;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.tile.TileEngineCreative;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.engine.TileEngineBase_BC8;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockEngineCreative extends BlockEngineBase_BC8 {
   public BlockEngineCreative(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEngineCreative(pos, state);
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (stack.getItem() instanceof IToolWrench wrench) {
         BlockEntity var11 = level.getBlockEntity(pos);
         if (player.isShiftKeyDown()) {
            if (var11 instanceof TileEngineBase_BC8 engine && engine.hasAlternateReceiver()) {
               return InteractionResult.PASS;
            } else {
               if (!level.isClientSide()) {
                  level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, 1.3F);
               }

               player.swing(hand);
               return InteractionResult.CONSUME;
            }
         } else {
            if (var11 instanceof TileEngineCreative creative) {
               creative.onWrenchInteract(player);
            }

            wrench.wrenchUsed(player, hand, stack, hitResult);
            if (!level.isClientSide()) {
               level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, 0.7F);
            }

            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.PASS;
      }
   }
}
