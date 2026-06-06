package buildcraft.transport.block;

import buildcraft.lib.misc.BlockDropsUtil;
import buildcraft.transport.tile.TileFilteredBuffer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockFilteredBuffer extends BaseEntityBlock {
   public static final MapCodec<BlockFilteredBuffer> CODEC = simpleCodec(BlockFilteredBuffer::new);

   public BlockFilteredBuffer(Properties properties) {
      super(properties);
   }

   protected MapCodec<? extends BaseEntityBlock> codec() {
      return CODEC;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new TileFilteredBuffer(pos, state);
   }

   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!level.isClientSide()) {
         BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof TileFilteredBuffer) {
            player.openMenu((TileFilteredBuffer)be);
         }
      }

      return InteractionResult.SUCCESS;
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof TileFilteredBuffer buffer) {
         BlockDropsUtil.dropTileContents(level, pos, buffer);
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileFilteredBuffer buffer) {
         buffer.onPlacedBy(placer, stack);
         level.sendBlockUpdated(pos, state, state, 3);
      }
   }
}
