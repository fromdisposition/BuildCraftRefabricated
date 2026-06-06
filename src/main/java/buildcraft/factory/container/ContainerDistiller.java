package buildcraft.factory.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileDistiller_BC8;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.widget.WidgetFluidTank;
import buildcraft.lib.integration.jei.JeiTransferUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ContainerDistiller extends BcMenu {
   private static final ItemHandlerSimple FALLBACK_SLOTS = createFallbackSlots();
   public final TileDistiller_BC8 tile;
   public final WidgetFluidTank widgetTankIn;
   public final WidgetFluidTank widgetTankGasOut;
   public final WidgetFluidTank widgetTankLiquidOut;

   public ContainerDistiller(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileDistiller_BC8.class));
   }

   public ContainerDistiller(int containerId, Inventory playerInv, TileDistiller_BC8 tile) {
      super(BCFactoryMenuTypes.DISTILLER, containerId, playerInv.player);
      this.tile = tile;
      ItemHandlerSimple machineSlots = tile != null ? tile.containerSlots : FALLBACK_SLOTS;
      this.addSlot(new SlotBase(machineSlots, 0, 8, 35));
      this.addSlot(new SlotBase(machineSlots, 1, 152, 10));
      this.addSlot(new SlotBase(machineSlots, 2, 152, 55));
      this.addFullPlayerInventory(8, 79);
      this.widgetTankIn = this.addWidget(new WidgetFluidTank(this, tile != null ? tile.getTankIn() : null));
      this.widgetTankGasOut = this.addWidget(new WidgetFluidTank(this, tile != null ? tile.getTankGasOut() : null));
      this.widgetTankLiquidOut = this.addWidget(new WidgetFluidTank(this, tile != null ? tile.getTankLiquidOut() : null));
   }

   private static ItemHandlerSimple createFallbackSlots() {
      ItemHandlerSimple slots = new ItemHandlerSimple(3, 1);
      slots.setChecker((slot, stack) -> false);
      return slots;
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
         if (index < 3) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
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
