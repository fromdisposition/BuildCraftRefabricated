package buildcraft.core.block;

import buildcraft.core.tile.TileMarkerPath;
import buildcraft.lib.block.BlockMarkerBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMarkerPath extends BlockMarkerBase {
   public BlockMarkerPath(Properties properties) {
      super(properties);
   }

   @Override
   public BlockEntity createTileEntity(BlockPos pos, BlockState state) {
      return new TileMarkerPath(pos, state);
   }

   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!world.isClientSide() && world.getBlockEntity(pos) instanceof TileMarkerPath marker) {
         marker.reverseDirection();
      }

      return InteractionResult.SUCCESS;
   }
}
