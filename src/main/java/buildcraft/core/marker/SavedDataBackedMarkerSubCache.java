package buildcraft.core.marker;

import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.net.MessageMarker;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public abstract class SavedDataBackedMarkerSubCache<C extends MarkerConnection<C>, D extends MarkerSavedDataBase<C>> extends MarkerSubCache<C> {
   protected final D savedData;

   protected SavedDataBackedMarkerSubCache(Level world, int cacheId, D data, BiFunction<SavedDataBackedMarkerSubCache<C, D>, List<BlockPos>, C> connectionFactory) {
      super(world, cacheId);
      this.savedData = data;

      for (BlockPos pos : data.markerPositions) {
         this.loadMarker(pos, null);
      }

      for (List<BlockPos> connectionPositions : data.markerConnections) {
         if (connectionPositions.size() >= 2) {
            this.addConnection(connectionFactory.apply(this, connectionPositions));
         }
      }

      data.setSubCache(this);
      data.setDirty();
   }

   @Override
   protected void markSavedDataDirty() {
      if (this.savedData != null) {
         this.savedData.setDirty();
      }
   }

   protected boolean handleConnectionMessage(MessageMarker message, BiFunction<SavedDataBackedMarkerSubCache<C, D>, List<BlockPos>, C> connectionFactory) {
      List<BlockPos> positions = message.positions();
      if (message.connection()) {
         if (message.add()) {
            for (BlockPos p : positions) {
               C existing = this.getConnection(p);
               this.destroyConnection(existing);
            }

            this.addConnection(connectionFactory.apply(this, positions));
         } else {
            for (BlockPos p : positions) {
               C existing = this.getConnection(p);
               if (existing != null) {
                  existing.removeMarker(p);
                  this.refreshConnection(existing);
               }
            }
         }
      }

      return false;
   }
}
