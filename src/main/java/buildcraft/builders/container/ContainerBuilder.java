/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.EnumContainerContentsMode;
import buildcraft.builders.snapshot.EnumFluidHandlingMode;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import buildcraft.lib.net.PacketBufferBC;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ContainerBuilder extends ContainerBCTile<TileBuilder> {
   private static final int DATA_CAN_EXCAVATE = 0;
   private static final int DATA_SNAPSHOT_TYPE = 1;
   private static final int DATA_LEFT_TO_BREAK = 2;
   private static final int DATA_LEFT_TO_PLACE = 3;
   private static final int DATA_FLUID_MODE = 4;
   private static final int DATA_CONTENTS_MODE = 5;
   private static final int DATA_COUNT = 6;
   private static final int NET_DISPLAY_LIST = 10;
   private static final int NET_TANK_LEVELS = 11;
   public static final int NET_FLUID_MODE_CLICK = 12;
   public static final int NET_CONTENTS_MODE_CLICK = 13;
   private final ContainerData data;
   public final List<WidgetFluidTank> widgetTanks;
   private List<ItemStack> lastBroadcastDisplay = new ArrayList<>();
   private final List<ItemStack> syncedDisplay = new ArrayList<>();
   private final ContainerBuilder.FluidSnapshot[] lastBroadcastTanks = new ContainerBuilder.FluidSnapshot[4];

   public ContainerBuilder(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerBuilder(int containerId, Inventory playerInv, final TileBuilder tile) {
      super(BCBuildersMenuTypes.BUILDER, containerId, playerInv.player, tile);
      if (tile != null && tile.getLevel() != null && !tile.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0 -> tile.canExcavate() ? 1 : 0;
                  case 1 -> tile.snapshotType == null ? -1 : tile.snapshotType.ordinal();
                  case 2 -> tile.getBuilder() != null ? tile.getBuilder().leftToBreak : 0;
                  case 3 -> tile.getBuilder() != null ? tile.getBuilder().leftToPlace : 0;
                  case 4 -> tile.getFluidMode().ordinal();
                  case 5 -> tile.getContainerContentsMode().ordinal();
                  default -> 0;
               };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
               return 6;
            }
         };
      } else {
         this.data = new SimpleContainerData(6);
      }

      this.addDataSlots(this.data);
      ContainerBuilder.BuilderContainer slotContainer = new ContainerBuilder.BuilderContainer(tile);
      this.addSlot(new ContainerBuilder.SnapshotInputSlot(slotContainer, 0, 80, 27));

      for (int sy = 0; sy < 3; sy++) {
         for (int sx = 0; sx < 9; sx++) {
            this.addSlot(new Slot(slotContainer, 1 + sx + sy * 9, 8 + sx * 18, 72 + sy * 18));
         }
      }

      for (int y = 0; y < 6; y++) {
         for (int x = 0; x < 4; x++) {
            this.addSlot(new SlotDisplay(this::getDisplay, x + y * 4, 179 + x * 18, 18 + y * 18));
         }
      }

      List<WidgetFluidTank> widgets = new ArrayList<>(4);
      if (tile != null) {
         for (int i = 0; i < 4; i++) {
            widgets.add(this.addWidget(new WidgetFluidTank(this, tile.getTank(i))));
         }
      }

      this.widgetTanks = List.copyOf(widgets);
      this.addFullPlayerInventory(8, 140, playerInv);
   }

   private static TileBuilder getTile(Inventory playerInv, BlockPos pos) {
      return playerInv.player.level() != null && playerInv.player.level().getBlockEntity(pos) instanceof TileBuilder builder ? builder : null;
   }

   private ItemStack getDisplay(int index) {
      if (this.tile != null && this.tile.getLevel() != null && this.tile.getLevel().isClientSide()) {
         return index >= 0 && index < this.syncedDisplay.size() ? this.syncedDisplay.get(index) : ItemStack.EMPTY;
      } else if (this.tile != null && this.tile.snapshotType == EnumSnapshotType.BLUEPRINT) {
         List<ItemStack> live = this.tile.blueprintBuilder.remainingDisplayRequired;
         return index >= 0 && index < live.size() ? live.get(index) : ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      if (this.tile != null && this.tile.getLevel() != null && !this.tile.getLevel().isClientSide()) {
         this.broadcastDisplayList();
         this.broadcastTankLevels();
      }
   }

   private void broadcastDisplayList() {
      List<ItemStack> current = this.tile.snapshotType == EnumSnapshotType.BLUEPRINT && this.tile.blueprintBuilder != null
         ? this.tile.blueprintBuilder.remainingDisplayRequired
         : List.of();
      if (!displayListEquals(current, this.lastBroadcastDisplay)) {
         List<ItemStack> snap = new ArrayList<>(current.size());

         for (ItemStack s : current) {
            snap.add(s.copy());
         }

         this.lastBroadcastDisplay = snap;
         RegistryAccess registries = this.tile.getLevel().registryAccess();
         this.sendMessage(10, buf -> {
            RegistryFriendlyByteBuf rbuf = new RegistryFriendlyByteBuf(buf, registries);
            rbuf.writeVarInt(snap.size());

            for (ItemStack stack : snap) {
               ItemStack.OPTIONAL_STREAM_CODEC.encode(rbuf, stack);
            }
         });
      }
   }

   private void broadcastTankLevels() {
      ContainerBuilder.FluidSnapshot[] current = new ContainerBuilder.FluidSnapshot[4];
      boolean changed = false;

      for (int i = 0; i < 4; i++) {
         SingleFluidTank t = this.tile.getTank(i);
         if (t == null) {
            current[i] = ContainerBuilder.FluidSnapshot.EMPTY;
         } else {
            FluidStack res = t.getFluidStack();
            int amount = t.getAmountMb();
            if (!res.isEmpty() && amount != 0) {
               current[i] = new ContainerBuilder.FluidSnapshot(BuiltInRegistries.FLUID.getKey(res.getFluid()), amount);
            } else {
               current[i] = ContainerBuilder.FluidSnapshot.EMPTY;
            }

            ContainerBuilder.FluidSnapshot last = this.lastBroadcastTanks[i];
            if (last == null || !last.matches(current[i])) {
               changed = true;
            }
         }
      }

      if (changed) {
         for (int i = 0; i < 4; i++) {
            this.lastBroadcastTanks[i] = current[i];
         }

         this.sendMessage(11, buf -> {
            for (int ix = 0; ix < 4; ix++) {
               ContainerBuilder.FluidSnapshot fs = current[ix];
               if (fs.fluidId != null && fs.amount > 0) {
                  buf.writeUtf(fs.fluidId.toString());
                  buf.writeVarInt(fs.amount);
               } else {
                  buf.writeUtf("");
                  buf.writeVarInt(0);
               }
            }
         });
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 12 && !isClient) {
         if (this.tile != null) {
            this.tile.cycleFluidMode();
         }
      } else if (id == 13 && !isClient) {
         if (this.tile != null) {
            this.tile.cycleContainerContentsMode();
         }
      } else if (id == 10 && isClient) {
         RegistryFriendlyByteBuf rbuf = new RegistryFriendlyByteBuf(buffer, ctx.player().level().registryAccess());
         int count = rbuf.readVarInt();
         List<ItemStack> newList = new ArrayList<>(count);

         for (int i = 0; i < count; i++) {
            ItemStack stack = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(rbuf);
            if (!stack.isEmpty()) {
               newList.add(stack);
            }
         }

         this.syncedDisplay.clear();
         this.syncedDisplay.addAll(newList);
      } else if (id == 11 && isClient) {
         for (int i = 0; i < 4; i++) {
            String fluidIdStr = buffer.readUtf();
            int amount = buffer.readVarInt();
            if (this.tile != null) {
               SingleFluidTank handler = this.tile.getTank(i);
               if (handler != null) {
                  applyTankState(handler, fluidIdStr, amount);
               }
            }
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
      }
   }

   private static void applyTankState(SingleFluidTank handler, String fluidIdStr, int amount) {
      if (!fluidIdStr.isEmpty() && amount > 0) {
         Identifier fluidId = Identifier.tryParse(fluidIdStr);
         if (fluidId != null) {
            Fluid fluid = (Fluid)BuiltInRegistries.FLUID.getValue(fluidId);
            if (fluid != null && fluid != Fluids.EMPTY) {
               handler.setContents(new FluidStack(fluid, amount));
               return;
            }
         }
      }

      handler.setContents(FluidStack.EMPTY);
   }

   private static boolean displayListEquals(List<ItemStack> a, List<ItemStack> b) {
      if (a.size() != b.size()) {
         return false;
      }

      for (int i = 0; i < a.size(); i++) {
         ItemStack sa = a.get(i);
         ItemStack sb = b.get(i);
         if (!ItemStack.isSameItemSameComponents(sa, sb) || sa.getCount() != sb.getCount()) {
            return false;
         }
      }

      return true;
   }

   public boolean getSyncedCanExcavate() {
      return this.data.get(0) != 0;
   }

   public int getSyncedSnapshotType() {
      return this.data.get(1);
   }

   public int getSyncedLeftToBreak() {
      return this.data.get(2);
   }

   public int getSyncedLeftToPlace() {
      return this.data.get(3);
   }

   public EnumFluidHandlingMode getSyncedFluidMode() {
      return EnumFluidHandlingMode.fromOrdinal(this.data.get(4));
   }

   public EnumContainerContentsMode getSyncedContentsMode() {
      return EnumContainerContentsMode.fromOrdinal(this.data.get(5));
   }

   private static class BuilderContainer implements Container {
      private final TileBuilder tile;

      BuilderContainer(TileBuilder tile) {
         this.tile = tile;
      }

      public int getContainerSize() {
         return 28;
      }

      public boolean isEmpty() {
         if (this.tile == null) {
            return true;
         }

         if (!this.tile.getSnapshot().isEmpty()) {
            return false;
         }

         for (int i = 0; i < 27; i++) {
            if (!this.tile.getResource(i).isEmpty()) {
               return false;
            }
         }

         return true;
      }

      public ItemStack getItem(int slot) {
         if (this.tile == null) {
            return ItemStack.EMPTY;
         } else {
            return slot == 0 ? this.tile.getSnapshot() : this.tile.getResource(slot - 1);
         }
      }

      public ItemStack removeItem(int slot, int count) {
         if (this.tile == null) {
            return ItemStack.EMPTY;
         }

         ItemStack current = this.getItem(slot);
         if (current.isEmpty()) {
            return ItemStack.EMPTY;
         }

         ItemStack result = current.split(count);
         if (current.isEmpty()) {
            this.setItem(slot, ItemStack.EMPTY);
         } else {
            this.setItem(slot, current);
         }

         return result;
      }

      public ItemStack removeItemNoUpdate(int slot) {
         if (this.tile == null) {
            return ItemStack.EMPTY;
         }

         ItemStack current = this.getItem(slot);
         this.setItem(slot, ItemStack.EMPTY);
         return current;
      }

      public void setItem(int slot, ItemStack stack) {
         if (this.tile != null) {
            if (slot == 0) {
               this.tile.setSnapshot(stack);
            } else {
               this.tile.setResource(slot - 1, stack);
            }
         }
      }

      public void setChanged() {
         if (this.tile != null) {
            this.tile.setChanged();
         }
      }

      public boolean stillValid(Player player) {
         return true;
      }

      public void clearContent() {
         if (this.tile != null) {
            this.tile.setSnapshot(ItemStack.EMPTY);

            for (int i = 0; i < 27; i++) {
               this.tile.setResource(i, ItemStack.EMPTY);
            }
         }
      }
   }

   private record FluidSnapshot(Identifier fluidId, int amount) {
      static final ContainerBuilder.FluidSnapshot EMPTY = new ContainerBuilder.FluidSnapshot(null, 0);

      boolean matches(ContainerBuilder.FluidSnapshot other) {
         if (this == other) {
            return true;
         } else if (this.amount != other.amount) {
            return false;
         } else {
            return this.fluidId == null ? other.fluidId == null : this.fluidId.equals(other.fluidId);
         }
      }
   }

   private static class SnapshotInputSlot extends Slot {
      public SnapshotInputSlot(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      public boolean mayPlace(ItemStack stack) {
         return stack.getItem() instanceof ItemSnapshot item ? item.isUsed() : false;
      }

      public int getMaxStackSize() {
         return 1;
      }
   }
}
