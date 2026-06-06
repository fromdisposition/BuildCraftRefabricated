package buildcraft.factory.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import buildcraft.lib.integration.jei.JeiTransferUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.ItemHandlerSimple;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ContainerHeatExchange extends BcMenu {
   private static final ItemHandlerSimple FALLBACK_SLOTS = createFallbackSlots();
   @Nullable
   public final TileHeatExchange tile;
   public final WidgetFluidTank widgetTankStartInput;
   public final WidgetFluidTank widgetTankStartOutput;
   public final WidgetFluidTank widgetTankEndInput;
   public final WidgetFluidTank widgetTankEndOutput;

   public ContainerHeatExchange(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, resolveStartTile(MenuBlockEntityLookup.get(playerInv, pos, TileHeatExchange.class)));
   }

   public ContainerHeatExchange(int containerId, Inventory playerInv, @Nullable TileHeatExchange tile) {
      super(BCFactoryMenuTypes.HEAT_EXCHANGE, containerId, playerInv.player);
      this.tile = tile;
      ItemHandlerSimple machineSlots = tile != null ? tile.containerSlots : FALLBACK_SLOTS;
      this.addSlot(new SlotBase(machineSlots, 0, 8, 23));
      this.addSlot(new SlotBase(machineSlots, 1, 8, 64));
      this.addSlot(new SlotBase(machineSlots, 2, 152, 12));
      this.addSlot(new SlotBase(machineSlots, 3, 152, 54));
      this.addFullPlayerInventory(8, 89);
      TileHeatExchange.ExchangeSectionStart start = startSection(tile);
      TileHeatExchange.ExchangeSectionEnd end = start != null ? start.getEndSection() : null;
      this.widgetTankStartInput = this.addWidget(new WidgetFluidTank(this, start != null ? start.tankInput : null));
      this.widgetTankStartOutput = this.addWidget(new WidgetFluidTank(this, start != null ? start.tankOutput : null));
      this.widgetTankEndInput = this.addWidget(new WidgetFluidTank(this, end != null ? end.tankInput : null));
      this.widgetTankEndOutput = this.addWidget(new WidgetFluidTank(this, end != null ? end.tankOutput : null));
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 103 && !isClient && this.tile != null) {
         int count = buffer.readVarInt();

         for (int i = 0; i < count; i++) {
            int slot = buffer.readVarInt();
            Item bucket = (Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(buffer.readUtf()));
            JeiTransferUtil.moveBucketToSlot(this.player.getInventory(), bucket, this.tile.containerSlots, slot);
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
      }
   }

   @Nullable
   public TileHeatExchange.ExchangeSectionStart startSection() {
      return startSection(this.tile);
   }

   @Nullable
   public TileHeatExchange.ExchangeSectionEnd endSection() {
      TileHeatExchange.ExchangeSectionStart start = this.startSection();
      return start != null ? start.getEndSection() : null;
   }

   @Nullable
   private static TileHeatExchange.ExchangeSectionStart startSection(@Nullable TileHeatExchange tile) {
      if (tile == null) {
         return null;
      } else {
         return tile.getSection() instanceof TileHeatExchange.ExchangeSectionStart s ? s : null;
      }
   }

   @Nullable
   private static TileHeatExchange resolveStartTile(@Nullable TileHeatExchange exchange) {
      return exchange != null ? exchange.findStart() : null;
   }

   private static ItemHandlerSimple createFallbackSlots() {
      ItemHandlerSimple slots = new ItemHandlerSimple(4, 1);
      slots.setChecker((slot, stack) -> false);
      return slots;
   }

   @Override
   public boolean stillValid(Player player) {
      if (this.tile == null) {
         return false;
      } else {
         return this.tile.getLevel() != null && this.tile.getLevel().getBlockEntity(this.tile.getBlockPos()) == this.tile
            ? player.distanceToSqr(this.tile.getBlockPos().getX() + 0.5, this.tile.getBlockPos().getY() + 0.5, this.tile.getBlockPos().getZ() + 0.5) <= 64.0
            : false;
      }
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (index < 4) {
            if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, itemstack1);
      }

      return itemstack;
   }
}
