/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.gui;

import buildcraft.api.core.BCLog;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.fabric.BcRegistryUtil;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.BcEnvelopeCodec;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.MessageContainerPayload;
import buildcraft.lib.recipe.BucketRecipeTransfer;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

final class BcMenuSupport {
   interface StackMover {
      boolean move(ItemStack stack, int startIndex, int endIndex, boolean reverse);
   }

   private BcMenuSupport() {
   }

   static void sendMessage(AbstractContainerMenu menu, Player player, int id, IPayloadWriter writer) {
      byte[] bytes = BcEnvelopeCodec.encode(writer);
      if (bytes == null) {
         BCLog.logger.warn("[lib.container] Container message {} exceeds payload limit", id);
      } else {
         MessageContainerPayload payload = new MessageContainerPayload(menu.containerId, id, bytes);
         if (player.level().isClientSide()) {
            BcPacketDistributor.sendToServer(payload);
         } else if (player instanceof ServerPlayer serverPlayer) {
            BcPacketDistributor.sendToPlayer(serverPlayer, payload);
         }
      }
   }

   static void sendWidgetData(IBcMenu menu, List<Widget_Neptune<?>> widgets, Widget_Neptune<?> widget, IPayloadWriter writer) {
      int widgetId = widgets.indexOf(widget);
      if (widgetId == -1) {
         BCLog.logger.warn("[lib.container] sendWidgetData: widget not found! (" + (widget == null ? "null" : widget.getClass()) + ") in " + menu.getClass());
      } else {
         menu.sendMessage(BcMenu.NET_WIDGET, buf -> {
            buf.writeShort(widgetId);
            writer.write(buf);
         });
      }
   }

   static void readMessage(
      AbstractContainerMenu menu,
      Player player,
      List<Widget_Neptune<?>> widgets,
      Supplier<ItemHandlerSimple> bucketSlots,
      int id,
      FriendlyByteBuf buffer,
      boolean isClient,
      BCPayloadContext ctx
   ) {
      if (id == BcMenu.NET_WIDGET) {
         int widgetId = buffer.readUnsignedShort();
         if (widgetId < 0 || widgetId >= widgets.size()) {
            BCLog.logger.warn("[lib.container] Received invalid widget ID " + widgetId + " (have " + widgets.size() + " widgets)");
            return;
         }

         Widget_Neptune<?> widget = widgets.get(widgetId);

         try {
            if (isClient) {
               widget.handleWidgetDataClient(ctx, buffer);
            } else {
               widget.handleWidgetDataServer(ctx, buffer);
            }
         } catch (Exception e) {
            BCLog.logger.warn("[lib.container] Error handling widget data for widget " + widgetId, e);
         }
      } else if (id == BcMenu.NET_GHOST_SLOT_SET && !isClient) {
         int slotIdx = buffer.readUnsignedShort();
         String itemId = buffer.readUtf();
         if (slotIdx >= 0 && slotIdx < menu.slots.size() && menu.slots.get(slotIdx) instanceof SlotPhantom phantom) {
            Identifier itemIdentifier = Identifier.tryParse(itemId);
            if (itemIdentifier == null) {
               return;
            }

            Item bcItem = BcRegistryUtil.getItem(itemIdentifier);
            if (bcItem != null) {
               phantom.set(new ItemStack((ItemLike) bcItem, 1));
            }
         }
      } else if (id == BcMenu.NET_BUCKET_TRANSFER && !isClient) {
         ItemHandlerSimple machineSlots = bucketSlots.get();
         if (machineSlots != null) {
            BucketRecipeTransfer.apply(buffer, player.getInventory(), machineSlots);
         }
      }
   }

   static boolean clickPhantom(AbstractContainerMenu menu, int slotId) {
      if ((slotId < 0 ? null : menu.slots.get(slotId)) instanceof SlotPhantom phantom) {
         ItemStack held = menu.getCarried();
         if (held.isEmpty()) {
            phantom.set(ItemStack.EMPTY);
         } else {
            ItemStack copy = held.copy();
            copy.setCount(1);
            phantom.set(copy);
         }

         return true;
      }

      return false;
   }

   static ItemStack quickMoveStack(AbstractContainerMenu menu, int index, StackMover mover) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = menu.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack slotStack = slot.getItem();
         itemstack = slotStack.copy();
         int playerInvSize = 36;
         int containerSlots = menu.slots.size() - playerInvSize;
         if (index < containerSlots) {
            if (!mover.move(slotStack, containerSlots, menu.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!moveItemStackToValid(menu, slotStack, 0, containerSlots)) {
            return ItemStack.EMPTY;
         }

         if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         return itemstack;
      } else {
         return itemstack;
      }
   }

   private static boolean moveItemStackToValid(AbstractContainerMenu menu, ItemStack stack, int startIndex, int endIndex) {
      boolean moved = false;

      for (int i = startIndex; i < endIndex && !stack.isEmpty(); i++) {
         Slot targetSlot = menu.slots.get(i);
         if (targetSlot.mayPlace(stack)) {
            ItemStack existing = targetSlot.getItem();
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(stack, existing)) {
               int maxSize = Math.min(targetSlot.getMaxStackSize(stack), stack.getMaxStackSize());
               int space = maxSize - existing.getCount();
               if (space > 0) {
                  int transfer = Math.min(space, stack.getCount());
                  existing.grow(transfer);
                  stack.shrink(transfer);
                  targetSlot.set(existing);
                  moved = true;
               }
            }
         }
      }

      for (int i = startIndex; i < endIndex && !stack.isEmpty(); i++) {
         Slot targetSlot = menu.slots.get(i);
         if (targetSlot.mayPlace(stack) && targetSlot.getItem().isEmpty()) {
            int maxSize = Math.min(targetSlot.getMaxStackSize(stack), stack.getMaxStackSize());
            int transfer = Math.min(maxSize, stack.getCount());
            targetSlot.set(stack.split(transfer));
            moved = true;
         }
      }

      return moved;
   }
}
