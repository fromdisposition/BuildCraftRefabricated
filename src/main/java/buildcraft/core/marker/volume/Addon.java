package buildcraft.core.marker.volume;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public abstract class Addon {
   public VolumeBox volumeBox;

   public abstract IFastAddonRenderer<? extends Addon> getRenderer();

   public EnumAddonSlot getSlot() {
      return this.volumeBox
         .addons
         .entrySet()
         .stream()
         .filter(slotAddon -> slotAddon.getValue() == this)
         .findFirst()
         .orElseThrow(IllegalStateException::new)
         .getKey();
   }

   public AABB getBoundingBox() {
      return this.getSlot().getBoundingBox(this.volumeBox);
   }

   public boolean canBePlaceInto(VolumeBox volumeBox) {
      return !(this instanceof ISingleAddon) || !volumeBox.addons.values().stream().anyMatch(addon -> addon.getClass() == this.getClass());
   }

   public void onAdded() {
   }

   public void onRemoved() {
   }

   public void onVolumeBoxSizeChange() {
   }

   public void onPlayerRightClick(Player player) {
   }

   public abstract CompoundTag writeToNBT(CompoundTag var1);

   public abstract void readFromNBT(CompoundTag var1);

   public void postReadFromNbt() {
   }
}
