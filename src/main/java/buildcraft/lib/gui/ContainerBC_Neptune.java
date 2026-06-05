/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.Unpooled;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.world.inventory.ContainerInput;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.fabric.PacketDistributor;
import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.api.core.BCLog;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.BCPacketLimits;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.MessageContainerPayload;
import buildcraft.lib.net.PacketBufferBC;

@SuppressWarnings("unchecked")
public abstract class ContainerBC_Neptune extends RecipeBookMenu {

    public static final int NET_WIDGET = 0;

    public static final int NET_JEI_RECIPE_TRANSFER = 100;

    public static final int NET_GHOST_SLOT_SET = 101;

    public static final int NET_JEI_TRANSFER_ITEMS = 102;

    public static final int NET_JEI_TRANSFER_BUCKETS = 103;

    public final Player player;
    private final List<Widget_Neptune<?>> widgets = new ArrayList<>();

    protected ContainerBC_Neptune(MenuType<?> menuType, int containerId, Player player) {
        super(menuType, containerId);
        this.player = player;
    }

    protected void addFullPlayerInventory(int startX, int startY) {
        addFullPlayerInventory(startX, startY, player.getInventory());
    }

    protected void addFullPlayerInventory(int startX, int startY, Inventory inv) {
        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlot(new Slot(inv, sx + sy * 9 + 9, startX + sx * 18, startY + sy * 18));
            }
        }
        for (int sx = 0; sx < 9; sx++) {
            addSlot(new Slot(inv, sx, startX + sx * 18, startY + 58));
        }
    }

    public <W extends Widget_Neptune<?>> W addWidget(W widget) {
        if (widget == null) throw new NullPointerException("widget");
        widgets.add(widget);
        return widget;
    }

    public ImmutableList<Widget_Neptune<?>> getWidgets() {
        return ImmutableList.copyOf(widgets);
    }

    public final void sendMessage(int id, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());
        writer.write(buffer);
        int size = buffer.readableBytes();
        if (size > BCPacketLimits.MAX_PAYLOAD_BYTES) {
            buffer.release();
            BCLog.logger.warn("[lib.container] Container message {} exceeds payload limit ({} bytes)", id, size);
            return;
        }
        byte[] bytes = new byte[size];
        buffer.readBytes(bytes);
        buffer.release();

        MessageContainerPayload payload = new MessageContainerPayload(containerId, id, bytes);
        if (player.level().isClientSide()) {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);
        } else if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, payload);
        }
    }

    void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer) {
        int widgetId = widgets.indexOf(widget);
        if (widgetId == -1) {
            BCLog.logger.warn("[lib.container] sendWidgetData: widget not found! ("
                + (widget == null ? "null" : widget.getClass()) + ") in " + getClass());
            return;
        }
        sendMessage(NET_WIDGET, (buf) -> {
            buf.writeShort(widgetId);
            writer.write(buf);
        });
    }

    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        if (id == NET_WIDGET) {
            int widgetId = buffer.readUnsignedShort();
            if (widgetId < 0 || widgetId >= widgets.size()) {
                BCLog.logger.warn("[lib.container] Received invalid widget ID " + widgetId
                    + " (have " + widgets.size() + " widgets)");
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
        } else if (id == NET_JEI_RECIPE_TRANSFER && !isClient) {

            Identifier recipeId = Identifier.tryParse(buffer.readUtf());
            if (recipeId == null) {
                return;
            }
            if (player.level() instanceof ServerLevel serverLevel) {
                net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> key =
                        net.minecraft.resources.ResourceKey.create(
                                net.minecraft.core.registries.Registries.RECIPE, recipeId);
                Optional<RecipeHolder<CraftingRecipe>> holder = serverLevel.recipeAccess()
                        .byKey(key)
                        .filter(r -> r.value() instanceof CraftingRecipe)
                        .map(r -> (RecipeHolder<CraftingRecipe>) (RecipeHolder<?>) r);
                holder.ifPresent(recipe -> handlePlacement(
                        false, player.isCreative(), recipe,
                        serverLevel, player.getInventory()));
            }
        } else if (id == NET_GHOST_SLOT_SET && !isClient) {

            int slotIdx = buffer.readUnsignedShort();
            String itemId = buffer.readUtf();
            if (slotIdx >= 0 && slotIdx < slots.size() && slots.get(slotIdx) instanceof SlotPhantom phantom) {
                Identifier itemIdentifier = Identifier.tryParse(itemId);
                if (itemIdentifier == null) {
                    return;
                }
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemIdentifier).ifPresent(itemRef -> {
                    ItemStack stack = new ItemStack(itemRef.value(), 1);
                    phantom.set(stack);
                });
            }
        }
    }

    @Override

    public void clicked(int slotId, int dragType, ContainerInput containerInput, Player player) {

        Slot slot = slotId < 0 ? null : this.slots.get(slotId);
        if (slot instanceof SlotPhantom) {
            SlotPhantom phantom = (SlotPhantom) slot;
            ItemStack held = getCarried();
            if (held.isEmpty()) {
                phantom.set(ItemStack.EMPTY);
            } else {
                ItemStack copy = held.copy();
                copy.setCount(1);
                phantom.set(copy);
            }
            return;
        }
        super.clicked(slotId, dragType, containerInput, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return itemstack;

        ItemStack slotStack = slot.getItem();
        itemstack = slotStack.copy();

        int playerInvSize = 36;
        int containerSlots = this.slots.size() - playerInvSize;

        if (index < containerSlots) {

            if (!this.moveItemStackTo(slotStack, containerSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {

            if (!moveItemStackToValid(slotStack, 0, containerSlots)) {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return itemstack;
    }

    private boolean moveItemStackToValid(ItemStack stack, int startIndex, int endIndex) {
        boolean moved = false;

        for (int i = startIndex; i < endIndex && !stack.isEmpty(); i++) {
            Slot targetSlot = this.slots.get(i);
            if (!targetSlot.mayPlace(stack)) continue;

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

        for (int i = startIndex; i < endIndex && !stack.isEmpty(); i++) {
            Slot targetSlot = this.slots.get(i);
            if (!targetSlot.mayPlace(stack)) continue;

            if (targetSlot.getItem().isEmpty()) {
                int maxSize = Math.min(targetSlot.getMaxStackSize(stack), stack.getMaxStackSize());
                int transfer = Math.min(maxSize, stack.getCount());
                targetSlot.set(stack.split(transfer));
                moved = true;
            }
        }

        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public PostPlaceAction handlePlacement(boolean useMaxItems, boolean isCreative, RecipeHolder<?> recipe,
        ServerLevel level, Inventory playerInv) {
        return PostPlaceAction.NOTHING;
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents contents) {

    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }
}
