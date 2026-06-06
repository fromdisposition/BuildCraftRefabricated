package buildcraft.core.marker.volume;

import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.data.Box;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class VolumeBox {
   public final Level world;
   public UUID id;
   public Box box;
   private UUID player = null;
   private UUID oldPlayer = null;
   private BlockPos held = null;
   private double dist = 0.0;
   private BlockPos oldMin = null;
   private BlockPos oldMax = null;
   public final Map<EnumAddonSlot, Addon> addons = new EnumMap<>(EnumAddonSlot.class);
   public final List<Lock> locks = new ArrayList<>();

   public VolumeBox(Level world, BlockPos at) {
      if (world == null) {
         throw new NullPointerException("world");
      }

      this.world = world;
      this.id = UUID.randomUUID();
      this.box = new Box(at, at);
   }

   public VolumeBox(Level world, CompoundTag nbt) {
      if (world == null) {
         throw new NullPointerException("world");
      }

      this.world = world;
      String idStr = nbt.getString("id").orElse("");

      try {
         this.id = idStr.isEmpty() ? UUID.randomUUID() : UUID.fromString(idStr);
      } catch (IllegalArgumentException e) {
         BCLog.logger.warn("[core.volume] Invalid volume box id '{}', generating new", idStr);
         this.id = UUID.randomUUID();
      }

      this.box = new Box();
      this.box.initialize((CompoundTag)nbt.getCompound("box").orElseGet(CompoundTag::new));
      this.player = nbt.contains("player") ? UUID.fromString(nbt.getString("player").orElse("")) : null;
      this.oldPlayer = nbt.contains("oldPlayer") ? UUID.fromString(nbt.getString("oldPlayer").orElse("")) : null;
      if (nbt.contains("held")) {
         CompoundTag heldTag = (CompoundTag)nbt.getCompound("held").orElseGet(CompoundTag::new);
         this.held = new BlockPos(heldTag.getInt("X").orElse(0), heldTag.getInt("Y").orElse(0), heldTag.getInt("Z").orElse(0));
      }

      this.dist = nbt.getDouble("dist").orElse(0.0);
      if (nbt.contains("oldMin")) {
         CompoundTag oldMinTag = (CompoundTag)nbt.getCompound("oldMin").orElseGet(CompoundTag::new);
         this.oldMin = new BlockPos(oldMinTag.getInt("X").orElse(0), oldMinTag.getInt("Y").orElse(0), oldMinTag.getInt("Z").orElse(0));
      }

      if (nbt.contains("oldMax")) {
         CompoundTag oldMaxTag = (CompoundTag)nbt.getCompound("oldMax").orElseGet(CompoundTag::new);
         this.oldMax = new BlockPos(oldMaxTag.getInt("X").orElse(0), oldMaxTag.getInt("Y").orElse(0), oldMaxTag.getInt("Z").orElse(0));
      }

      if (nbt.contains("addons")) {
         ListTag addonsList = (ListTag)nbt.getList("addons").orElseGet(ListTag::new);

         for (int i = 0; i < addonsList.size(); i++) {
            CompoundTag addonsEntryTag = (CompoundTag)addonsList.getCompound(i).orElseGet(CompoundTag::new);
            String addonClassName = addonsEntryTag.getString("addonClass").orElse("");

            try {
               Class<? extends Addon> addonClass = AddonsRegistry.INSTANCE.getClassByName(Identifier.parse(addonClassName));
               Addon addon = addonClass.getDeclaredConstructor().newInstance();
               addon.volumeBox = this;
               addon.readFromNBT((CompoundTag)addonsEntryTag.getCompound("addonData").orElseGet(CompoundTag::new));
               String slotStr = addonsEntryTag.getString("slot").orElse("");
               EnumAddonSlot slot = EnumAddonSlot.valueOf(slotStr);
               this.addons.put(slot, addon);
               addon.postReadFromNbt();
            } catch (Exception e) {
               BCLog.logger.warn("[core.volume] Failed to load a volume box addon from NBT", e);
            }
         }
      }

      if (nbt.contains("locks")) {
         ListTag locksList = (ListTag)nbt.getList("locks").orElseGet(ListTag::new);

         for (int i = 0; i < locksList.size(); i++) {
            CompoundTag lockTag = (CompoundTag)locksList.getCompound(i).orElseGet(CompoundTag::new);

            try {
               Lock lock = new Lock();
               lock.readFromNBT(lockTag);
               this.locks.add(lock);
            } catch (RuntimeException e) {
               BCLog.logger.warn("[core.volume] Skipping invalid volume box lock entry", e);
            }
         }
      }
   }

   public boolean isEditing() {
      return this.player != null;
   }

   private void resetEditing() {
      this.oldMin = this.oldMax = null;
      this.held = null;
      this.dist = 0.0;
   }

   public void cancelEditing() {
      this.player = null;
      this.box.reset();
      this.box.extendToEncompass(this.oldMin);
      this.box.extendToEncompass(this.oldMax);
      this.resetEditing();
   }

   public void confirmEditing() {
      this.player = null;
      this.resetEditing();
      this.addons.values().forEach(Addon::onVolumeBoxSizeChange);
   }

   public void pauseEditing() {
      this.oldPlayer = this.player;
      this.player = null;
   }

   public void resumeEditing() {
      this.player = this.oldPlayer;
      this.oldPlayer = null;
   }

   public void setPlayer(Player player) {
      this.player = player.getGameProfile().id();
   }

   public boolean isEditingBy(Player player) {
      return player != null && Objects.equals(this.player, player.getGameProfile().id());
   }

   public boolean isPausedEditingBy(Player player) {
      return this.oldPlayer != null && Objects.equals(this.oldPlayer, player.getGameProfile().id());
   }

   public Player getPlayer(Level world) {
      return world.getPlayerByUUID(this.player);
   }

   public void setHeldDistOldMinOldMax(BlockPos held, double dist, BlockPos oldMin, BlockPos oldMax) {
      this.held = held;
      this.dist = dist;
      this.oldMin = oldMin;
      this.oldMax = oldMax;
   }

   public BlockPos getHeld() {
      return this.held;
   }

   public double getDist() {
      return this.dist;
   }

   public Stream<Lock.Target> getLockTargetsStream() {
      return this.locks.stream().flatMap(lock -> lock.targets.stream());
   }

   public CompoundTag writeToNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.putString("id", this.id.toString());
      nbt.put("box", this.box.writeToNBT());
      if (this.player != null) {
         nbt.putString("player", this.player.toString());
      }

      if (this.oldPlayer != null) {
         nbt.putString("oldPlayer", this.oldPlayer.toString());
      }

      if (this.held != null) {
         CompoundTag heldTag = new CompoundTag();
         heldTag.putInt("X", this.held.getX());
         heldTag.putInt("Y", this.held.getY());
         heldTag.putInt("Z", this.held.getZ());
         nbt.put("held", heldTag);
      }

      nbt.putDouble("dist", this.dist);
      if (this.oldMin != null) {
         CompoundTag oldMinTag = new CompoundTag();
         oldMinTag.putInt("X", this.oldMin.getX());
         oldMinTag.putInt("Y", this.oldMin.getY());
         oldMinTag.putInt("Z", this.oldMin.getZ());
         nbt.put("oldMin", oldMinTag);
      }

      if (this.oldMax != null) {
         CompoundTag oldMaxTag = new CompoundTag();
         oldMaxTag.putInt("X", this.oldMax.getX());
         oldMaxTag.putInt("Y", this.oldMax.getY());
         oldMaxTag.putInt("Z", this.oldMax.getZ());
         nbt.put("oldMax", oldMaxTag);
      }

      ListTag addonsList = new ListTag();
      this.addons.entrySet().forEach(entry -> {
         CompoundTag addonsEntryTag = new CompoundTag();
         addonsEntryTag.putString("slot", entry.getKey().name());
         addonsEntryTag.putString("addonClass", AddonsRegistry.INSTANCE.getNameByClass((Class<? extends Addon>)entry.getValue().getClass()).toString());
         addonsEntryTag.put("addonData", entry.getValue().writeToNBT(new CompoundTag()));
         addonsList.add(addonsEntryTag);
      });
      nbt.put("addons", addonsList);
      ListTag locksList = new ListTag();

      for (Lock lock : this.locks) {
         locksList.add(lock.writeToNBT());
      }

      nbt.put("locks", locksList);
      return nbt;
   }

   @Override
   public boolean equals(Object o) {
      return this == o || o != null && this.getClass() == o.getClass() && this.id.equals(((VolumeBox)o).id);
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }
}
