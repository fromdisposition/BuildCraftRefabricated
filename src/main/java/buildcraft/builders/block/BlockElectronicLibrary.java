package buildcraft.builders.block;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.lib.misc.BlockDropsUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockElectronicLibrary extends HorizontalDirectionalBlock implements EntityBlock {
   public static final MapCodec<BlockElectronicLibrary> CODEC = simpleCodec(BlockElectronicLibrary::new);

   public BlockElectronicLibrary(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
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
      return new TileElectronicLibrary(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return blockEntityType != BCBuildersBlockEntities.LIBRARY ? null : (lvl, pos, st, be) -> {
         if (be instanceof TileElectronicLibrary tile) {
            tile.tick();
         }
      };
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileElectronicLibrary library) {
         player.openMenu(library);
      }

      return InteractionResult.SUCCESS;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileElectronicLibrary library) {
         library.onPlacedBy(placer, stack);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof TileElectronicLibrary library) {
         BlockDropsUtil.dropTileContents(level, pos, library);
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
