package buildcraft.core.tile;

import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.core.BCCoreBlockEntities;
import buildcraft.core.marker.VolumeCache;
import buildcraft.core.marker.VolumeConnection;
import buildcraft.core.marker.VolumeSubCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.tile.TileMarker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class TileMarkerVolume extends TileMarker<VolumeConnection> implements ITileAreaProvider {
   private static final Identifier ADVANCEMENT_MARKERS = Identifier.parse("buildcraftcore:markers");
   private boolean showSignals = false;

   public TileMarkerVolume(BlockPos pos, BlockState state) {
      super(BCCoreBlockEntities.MARKER_VOLUME, pos, state);
   }

   public boolean isShowingSignals() {
      return this.showSignals;
   }

   public VolumeCache getCache() {
      return VolumeCache.INSTANCE;
   }

   @Override
   public boolean isActiveForRender() {
      return this.showSignals || this.getCurrentConnection() != null;
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putBoolean("showSignals", this.showSignals);
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.showSignals = input.getBooleanOr("showSignals", false);
   }

   public void switchSignals() {
      if (this.level != null && !this.level.isClientSide()) {
         this.showSignals = !this.showSignals;
         this.setChanged();
      }
   }

   @Nonnull
   public AABB getRenderBoundingBox() {
      return new AABB(
         Double.NEGATIVE_INFINITY,
         Double.NEGATIVE_INFINITY,
         Double.NEGATIVE_INFINITY,
         Double.POSITIVE_INFINITY,
         Double.POSITIVE_INFINITY,
         Double.POSITIVE_INFINITY
      );
   }

   public void onManualConnectionAttempt(Player player) {
      MarkerSubCache<VolumeConnection> cache = this.getLocalCache();
      boolean connected = false;
      UnmodifiableIterator c = cache.getValidConnections(this.getBlockPos()).iterator();

      while (c.hasNext()) {
         BlockPos other = (BlockPos)c.next();
         if (cache.tryConnect(this.getBlockPos(), other)) {
            connected = true;
         }
      }

      VolumeConnection cx = this.getCurrentConnection();
      if (cx != null) {
         for (BlockPos corner : PositionUtil.getCorners(cx.getBox().min(), cx.getBox().max())) {
            if (!cx.getMarkerPositions().contains(corner) && cache.hasLoadedOrUnloadedMarker(corner) && cx.addMarker(corner)) {
               connected = true;
            }
         }
      }

      if (connected && this.level != null && !this.level.isClientSide()) {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_MARKERS);
      }
   }

   @Override
   public void onPlacedBy(LivingEntity placer, ItemStack stack) {
      if (this.level != null) {
         VolumeSubCache cache = VolumeCache.INSTANCE.getSubCache(this.level);
         BlockPos pos = this.getBlockPos();
         UnmodifiableIterator var5 = cache.getValidConnections(pos).iterator();

         while (var5.hasNext()) {
            BlockPos other = (BlockPos)var5.next();
            VolumeConnection c = cache.getConnection(other);
            if (c != null && c.getBox().isCorner(pos) && c.addMarker(pos)) {
               break;
            }
         }
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      super.getDebugInfo(left, right, side);
      left.add("Min = " + this.min());
      left.add("Max = " + this.max());
      left.add("Signals = " + this.showSignals);
   }

   @Override
   public BlockPos min() {
      VolumeConnection connection = this.getCurrentConnection();
      return connection == null ? this.getBlockPos() : connection.getBox().min();
   }

   @Override
   public BlockPos max() {
      VolumeConnection connection = this.getCurrentConnection();
      return connection == null ? this.getBlockPos() : connection.getBox().max();
   }

   @Override
   public void removeFromWorld() {
      if (this.level != null && !this.level.isClientSide()) {
         VolumeConnection connection = this.getCurrentConnection();
         if (connection != null) {
            for (BlockPos p : ImmutableList.copyOf(connection.getMarkerPositions())) {
               BlockState markerState = this.level.getBlockState(p);
               if (!markerState.isAir()) {
                  Block.popResource(this.level, p, new ItemStack(markerState.getBlock()));
                  this.level.destroyBlock(p, false);
               }
            }
         }
      }
   }

   @Override
   public boolean isValidFromLocation(BlockPos pos) {
      VolumeConnection connection = this.getCurrentConnection();
      if (connection == null) {
         return false;
      }

      Box box = connection.getBox();
      if (box.contains(pos)) {
         return false;
      }

      for (BlockPos p : PositionUtil.getCorners(box.min(), box.max())) {
         if (PositionUtil.isNextTo(p, pos)) {
            return true;
         }
      }

      return false;
   }
}
