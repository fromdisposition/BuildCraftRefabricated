package buildcraft.builders.container;

import buildcraft.lib.net.BcPayloadBuffers;
import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.core.BCLog;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.filler.FillerPatternStatementGroups;
import buildcraft.builders.filler.FillerType;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.fabric.menu.FillerPlannerMenuKey;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.statement.StatementContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerFillerPlanner extends BcMenu implements IContainerFilling {
   public static final int NET_STATEMENT = 11;
   public static final int NET_INVERT = 12;
   @Nullable
   public final AddonFillerPlanner addon;
   private final FullStatement<IFillerPattern> patternStatementClient = new FullStatement<>(
      FillerType.INSTANCE, 4, (statement, paramIndex) -> this.onStatementChange()
   );
   public final StatementContext<IFillerPattern> possiblePatternsContext = FillerPatternStatementGroups.CONTEXT;
   private byte[] lastStatementHash = null;

   public ContainerFillerPlanner(int containerId, Inventory playerInv, AddonFillerPlanner addon) {
      super(BCBuildersMenuTypes.FILLER_PLANNER, containerId, playerInv.player);
      this.addon = addon;
   }

   public ContainerFillerPlanner(int containerId, Inventory playerInv, FillerPlannerMenuKey key) {
      super(BCBuildersMenuTypes.FILLER_PLANNER, containerId, playerInv.player);
      this.addon = resolveAddon(key);
   }

   private static AddonFillerPlanner resolveAddon(FillerPlannerMenuKey key) {
      if (key == null) {
         return null;
      }

      for (VolumeBox vb : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
         if (vb.id.equals(key.boxId())) {
            Addon a = vb.addons.get(key.slot());
            if (a instanceof AddonFillerPlanner planner) {
               return planner;
            }
            break;
         }
      }

      return null;
   }

   @Override
   public void onStatementChange() {
      if (this.player != null && this.player.level() != null && this.player.level().isClientSide()) {
         this.sendMessage(11, buf -> {
            PacketBufferBC buffer = BcPayloadBuffers.ensure(buf.unwrap());
            this.patternStatementClient.writeToBuffer(buffer);
         });
      }
   }

   @Override
   public Player getPlayer() {
      return this.player;
   }

   @Override
   public FullStatement<IFillerPattern> getPatternStatementClient() {
      return this.patternStatementClient;
   }

   @Override
   public FullStatement<IFillerPattern> getPatternStatement() {
      return this.addon != null ? this.addon.patternStatement : this.patternStatementClient;
   }

   @Override
   public boolean isInverted() {
      return this.addon != null && this.addon.inverted;
   }

   @Override
   public void setInverted(boolean value) {
      if (this.addon != null) {
         this.addon.inverted = value;
      }
   }

   @Override
   public void valuesChanged() {
      if (this.addon != null) {
         this.addon.updateBuildingInfo();
         if (this.player != null && this.player.level() != null && !this.player.level().isClientSide()) {
            WorldSavedDataVolumeBoxes.get(this.player.level()).markDirtyAndBroadcast();
         }
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 11) {
         try {
            if (isClient) {
               this.patternStatementClient.readFromBuffer(buffer);
            } else if (this.addon != null) {
               this.addon.patternStatement.readFromBuffer(buffer);
               this.valuesChanged();
            }
         } catch (IOException e) {
            BCLog.logger.warn("[builders.filler] Failed to read filler planner data from the network buffer", e);
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
         if (!isClient) {
            if (id == 12 && this.addon != null) {
               this.addon.inverted = !this.addon.inverted;
               this.valuesChanged();
            }
         }
      }
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      if (this.addon != null && this.player != null && this.player.level() != null && !this.player.level().isClientSide()) {
         ByteBuf temp = Unpooled.buffer();
         PacketBufferBC bcBuf = BcPayloadBuffers.ensure(temp);
         this.addon.patternStatement.writeToBuffer(bcBuf);
         byte[] current = new byte[temp.readableBytes()];
         temp.readBytes(current);
         temp.release();
         if (this.lastStatementHash == null || !Arrays.equals(this.lastStatementHash, current)) {
            this.lastStatementHash = current;
            this.sendMessage(11, buf -> buf.writeBytes(current));
         }
      }
   }

   @Override
   public boolean stillValid(Player player) {
      if (this.addon != null && this.addon.volumeBox != null) {
         BlockPos center = this.addon.volumeBox.box.center();
         double maxDist = BCCoreConfig.markerMaxDistance.get();
         return player.distanceToSqr(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5) <= maxDist * maxDist;
      } else {
         return false;
      }
   }
}
