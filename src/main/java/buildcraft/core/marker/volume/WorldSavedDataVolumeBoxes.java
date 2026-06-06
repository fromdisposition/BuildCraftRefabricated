package buildcraft.core.marker.volume;

import buildcraft.lib.net.BcPacketDistributor;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.AABB;

public class WorldSavedDataVolumeBoxes extends SavedData {
   private static final String DATA_NAME = "buildcraft_volume_boxes";
   public Level world;
   public final List<VolumeBox> volumeBoxes = new ArrayList<>();

   public static SavedDataType<WorldSavedDataVolumeBoxes> createType(Level world) {
      return new SavedDataType<>(
         Identifier.withDefaultNamespace("buildcraft_volume_boxes"), () -> new WorldSavedDataVolumeBoxes(world), buildCodec(world), DataFixTypes.LEVEL
      );
   }

   private static Codec<WorldSavedDataVolumeBoxes> buildCodec(Level world) {
      return CompoundTag.CODEC.xmap(nbt -> fromNbt(nbt, world), WorldSavedDataVolumeBoxes::toNbt);
   }

   private static WorldSavedDataVolumeBoxes fromNbt(CompoundTag nbt, Level world) {
      WorldSavedDataVolumeBoxes instance = new WorldSavedDataVolumeBoxes(world);
      if (nbt.contains("volumeBoxes")) {
         ListTag listTag = (ListTag)nbt.getList("volumeBoxes").orElseGet(ListTag::new);

         for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = (CompoundTag)listTag.getCompound(i).orElseGet(CompoundTag::new);
            instance.volumeBoxes.add(new VolumeBox(world, tag));
         }
      }

