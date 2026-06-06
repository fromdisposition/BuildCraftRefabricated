package buildcraft.builders.block;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.misc.AdvancementUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
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
import org.jetbrains.annotations.Nullable;

public class BlockQuarry extends HorizontalDirectionalBlock implements EntityBlock {
   public static final MapCodec<BlockQuarry> CODEC = simpleCodec(BlockQuarry::new);
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftbuilders:shaping_the_world");

   public BlockQuarry(Properties properties) {
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
      return new TileQuarry(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
      return blockEntityType != BCBuildersBlockEntities.QUARRY ? null : (lvl, pos, st, be) -> {
         if (be instanceof TileQuarry quarry) {
            quarry.tick();
         }
      };
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TileQuarry quarry) {
         quarry.onPlacedBy(placer, stack);
      }

      if (!level.isClientSide() && placer instanceof Player player) {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof TileQuarry quarry) {
         for (BlockPos blockPos : quarry.framePoses) {
            if (level.getBlockState(blockPos).is(BCBuildersBlocks.FRAME)) {
               level.removeBlock(blockPos, false);
            }
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }
}
