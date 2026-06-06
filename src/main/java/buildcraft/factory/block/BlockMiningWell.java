package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.tile.TileMiner;
import buildcraft.factory.tile.TileMiningWell;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class BlockMiningWell extends BaseEntityBlock {
   public static final Property<Direction> FACING = BuildCraftProperties.BLOCK_FACING;
   public static final MapCodec<BlockMiningWell> CODEC = simpleCodec(BlockMiningWell::new);

   public BlockMiningWell(Properties properties) {
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
      return new TileMiningWell(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide()
         ? createTickerHelper(type, BCFactoryBlockEntities.MINING_WELL, (lvl, pos, st, tile) -> tile.clientTick())
         : createTickerHelper(type, BCFactoryBlockEntities.MINING_WELL, (lvl, pos, st, tile) -> tile.serverTick());
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileMiningWell well) {
         well.onPlacedBy(placer, stack);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileMiner miner) {
         miner.onRemove();
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
