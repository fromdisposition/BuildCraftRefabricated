/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.marker.volume;

import buildcraft.lib.sync.ClientStateMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public enum ClientVolumeBoxes implements ClientStateMirror {
   INSTANCE;

   public final List<VolumeBox> volumeBoxes = new ArrayList<>();

   public void applyFullSync(Level world, List<CompoundTag> fullTags) {
      Set<UUID> previousIds = new HashSet<>();

      for (VolumeBox vb : this.volumeBoxes) {
         previousIds.add(vb.id);
      }

      List<VolumeBox> rebuilt = new ArrayList<>(fullTags.size());

      for (CompoundTag tag : fullTags) {
         rebuilt.add(new VolumeBox(world, tag));
      }

      this.volumeBoxes.clear();
      this.volumeBoxes.addAll(rebuilt);
      notifyAddedAddons(rebuilt, previousIds);
   }

   public void applyDelta(Level world, List<UUID> removedIds, List<CompoundTag> changedTags) {
      Set<UUID> previousIds = new HashSet<>();

      for (VolumeBox vb : this.volumeBoxes) {
         previousIds.add(vb.id);
      }

      for (UUID removed : removedIds) {
         this.volumeBoxes.removeIf(vb -> vb.id.equals(removed));
      }

      List<VolumeBox> added = new ArrayList<>();

      for (CompoundTag tag : changedTags) {
         VolumeBox updated = new VolumeBox(world, tag);
         this.volumeBoxes.removeIf(vb -> vb.id.equals(updated.id));
         this.volumeBoxes.add(updated);
         if (!previousIds.contains(updated.id)) {
            added.add(updated);
         }
      }

      notifyAddedAddons(added, Set.of());
   }

   private static void notifyAddedAddons(List<VolumeBox> boxes, Set<UUID> previousIds) {
      for (VolumeBox vb : boxes) {
         if (!previousIds.contains(vb.id)) {
            for (Addon addon : vb.addons.values()) {
               if (addon != null) {
                  addon.onAdded();
               }
            }
         }
      }
   }

   @Override
   public void applyFullSync(Runnable fullReplace) {
      fullReplace.run();
   }

   @Override
   public void applyDelta(Runnable deltaApply) {
      deltaApply.run();
   }
}
