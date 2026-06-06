package buildcraft.robotics.block;

import buildcraft.lib.misc.BlockDropsUtil;
import buildcraft.robotics.BCRoboticsBlockEntities;
import buildcraft.robotics.tile.TileZonePlanner;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public class BlockZonePlanner extends BaseEntityBlock {
   public static final MapCodec<BlockZonePlanner> CODEC = simpleCodec(BlockZonePlanner::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

   public BlockZonePlanner(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
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

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileZonePlanner(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : createTickerHelper(type, BCRoboticsBlockEntities.ZONE_PLANNER, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!level.isClientSide()) {
         BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof TileZonePlanner) {
            player.openMenu((TileZonePlanner)be);
         }
      }

      return InteractionResult.SUCCESS;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof TileZonePlanner planner) {
         BlockDropsUtil.dropItems(
            level,
            pos,
            planner.invPaintbrushes,
            planner.invInputPaintbrush,
            planner.invInputMapLocation,
            planner.invInputResult,
            planner.invOutputPaintbrush,
            planner.invOutputMapLocation,
            planner.invOutputResult
         );
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
