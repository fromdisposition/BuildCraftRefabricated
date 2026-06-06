package buildcraft.core.tile;

import buildcraft.api.core.IPathProvider;
import buildcraft.core.BCCoreBlockEntities;
import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.PathConnection;
import buildcraft.lib.tile.TileMarker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileMarkerPath extends TileMarker<PathConnection> implements IPathProvider {
   public TileMarkerPath(BlockPos pos, BlockState state) {
      super(BCCoreBlockEntities.MARKER_PATH, pos, state);
   }

   public ImmutableList<BlockPos> getPath() {
      PathConnection connection = this.getCurrentConnection();
      return connection == null ? ImmutableList.of() : connection.getMarkerPositions();
   }

   @Override
   public void removeFromWorld() {
      if (this.level != null && !this.level.isClientSide()) {
         UnmodifiableIterator var1 = this.getPath().iterator();

         while (var1.hasNext()) {
            BlockPos pos = (BlockPos)var1.next();
            BlockState markerState = this.level.getBlockState(pos);
            if (!markerState.isAir()) {
               Block.popResource(this.level, pos, new ItemStack(markerState.getBlock()));
               this.level.destroyBlock(pos, false);
            }
         }
      }
   }

   public PathCache getCache() {
      return PathCache.INSTANCE;
   }

   @Override
   public boolean isActiveForRender() {
      PathConnection connection = this.getCurrentConnection();
      return connection != null;
   }

   public void reverseDirection() {
      if (this.level != null && !this.level.isClientSide()) {
         PathConnection connection = this.getCurrentConnection();
         if (connection != null) {
            connection.reverseDirection();
         }
      }
   }
}
