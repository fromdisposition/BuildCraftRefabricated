package buildcraft.energy.blocks;

import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.BlockDropsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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

public class BlockEngineFE extends BlockEngineBase_BC8 {
   public BlockEngineFE(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEngineRF(pos, state);
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (stack.getItem() instanceof IItemPipe pipe) {
         InteractionResult placed = EnginePipeInteraction.tryPlacePipe(pipe, stack, level, player, hand, hitResult, PipeApi.flowRf, PipeApi.flowPower);
         return placed != null ? placed : this.openGui(state, level, pos, player);
      } else {
         if (player.isShiftKeyDown()) {
            return this.openGui(state, level, pos, player);
         }

         if (stack.getItem() instanceof IToolWrench) {
            if (level.getBlockEntity(pos) instanceof TileEngineBase_BC8 engine && engine.hasAlternateReceiver()) {
               return InteractionResult.PASS;
            } else {
               if (!level.isClientSide()) {
                  level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, 1.3F);
               }

               player.swing(hand);
               return InteractionResult.CONSUME;
            }
         } else {
            return this.openGui(state, level, pos, player);
         }
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return this.openGui(state, level, pos, player);
   }

   private InteractionResult openGui(BlockState state, Level level, BlockPos pos, Player player) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      if (level.getBlockEntity(pos) instanceof TileEngineRF engine && player instanceof ServerPlayer serverPlayer) {
         serverPlayer.openMenu(engine);
      }

      return InteractionResult.SUCCESS;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof TileEngineRF engine) {
         BlockDropsUtil.dropItems(level, pos, engine.upgrades);
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