      return instance;
   }

   private static CompoundTag toNbt(WorldSavedDataVolumeBoxes data) {
      CompoundTag nbt = new CompoundTag();
      ListTag listTag = new ListTag();

      for (VolumeBox volumeBox : data.volumeBoxes) {
         listTag.add(volumeBox.writeToNBT());
      }

      nbt.put("volumeBoxes", listTag);
      return nbt;
   }

   public WorldSavedDataVolumeBoxes(Level world) {
      this.world = world;
   }

   public VolumeBox getVolumeBoxAt(BlockPos pos) {
      for (VolumeBox volumeBox : this.volumeBoxes) {
         if (volumeBox.box.contains(pos)) {
            return volumeBox;
         }
      }

      return null;
   }

   public void addVolumeBox(BlockPos pos) {
      VolumeBox box = new VolumeBox(this.world, pos);
      this.volumeBoxes.add(box);
      this.setDirty();
      this.broadcastDelta(Set.of(box.id), Set.of(), pos);
   }

   public void sendTo(ServerPlayer player) {
      BcPacketDistributor.sendToPlayer(player, this.createFullMessage());
   }

   public void markDirtyAndBroadcast() {
      this.setDirty();
      this.broadcastDelta(this.volumeBoxes.stream().map(vb -> vb.id).collect(Collectors.toSet()), Set.of());
   }

   public void markDirtyAndBroadcast(VolumeBox box) {
      this.setDirty();
      this.broadcastDelta(Set.of(box.id), Set.of(), getTrackingPos(box));
   }

   public void markRemovedAndBroadcast(UUID id, BlockPos lastKnownPos) {
      this.volumeBoxes.removeIf(vb -> vb.id.equals(id));
      this.setDirty();
      this.broadcastDelta(Set.of(), Set.of(id), lastKnownPos);
   }

   private MessageVolumeBoxes createFullMessage() {
      List<CompoundTag> tags = this.volumeBoxes.stream().map(VolumeBox::writeToNBT).toList();
      return MessageVolumeBoxes.full(tags);
   }

   private void broadcastDelta(Set<UUID> updatedIds, Set<UUID> removedIds) {
      if (!updatedIds.isEmpty() || !removedIds.isEmpty()) {
         BlockPos anchor = BlockPos.ZERO;
         if (!updatedIds.isEmpty()) {
            VolumeBox first = this.getVolumeBoxFromId(updatedIds.iterator().next());
            if (first != null) {
               anchor = getTrackingPos(first);
            }
         }

         this.broadcastDelta(updatedIds, removedIds, anchor);
      }
   }

   private void broadcastDelta(Set<UUID> updatedIds, Set<UUID> removedIds, BlockPos trackingAnchor) {
      if (this.world instanceof ServerLevel sl) {
         if (!updatedIds.isEmpty() || !removedIds.isEmpty()) {
            List<CompoundTag> changed = updatedIds.stream().map(this::getVolumeBoxFromId).filter(Objects::nonNull).map(VolumeBox::writeToNBT).toList();
            MessageVolumeBoxes message = MessageVolumeBoxes.delta(new ArrayList<>(removedIds), changed);
            Set<ServerPlayer> sent = new HashSet<>();
            if (!updatedIds.isEmpty()) {
               for (UUID id : updatedIds) {
                  VolumeBox box = this.getVolumeBoxFromId(id);
                  if (box != null) {
                     for (ServerPlayer player : PlayerLookup.tracking(sl, getTrackingPos(box))) {
                        if (sent.add(player)) {
                           BcPacketDistributor.sendToPlayer(player, message);
                        }
                     }
                  }
               }
            }

            if (!removedIds.isEmpty()) {
               for (ServerPlayer player : PlayerLookup.tracking(sl, trackingAnchor)) {
                  if (sent.add(player)) {
                     BcPacketDistributor.sendToPlayer(player, message);
                  }
               }
            }
         }
      }
   }

   private static BlockPos getTrackingPos(VolumeBox volumeBox) {
      return volumeBox.box.isInitialized() ? volumeBox.box.min() : BlockPos.ZERO;
   }

   public VolumeBox getVolumeBoxFromId(UUID id) {
      for (VolumeBox volumeBox : this.volumeBoxes) {
         if (volumeBox.id.equals(id)) {
            return volumeBox;
         }
      }

      return null;
   }

   public VolumeBox getCurrentEditing(Player player) {
      for (VolumeBox volumeBox : this.volumeBoxes) {
         if (volumeBox.isEditingBy(player)) {
            return volumeBox;
         }
      }

      return null;
   }

   public void tick() {
      if (this.volumeBoxes.isEmpty()) {
         return;
      }

      boolean dirty = false;
      Set<UUID> tickUpdated = null;

      for (VolumeBox volumeBox : this.volumeBoxes) {
         if (volumeBox.isEditing() || !volumeBox.locks.isEmpty()) {
            if (volumeBox.isEditing()) {
               Player player = volumeBox.getPlayer(this.world);
               if (player == null) {
                  volumeBox.pauseEditing();
                  dirty = true;
                  if (tickUpdated == null) {
                     tickUpdated = new HashSet<>();
                  }

                  tickUpdated.add(volumeBox.id);
               } else {
                  AABB oldAabb = volumeBox.box.getBoundingBox();
                  volumeBox.box.reset();
                  volumeBox.box.extendToEncompass(volumeBox.getHeld());
                  BlockPos lookingAt = BlockPos.containing(
                     player.position().add(0.0, player.getEyeHeight(), 0.0).add(player.getLookAngle().scale(volumeBox.getDist()))
                  );
                  volumeBox.box.extendToEncompass(lookingAt);
                  if (!volumeBox.box.getBoundingBox().equals(oldAabb)) {
                     dirty = true;
                     if (tickUpdated == null) {
                        tickUpdated = new HashSet<>();
                     }

                     tickUpdated.add(volumeBox.id);
                  }
               }
            }

            if (!volumeBox.locks.isEmpty()) {
               boolean removed = volumeBox.locks.removeIf(lock -> !lock.cause.stillWorks(this.world));
               if (removed) {
                  dirty = true;
                  if (tickUpdated == null) {
                     tickUpdated = new HashSet<>();
                  }

                  tickUpdated.add(volumeBox.id);
               }
            }
         }
      }

      if (dirty && tickUpdated != null) {
         this.setDirty();
         this.broadcastDelta(tickUpdated, Set.of());
      }
   }

   public static WorldSavedDataVolumeBoxes get(Level world) {
      if (world.isClientSide()) {
         throw new IllegalArgumentException("Tried to create a world saved data instance on the client!");
      }

      ServerLevel serverLevel = (ServerLevel)world;
      return (WorldSavedDataVolumeBoxes)serverLevel.getDataStorage().computeIfAbsent(createType(world));
   }
}
