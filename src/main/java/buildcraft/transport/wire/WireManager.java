package buildcraft.transport.wire;

import buildcraft.api.transport.EnumWirePart;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WireManager implements IWireManager {
   private final IPipeHolder holder;
   public final Map<EnumWirePart, DyeColor> parts = new EnumMap<>(EnumWirePart.class);
   public final Set<EnumWirePart> poweredClient = EnumSet.noneOf(EnumWirePart.class);
   public final Map<EnumWireBetween, DyeColor> betweens = new EnumMap<>(EnumWireBetween.class);
   public boolean initialised = false;
   private long poweredCacheTick = -1L;
   private final EnumMap<EnumWirePart, Boolean> poweredServerCache = new EnumMap<>(EnumWirePart.class);

   public WireManager(IPipeHolder holder) {
      this.holder = holder;
   }

   public SavedDataWireSystems getWireSystems() {
      return SavedDataWireSystems.get(this.holder.getPipeWorld());
   }

   @Override
   public IPipeHolder getHolder() {
      return this.holder;
   }

   public void invalidate() {
      if (!this.holder.getPipeWorld().isClientSide()) {
         this.removePartsFromSystem(this.parts.keySet());
      }

      this.invalidatePoweredCache();
   }

   private void invalidatePoweredCache() {
      this.poweredCacheTick = -1L;
      this.poweredServerCache.clear();
   }

   public void validate() {
      if (!this.holder.getPipeWorld().isClientSide()) {
         this.initialised = false;
      }
   }

   public void tick() {
      if (!this.initialised) {
         this.initialised = true;
         if (!this.holder.getPipeWorld().isClientSide()) {
            for (EnumWirePart part : this.parts.keySet()) {
               this.getWireSystems().buildAndAddWireSystem(new WireSystem.WireElement(this.holder.getPipePos(), part));
            }
         }

         this.updateBetweens(false);
      }
   }

   @Override
   public boolean addPart(EnumWirePart part, DyeColor colour) {
      if (this.getColorOfPart(part) == null) {
         this.parts.put(part, colour);
         if (!this.holder.getPipeWorld().isClientSide()) {
            this.getWireSystems().buildAndAddWireSystem(new WireSystem.WireElement(this.holder.getPipePos(), part));
            this.holder.getPipeTile().setChanged();
         }

         this.updateBetweens(false);
         this.invalidatePoweredCache();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public DyeColor removePart(EnumWirePart part) {
      DyeColor color = this.getColorOfPart(part);
      if (color == null) {
         return null;
      }

      this.parts.remove(part);
      if (!this.holder.getPipeWorld().isClientSide()) {
         WireSystem.WireElement element = new WireSystem.WireElement(this.holder.getPipePos(), part);
         WireSystem.getConnectedElementsOfElement(this.holder, element).forEach(this.getWireSystems()::buildAndAddWireSystem);
         this.getWireSystems().getWireSystemsWithElement(element).forEach(this.getWireSystems()::removeWireSystem);
         this.holder.getPipeTile().setChanged();
      }

      this.updateBetweens(false);
      this.invalidatePoweredCache();
      return color;
   }

   public void removeParts(Collection<EnumWirePart> toRemove) {
      toRemove.forEach(this.parts::remove);
      if (!this.holder.getPipeWorld().isClientSide()) {
         this.removePartsFromSystem(toRemove);
      }

      this.updateBetweens(false);
   }

   private void removePartsFromSystem(Collection<EnumWirePart> toRemove) {
      toRemove.stream()
         .map(part -> new WireSystem.WireElement(this.holder.getPipePos(), part))
         .flatMap(element -> WireSystem.getConnectedElementsOfElement(this.holder, element).stream())
         .distinct()
         .forEach(this.getWireSystems()::buildAndAddWireSystem);
      toRemove.stream()
         .map(part -> new WireSystem.WireElement(this.holder.getPipePos(), part))
         .flatMap(element -> this.getWireSystems().getWireSystemsWithElement(element).stream())
         .forEach(this.getWireSystems()::removeWireSystem);
      this.holder.getPipeTile().setChanged();
   }

   @Override
   public void updateBetweens(boolean recursive) {
      this.betweens.clear();
      this.parts
         .forEach(
            (part, color) -> {
               for (EnumWireBetween between : EnumWireBetween.VALUES) {
                  EnumWirePart[] betweenParts = between.parts;
                  if (between.to == null) {
                     if (betweenParts[0] == part && this.getColorOfPart(betweenParts[1]) == color
                        || betweenParts[1] == part && this.getColorOfPart(betweenParts[0]) == color) {
                        this.betweens.put(between, color);
                     }
                  } else if (WireSystem.canWireConnect(this.holder, between.to)) {
                     IPipe pipe = this.holder.getNeighbourPipe(between.to);
                     if (pipe != null) {
                        IWireManager wireManager = pipe.getHolder().getWireManager();
                        if (betweenParts[0] == part && wireManager.getColorOfPart(betweenParts[1]) == color) {
                           this.betweens.put(between, color);
                        }
                     }
                  }
               }
            }
         );
      if (!recursive && !this.holder.getPipeWorld().isClientSide()) {
         for (Direction side : Direction.values()) {
            BlockEntity tile = this.holder.getPipeWorld().getBlockEntity(this.holder.getPipePos().relative(side));
            if (tile instanceof IPipeHolder) {
               ((IPipeHolder)tile).getWireManager().updateBetweens(true);
            }
         }
      }
   }

   @Override
   public DyeColor getColorOfPart(EnumWirePart part) {
      return this.parts.get(part);
   }

   @Override
   public boolean hasPartOfColor(DyeColor color) {
      return this.parts.values().contains(color);
   }

   @Override
   public boolean isPowered(EnumWirePart part) {
      if (this.holder.getPipeWorld().isClientSide()) {
         return this.poweredClient.contains(part);
      }

      this.refreshPoweredServerCache();
      return this.poweredServerCache.getOrDefault(part, false);
   }

   private void refreshPoweredServerCache() {
      long tick = this.holder.getPipeWorld().getGameTime();
      if (this.poweredCacheTick != tick) {
         this.poweredCacheTick = tick;
         this.poweredServerCache.clear();

         for (EnumWirePart part : this.parts.keySet()) {
            this.poweredServerCache.put(part, this.computeServerPowered(part));
         }
      }
   }

   private boolean computeServerPowered(EnumWirePart part) {
      SavedDataWireSystems wireSystems = this.getWireSystems();
      List<WireSystem> wireSystemsWithElement = wireSystems.getWireSystemsWithElementAsReadOnlyList(new WireSystem.WireElement(this.holder.getPipePos(), part));
      if (!wireSystemsWithElement.isEmpty()) {
         for (WireSystem wireSystem : wireSystemsWithElement) {
            Boolean powered = wireSystems.wireSystems.get(wireSystem);
            if (powered != null && powered) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean isAnyPowered(DyeColor color) {
      if (!this.parts.isEmpty()) {
         for (Entry<EnumWirePart, DyeColor> partColor : this.parts.entrySet()) {
            if (partColor.getValue() == color && this.isPowered(partColor.getKey())) {
               return true;
            }
         }
      }

      return false;
   }

   public CompoundTag writeToNbt() {
      CompoundTag nbt = new CompoundTag();
      int[] wiresArray = new int[this.parts.size() * 2];
      int[] i = new int[]{0};
      this.parts.forEach((part, color) -> {
         wiresArray[i[0]] = part.ordinal();
         wiresArray[i[0] + 1] = color.getId();
         i[0] += 2;
      });
      nbt.putIntArray("parts", wiresArray);
      return nbt;
   }

   public void readFromNbt(CompoundTag nbt) {
      this.parts.clear();
      int[] wiresArray = nbt.getIntArray("parts").orElse(new int[0]);

      for (int i = 0; i < wiresArray.length; i += 2) {
         this.parts.put(EnumWirePart.VALUES[wiresArray[i]], DyeColor.byId(wiresArray[i + 1]));
      }
   }
}
