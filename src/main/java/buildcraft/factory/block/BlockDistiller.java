package buildcraft.factory.block;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.items.FluidItemDrops;
import buildcraft.api.tools.IToolWrench;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.misc.FluidUtilBC;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockDistiller extends BaseEntityBlock implements ICustomRotationHandler {
   public static final MapCodec<BlockDistiller> CODEC = simpleCodec(BlockDistiller::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

   public BlockDistiller(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.WEST));
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TileDistiller_BC8 distiller) {
         distiller.onPlacedBy(placer);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileDistiller_BC8(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCFactoryBlockEntities.DISTILLER, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCFactoryBlockEntities.DISTILLER, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (stack.getItem() instanceof IToolWrench) {
         if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileDistiller_BC8 distiller) {
               player.openMenu(distiller);
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      } else if (level.getBlockEntity(pos) instanceof TileDistiller_BC8 distiller) {
         boolean didChange = FluidUtilBC.onTankActivated(player, pos, hand, distiller.getTankIn());
         if (!didChange) {
            didChange = FluidUtilBC.onTankActivated(player, pos, hand, distiller.getTankGasOut());
         }

         if (!didChange) {
            didChange = FluidUtilBC.onTankActivated(player, pos, hand, distiller.getTankLiquidOut());
         }

         if (didChange) {
            return InteractionResult.SUCCESS;
         }

         if (!FluidUtilBC.isFluidContainerInHand(player, hand) && !level.isClientSide()) {
            player.openMenu(distiller);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public InteractionResult attemptRotation(Level level, BlockPos pos, BlockState state, Direction sideWrenched) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      Direction current = (Direction)state.getValue(FACING);
      level.setBlock(pos, (BlockState)state.setValue(FACING, current.getClockWise()), 3);
      return InteractionResult.SUCCESS;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!level.isClientSide()) {
         BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof TileDistiller_BC8) {
            player.openMenu((TileDistiller_BC8)be);
         }
      }

      return InteractionResult.SUCCESS;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileDistiller_BC8 distiller) {
         NonNullList<ItemStack> toDrop = NonNullList.create();
         FluidItemDrops.addFluidDrops(
            toDrop, distiller.getTankIn().getFluidStack(), distiller.getTankGasOut().getFluidStack(), distiller.getTankLiquidOut().getFluidStack()
         );

         for (ItemStack drop : toDrop) {
            Block.popResource(level, pos, drop);
         }

         for (int i = 0; i < distiller.containerSlots.getSlots(); i++) {
            ItemStack slotStack = distiller.containerSlots.getStackInSlot(i);
            if (!slotStack.isEmpty()) {
               Block.popResource(level, pos, slotStack);
            }
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
