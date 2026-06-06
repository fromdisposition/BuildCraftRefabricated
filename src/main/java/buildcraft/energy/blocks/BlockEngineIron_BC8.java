package buildcraft.energy.blocks;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.items.FluidItemDrops;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.lib.engine.BlockEngineBase_BC8;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockEngineIron_BC8 extends BlockEngineBase_BC8 {
   public BlockEngineIron_BC8(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileEngineIron_BC8(pos, state);
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      boolean isWrench = !stack.isEmpty() && stack.getItem() instanceof IToolWrench;
      TileEngineIron_BC8 engine = level.getBlockEntity(pos) instanceof TileEngineIron_BC8 e ? e : null;
      if (isWrench && engine != null && engine.getPowerStage() == EnumPowerStage.OVERHEAT) {
         if (!level.isClientSide()) {
            engine.clearOverheat(player);
            SoundUtil.playSlideSound(level, pos, state, InteractionResult.SUCCESS);
         }

         player.swing(hand);
         return InteractionResult.CONSUME;
      } else if (stack.getItem() instanceof IItemPipe pipe) {
         InteractionResult placed = EnginePipeInteraction.tryPlacePipe(pipe, stack, level, player, hand, hitResult, PipeApi.flowFluids, PipeApi.flowPower);
         return placed != null ? placed : this.openGui(state, level, pos, player);
      } else {
         if (player.isShiftKeyDown()) {
            return this.openGui(state, level, pos, player);
         }

         if (isWrench) {
            if (engine != null && engine.hasAlternateReceiver()) {
               return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
               level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.4F, 1.3F);
            }

            player.swing(hand);
            return InteractionResult.CONSUME;
         } else if (engine != null && FluidUtilBC.onTankActivated(player, pos, hand, engine.getCombinedFluidStorage())) {
            return InteractionResult.SUCCESS;
         } else {
            return (InteractionResult)(FluidUtilBC.isFluidContainerInHand(player, hand) ? InteractionResult.SUCCESS : this.openGui(state, level, pos, player));
         }
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return this.openGui(state, level, pos, player);
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileEngineIron_BC8 engine) {
         NonNullList<ItemStack> drops = NonNullList.create();
         FluidItemDrops.addFluidDrops(drops, engine.tankFuel.getFluidStack(), engine.tankCoolant.getFluidStack(), engine.tankResidue.getFluidStack());

         for (ItemStack drop : drops) {
            Block.popResource(level, pos, drop);
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   private InteractionResult openGui(BlockState state, Level level, BlockPos pos, Player player) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      if (level.getBlockEntity(pos) instanceof TileEngineIron_BC8 engine && player instanceof ServerPlayer serverPlayer) {
         serverPlayer.openMenu(engine);
      }

      return InteractionResult.SUCCESS;
   }
}
