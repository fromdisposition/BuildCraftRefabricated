package buildcraft.core.block;

import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.lib.block.BlockMarkerBase;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class BlockMarkerVolume extends BlockMarkerBase {
   public BlockMarkerVolume(Properties properties) {
      super(properties);
   }

   @Override
   public BlockEntity createTileEntity(BlockPos pos, BlockState state) {
      return new TileMarkerVolume(pos, state);
   }

   protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
      if (!world.isClientSide() && world.getBlockEntity(pos) instanceof TileMarkerVolume volume) {
         volume.onManualConnectionAttempt(player);
      }

      return InteractionResult.SUCCESS;
   }

   protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, @Nullable Orientation orientation, boolean isMoving) {
      checkSignalState(world, pos);
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return true;
   }

   private static void checkSignalState(Level world, BlockPos pos) {
      if (!world.isClientSide()) {
         if (world.getBlockEntity(pos) instanceof TileMarkerVolume volume) {
            boolean powered = world.hasNeighborSignal(pos);
            if (volume.isShowingSignals() != powered) {
               volume.switchSignals();
            }
         }
      }
   }
}
