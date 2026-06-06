package buildcraft.factory.block;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.items.FluidItemDrops;
import buildcraft.api.tools.IToolWrench;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.misc.FluidUtilBC;
import com.mojang.serialization.MapCodec;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockHeatExchange extends BaseEntityBlock implements ICustomRotationHandler {
   public static final MapCodec<BlockHeatExchange> CODEC = simpleCodec(BlockHeatExchange::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<BlockHeatExchange.EnumExchangePart> PART = EnumProperty.create("part", BlockHeatExchange.EnumExchangePart.class);
   public static final BooleanProperty CONNECTED_LEFT = BooleanProperty.create("connected_left");
   public static final BooleanProperty CONNECTED_RIGHT = BooleanProperty.create("connected_right");

   public BlockHeatExchange(Properties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))
                  .setValue(PART, BlockHeatExchange.EnumExchangePart.MIDDLE))
               .setValue(CONNECTED_LEFT, false))
            .setValue(CONNECTED_RIGHT, false)
      );
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, PART, CONNECTED_LEFT, CONNECTED_RIGHT});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction facing = context.getHorizontalDirection().getOpposite();
      BlockState state = (BlockState)this.defaultBlockState().setValue(FACING, facing);
      return this.updateConnections(state, context.getLevel(), context.getClickedPos(), facing);
   }

   protected BlockState updateShape(
      BlockState state,
      LevelReader level,
      ScheduledTickAccess scheduledTickAccess,
      BlockPos pos,
      Direction direction,
      BlockPos neighborPos,
      BlockState neighborState,
      RandomSource randomSource
   ) {
      if (direction.getAxis().isVertical()) {
         return state;
      }

      Direction facing = (Direction)state.getValue(FACING);
      return this.updateConnections(state, level, pos, facing);
   }

   private BlockState updateConnections(BlockState state, LevelReader level, BlockPos pos, Direction facing) {
      boolean connectLeft = doesNeighbourConnect(level, pos, facing, facing.getCounterClockWise());
      boolean connectRight = doesNeighbourConnect(level, pos, facing, facing.getClockWise());
      return (BlockState)((BlockState)state.setValue(CONNECTED_LEFT, connectLeft)).setValue(CONNECTED_RIGHT, connectRight);
   }

   private static boolean doesNeighbourConnect(LevelReader level, BlockPos pos, Direction thisFacing, Direction dir) {
      BlockState neighbour = level.getBlockState(pos.relative(dir));
      return neighbour.getBlock() instanceof BlockHeatExchange ? neighbour.getValue(FACING) == thisFacing : false;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileHeatExchange(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCFactoryBlockEntities.HEAT_EXCHANGE, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCFactoryBlockEntities.HEAT_EXCHANGE, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useItemOn(
      ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
   ) {
      if (stack.getItem() instanceof IToolWrench) {
         if (player.isShiftKeyDown()) {
            return (InteractionResult)(level.getBlockEntity(pos) instanceof TileHeatExchange exchange
               ? openExchangeMenu(level, exchange, player)
               : InteractionResult.PASS);
         } else {
            return InteractionResult.PASS;
         }
      } else if (level.getBlockEntity(pos) instanceof TileHeatExchange exchange) {
         TileHeatExchange.ExchangeSection section = exchange.getSection();
         if (section != null) {
            boolean didChange = FluidUtilBC.onTankActivated(player, pos, hand, section.tankInput);
            if (!didChange) {
               boolean didChangeOutput = FluidUtilBC.onTankActivated(player, pos, hand, section.tankOutput);
               didChange = didChangeOutput;
            }

            if (didChange) {
               return InteractionResult.SUCCESS;
            }
         }

         return (InteractionResult)(FluidUtilBC.isFluidContainerInHand(player, hand) ? InteractionResult.SUCCESS : openExchangeMenu(level, exchange, player));
      } else {
         return InteractionResult.PASS;
      }
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      return (InteractionResult)(level.getBlockEntity(pos) instanceof TileHeatExchange exchange
         ? openExchangeMenu(level, exchange, player)
         : InteractionResult.PASS);
   }

   @Override
   public InteractionResult attemptRotation(Level level, BlockPos pos, BlockState state, Direction sideWrenched) {
      return (InteractionResult)(level.getBlockEntity(pos) instanceof TileHeatExchange exchange && exchange.rotate()
         ? InteractionResult.SUCCESS
         : InteractionResult.PASS);
   }

   private static InteractionResult openExchangeMenu(Level level, TileHeatExchange exchange, Player player) {
      TileHeatExchange start = exchange.findStart();
      if (start == null) {
         return InteractionResult.PASS;
      }

      if (!level.isClientSide()) {
         player.openMenu(start);
      }

      return InteractionResult.SUCCESS;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileHeatExchange exchange) {
         TileHeatExchange.ExchangeSection section = exchange.getSection();
         if (section != null) {
            NonNullList<ItemStack> toDrop = NonNullList.create();
            FluidItemDrops.addFluidDrops(toDrop, section.tankInput);
            FluidItemDrops.addFluidDrops(toDrop, section.tankOutput);

            for (ItemStack drop : toDrop) {
               Block.popResource(level, pos, drop);
            }
         }

         for (int i = 0; i < exchange.containerSlots.getSlots(); i++) {
            ItemStack slotStack = exchange.containerSlots.getStackInSlot(i);
            if (!slotStack.isEmpty()) {
               Block.popResource(level, pos, slotStack);
            }
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
      super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
      if (level.getBlockEntity(pos) instanceof TileHeatExchange exchange) {
         exchange.markCheckNeighbours();
      }
   }

   public enum EnumExchangePart implements StringRepresentable {
      START,
      MIDDLE,
      END;

      private final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);

      public String getSerializedName() {
         return this.lowerCaseName;
      }
   }
}
