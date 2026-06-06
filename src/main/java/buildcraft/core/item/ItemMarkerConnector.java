package buildcraft.core.item;

import buildcraft.core.marker.PathSubCache;
import buildcraft.core.marker.VolumeSubCache;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.Lock;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerSubCache;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.PositionUtil;
import buildcraft.lib.misc.VecUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class ItemMarkerConnector extends Item {
   private static final Identifier ADVANCEMENT_VOLUME_MARKER = Identifier.parse("buildcraftcore:markers");
   private static final Identifier ADVANCEMENT_PATH_MARKER = Identifier.parse("buildcraftcore:path_markers");

   public ItemMarkerConnector(Properties properties) {
      super(properties);
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (!level.isClientSide()) {
         for (MarkerCache<?> cache : MarkerCache.CACHES) {
            if (interactCache(cache.getSubCache(level), player)) {
               return InteractionResult.SUCCESS;
            }
         }
      }

      InteractionResult volumeResult = this.onUseVolumeBoxes(level, player);
      return (InteractionResult)(volumeResult.consumesAction() ? volumeResult : InteractionResult.SUCCESS);
   }

   private static <S extends MarkerSubCache<?>> boolean interactCache(S cache, Player player) {
      ItemMarkerConnector.MarkerLineInteraction best = null;
      Vec3 playerPos = player.position().add(0.0, player.getEyeHeight(), 0.0);
      Vec3 playerLook = player.getLookAngle();
      UnmodifiableIterator var5 = cache.getAllMarkers().iterator();

      while (var5.hasNext()) {
         BlockPos marker = (BlockPos)var5.next();
         ImmutableList<BlockPos> possibles = cache.getValidConnections(marker);
         UnmodifiableIterator var8 = possibles.iterator();

         while (var8.hasNext()) {
            BlockPos possible = (BlockPos)var8.next();
            ItemMarkerConnector.MarkerLineInteraction interaction = new ItemMarkerConnector.MarkerLineInteraction(marker, possible, playerPos, playerLook);
            if (interaction.didInteract()) {
               best = interaction.getBetter(best);
            }
         }
      }

      if (best == null) {
         return false;
      }

      if (!cache.tryConnect(best.marker1, best.marker2) && !cache.tryConnect(best.marker2, best.marker1)) {
         return false;
      }

      if (cache instanceof VolumeSubCache) {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_VOLUME_MARKER);
      } else if (cache instanceof PathSubCache) {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PATH_MARKER);
      }

      return true;
   }

   public static boolean doesInteract(BlockPos a, BlockPos b, Player player) {
      return new ItemMarkerConnector.MarkerLineInteraction(a, b, player.position().add(0.0, player.getEyeHeight(), 0.0), player.getLookAngle()).didInteract();
   }

   private InteractionResult onUseVolumeBoxes(Level level, Player player) {
      if (level.isClientSide()) {
         return InteractionResult.PASS;
      }

      WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
      VolumeBox currentEditing = volumeBoxes.getCurrentEditing(player);
      Vec3 start = player.position().add(0.0, player.getEyeHeight(), 0.0);
      Vec3 end = start.add(player.getLookAngle().scale(4.0));
      Pair<VolumeBox, EnumAddonSlot> selectingVolumeBoxAndSlot = EnumAddonSlot.getSelectingVolumeBoxAndSlot(player, volumeBoxes.volumeBoxes);
      VolumeBox addonVolumeBox = (VolumeBox)selectingVolumeBoxAndSlot.getLeft();
      EnumAddonSlot addonSlot = (EnumAddonSlot)selectingVolumeBoxAndSlot.getRight();
      if (addonVolumeBox != null && addonSlot != null) {
         if (addonVolumeBox.addons.containsKey(addonSlot)
            && addonVolumeBox.getLockTargetsStream()
               .noneMatch(target -> target instanceof Lock.Target.TargetAddon && ((Lock.Target.TargetAddon)target).slot == addonSlot)) {
            if (player.isShiftKeyDown()) {
               addonVolumeBox.addons.get(addonSlot).onRemoved();
               addonVolumeBox.addons.remove(addonSlot);
               volumeBoxes.markDirtyAndBroadcast();
            } else {
               addonVolumeBox.addons.get(addonSlot).onPlayerRightClick(player);
               volumeBoxes.markDirtyAndBroadcast();
            }
         }
      } else if (player.isShiftKeyDown()) {
         if (currentEditing != null) {
            currentEditing.cancelEditing();
            volumeBoxes.markDirtyAndBroadcast();
            return InteractionResult.SUCCESS;
         }

         for (VolumeBox volumeBox : volumeBoxes.volumeBoxes) {
            Optional<Vec3> clip = volumeBox.box.getBoundingBox().clip(start, end);
            if (clip.isPresent()) {
               if (volumeBox.getLockTargetsStream().noneMatch(Lock.Target.TargetResize.class::isInstance)) {
                  volumeBox.addons.values().forEach(Addon::onRemoved);
                  UUID id = volumeBox.id;
                  BlockPos trackingPos = volumeBox.box.isInitialized() ? volumeBox.box.min() : BlockPos.ZERO;
                  volumeBoxes.markRemovedAndBroadcast(id, trackingPos);
                  return InteractionResult.SUCCESS;
               }

               return InteractionResult.FAIL;
            }
         }
      } else {
         if (currentEditing != null) {
            currentEditing.confirmEditing();
            volumeBoxes.markDirtyAndBroadcast();
            return InteractionResult.SUCCESS;
         }

         VolumeBox bestVolumeBox = null;
         double bestDist = Double.MAX_VALUE;
         BlockPos editing = null;

         for (VolumeBox volumeBox : volumeBoxes.volumeBoxes
            .stream()
            .filter(box -> box.getLockTargetsStream().noneMatch(Lock.Target.TargetResize.class::isInstance))
            .collect(Collectors.toList())) {
            for (BlockPos p : PositionUtil.getCorners(volumeBox.box.min(), volumeBox.box.max())) {
               Optional<Vec3> ray = new AABB(p).clip(start, end);
               if (ray.isPresent()) {
                  double dist = ray.get().distanceTo(start);
                  if (bestDist > dist) {
                     bestDist = dist;
                     bestVolumeBox = volumeBox;
                     editing = p;
                  }
               }
            }
         }

         if (bestVolumeBox != null) {
            bestVolumeBox.setPlayer(player);
            BlockPos min = bestVolumeBox.box.min();
            BlockPos max = bestVolumeBox.box.max();
            BlockPos held = min;
            if (editing.getX() == min.getX()) {
               held = VecUtil.replaceValue(held, Axis.X, max.getX());
            }

            if (editing.getY() == min.getY()) {
               held = VecUtil.replaceValue(held, Axis.Y, max.getY());
            }

            if (editing.getZ() == min.getZ()) {
               held = VecUtil.replaceValue(held, Axis.Z, max.getZ());
            }

            bestVolumeBox.setHeldDistOldMinOldMax(held, Math.max(1.5, bestDist + 0.5), bestVolumeBox.box.min(), bestVolumeBox.box.max());
            volumeBoxes.markDirtyAndBroadcast();
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.FAIL;
   }

   private static class MarkerLineInteraction {
      public final BlockPos marker1;
      public final BlockPos marker2;
      public final double distToPoint;
      public final double distToLine;

      public MarkerLineInteraction(BlockPos marker1, BlockPos marker2, Vec3 playerPos, Vec3 playerEndPos) {
         this.marker1 = marker1;
         this.marker2 = marker2;
         PositionUtil.LineSkewResult interactionPoint = PositionUtil.findLineSkewPoint(
            new PositionUtil.Line(VecUtil.convertCenter(marker1), VecUtil.convertCenter(marker2)), playerPos, playerEndPos
         );
         this.distToPoint = interactionPoint.closestPos.distanceTo(playerPos);
         this.distToLine = interactionPoint.distFromLine;
      }

      public boolean didInteract() {
         return this.distToPoint <= 3.0 && this.distToLine < 0.3;
      }

      public ItemMarkerConnector.MarkerLineInteraction getBetter(ItemMarkerConnector.MarkerLineInteraction other) {
         if (other == null) {
            return this;
         } else if (other.marker1 == this.marker2 && other.marker2 == this.marker1) {
            return other;
         } else if (other.distToLine < this.distToLine) {
            return other;
         } else if (other.distToLine > this.distToLine) {
            return this;
         } else {
            return other.distToPoint < this.distToPoint ? other : this;
         }
      }
   }
}
