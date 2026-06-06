package buildcraft.lib.tile;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class TileMarker<C extends MarkerConnection<C>> extends BlockEntity implements IDebuggable {
   private boolean chunkUnloading = false;

   public TileMarker(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public abstract MarkerCache<? extends MarkerSubCache<C>> getCache();

   public MarkerSubCache<C> getLocalCache() {
      return (MarkerSubCache<C>)this.getCache().getSubCache(this.level);
   }

   public abstract boolean isActiveForRender();

   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
   }

   public C getCurrentConnection() {
      return this.level == null ? null : this.getLocalCache().getConnection(this.getBlockPos());
   }

   public void buildcraft$onAttachedToLevel() {
      if (this.level != null && !this.level.isClientSide()) {
         this.getLocalCache().loadMarker(this.getBlockPos(), this);
      }
   }

   public void buildcraft$onChunkUnloading() {
      this.chunkUnloading = true;
      if (this.level != null && !this.level.isClientSide()) {
         this.getLocalCache().unloadMarker(this.getBlockPos());
      }
   }

   public void setRemoved() {
      super.setRemoved();
      if (this.level != null && !this.level.isClientSide() && !this.chunkUnloading) {
         this.getLocalCache().removeMarker(this.getBlockPos());
      }
   }

   protected void disconnectFromOthers() {
      C currentConnection = this.getCurrentConnection();
      if (currentConnection != null) {
         currentConnection.removeMarker(this.getBlockPos());
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      if (this.level != null) {
         C current = this.getCurrentConnection();
         MarkerSubCache<C> cache = this.getLocalCache();
         left.add("Exists = " + (cache.getMarker(this.getBlockPos()) == this));
         if (current == null) {
            left.add("Connection = null");
         } else {
            left.add("Connection:");
            current.getDebugInfo(this.getBlockPos(), left);
         }
      }
   }
}
