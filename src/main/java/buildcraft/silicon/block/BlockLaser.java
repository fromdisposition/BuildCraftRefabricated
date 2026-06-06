package buildcraft.silicon.block;

import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.tile.TileLaser;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.Nullable;

public class BlockLaser extends BaseEntityBlock {
   public static final MapCodec<BlockLaser> CODEC = simpleCodec(BlockLaser::new);
   public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

   public BlockLaser(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.UP));
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileLaser(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCSiliconBlockEntities.LASER, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCSiliconBlockEntities.LASER, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }
}
